# 게임 서버 트러블슈팅 — 체포 상태가 GPS 갱신에 덮어써지는 문제

측정일: 2026-02-13

[중복 실행 문제 2건](./troubleshooting.md)을 수정한 뒤, 같은 부하 환경에서 체포 처리를 반복하다가 발견했다.
앞의 두 건과 달리 **프로세스를 하나만 띄워도 발생한다.**

---

## 증상

경찰이 도둑을 체포하면 "체포 성공"이 정상으로 뜨는데, 서버에 저장된 도둑 상태가 이따금 `FREE`로 되돌아가 있었다.

그 도둑은 이렇게 된다.

- 감옥에 도착해도 `PRISON`으로 바뀌지 않는다. 전환 조건이 `status === TRANSFER`이기 때문이다.
- 게임 종료 판정에서 **자유 도둑으로 계산된다.** 마지막 도둑이었다면 게임이 끝나지 않는다.
- 다시 체포된다. `ALREADY_CAUGHT`에 걸리지 않으므로 경찰의 `arrest_count`가 한 번 더 올라간다.

체포 3,000회를 반복해 2회 발생했다. 약 1,500회에 한 번이라 플레이 중에는 거의 눈에 띄지 않는다.

## 원인

위치 정보는 멤버별로 **JSON 하나에 담겨** 해시 필드에 저장된다.

```
키     room:game:{gameId}:locations      (Hash)
필드   memberId
값     {"lat":..,"lng":..,"walk":..,"status":..,"penalty":..,"missionCompleted":..,"isConnected":..}
```

일부만 바꾸려 해도 전체를 읽고 전체를 써야 한다. `postGps`가 그렇게 되어 있었다.

```js
// GameController.postGps
const gameMember = await this.gameMemberService.findMemberGame(gameId, memberId);  // HGET
const status = gameMember.status;                                                  // 이때의 status
const penalty = await this.redisClient.getPenalty(memberId, gameId) || 0;          // HGET

const locationData = { lat, lng, walk, longestSurvived, position, status, penalty, ... };

await this.redisClient.setLocation(memberId, gameId, locationData);                // HSET
```

GPS가 실제로 바꿔야 할 값은 좌표와 걸음 수뿐인데, **읽어둔 `status`까지 함께 쓴다.**
읽기와 쓰기 사이에 체포가 `TRANSFER`를 쓰면, GPS의 쓰기가 그것을 덮는다.

```
GPS 읽기(FREE) → 체포 쓰기(TRANSFER) → GPS 쓰기(FREE)     ← 체포가 사라짐
```

Redis 명령은 하나씩은 원자적이다. 이 문제는 **읽기-수정-쓰기 전체가 원자적이지 않아서** 생긴다.
그래서 Redis 쪽 문제도, 프로세스를 여러 개 띄워서 생긴 문제도 아니다. Node가 `await`마다 다른 작업에 자리를 넘기는 동안 두 핸들러가 교차 실행된 것이다.

### 실제 명령 순서

지연을 주입해 틈을 벌린 상태의 Redis `MONITOR` 기록이다. (`docs/troubleshooting-logs/race-redis-monitor.log`)

| 시각 | 주체 | 명령 | |
|---|---|---|---|
| +0.0ms | GPS | `HGET locations 2` | status 읽음 |
| +52.2ms | 체포 | `HGET locations 2` · `1` | 도둑 · 경찰 확인 |
| +73.2ms | 체포 | `HSET locations 2 {"lat":0,…}` | **TRANSFER 기록** |
| +202.2ms | GPS | `HSET locations 2 {"lat":37.50004,…}` | **처음 읽은 status로 덮어씀** |

`HSET`의 `lat` 값으로 쓴 주체가 구분된다. 체포는 자기가 읽어둔 좌표(0)를, GPS는 방금 받은 좌표를 담고 있다.

덮어쓰기가 일어난 2건은 모두 도둑의 GPS가 **체포 요청보다 6~7ms 늦게** 전송된 경우였다.
체포는 Redis 확인과 DB 조회·갱신을 마친 뒤에야 `TRANSFER`를 쓰므로(처리 시간 p50 9ms), 그 직전에 GPS의 읽기가 끼어야 한다. 틈의 폭은 1~2ms다.

## 해결

Lua 스크립트로 병합을 Redis 안에서 처리하고, 각 핸들러가 **자기 필드만** 넘기게 했다.
스크립트 하나는 명령 하나로 실행되므로 읽기와 쓰기 사이에 다른 명령이 끼어들 수 없다.

```js
// RedisClient
this.pubClient.defineCommand("mergeLocation", {
    numberOfKeys: 1,
    lua: `
        local cur = redis.call('HGET', KEYS[1], ARGV[1])
        if not cur then return nil end
        local obj = cjson.decode(cur)
        for k, v in pairs(cjson.decode(ARGV[2])) do obj[k] = v end
        local enc = cjson.encode(obj)
        redis.call('HSET', KEYS[1], ARGV[1], enc)
        return enc
    `
});
```

```diff
- const locationData = { lat, lng, walk, longestSurvived, position, status, penalty, missionCompleted, isConnected: true, ... };
- await this.redisClient.setLocation(memberId, gameId, locationData);
+ const locationData = { lat, lng, walk, longestSurvived, timestamp: new Date().toISOString() };
+ const merged = await this.redisClient.mergeLocation(memberId, gameId, locationData);
+ const currentStatus = merged ? merged.status : status;
```

| 호출부 | 넘기는 필드 |
|---|---|
| `postGps` 정기 저장 | `lat` `lng` `walk` `longestSurvived` `timestamp` |
| `postGps` 경계 이탈 · 벌점 3회 | `penalty` / `status` + `penalty` |
| `updateMemberStatus` (체포 · 탈옥 · 감옥 도착 공통) | `status` |
| `updateInGameConnected` | `isConnected` |
| `missionComplete` | `missionCompleted` |

`postGps`는 병합 결과로 최신 `status`를 받아 이후 분기(탈옥 · 감옥 도착) 판정에 쓴다.
`joinRoom`의 최초 생성만 전체를 쓰고, 읽는 쪽(`getAllLocations` 7곳 · `getLocation` 5곳)은 저장 형식이 그대로라 수정하지 않았다.

`status`를 별도 키로 분리하는 방법도 있지만, 읽는 쪽 12곳을 전부 고쳐야 해서 택하지 않았다.

## 결과

| 검증 | 조건 | Before | After |
|---|---|---|---|
| 자연 발생률 | 체포 3,000회 | **2회** | **0회** |
| 메커니즘 | GPS 처리에 200ms 지연 주입 · 50회 | **50회 (100%)** | **0회** |
| DB | 위 조건에서 실제 체포 50번 | `arrest_count` **100** | **50** |
| 재체포 | 덮어써진 도둑 재체포 시도 | 50회 성공 | **0회** (`ALREADY_CAUGHT`) |

지연을 200ms로 벌려놓고도 0회가 나온 것이 핵심이다. 틈 자체가 없어졌다는 뜻이다.

### 회귀 확인

상태 전이와 전체 흐름을 다시 확인했다.

| 항목 | 결과 |
|---|---|
| 탈옥 (`PRISON` + 감옥 밖 GPS) | `get escape` 수신, `status = FREE` |
| 감옥 도착 (`TRANSFER` + 감옥 안 GPS) | `modify member status` 수신, `status = PRISON` |
| 벌점 3회 누적 | `penalty` 1 → 2 → `get arrest(PENALTY)`, `status = TRANSFER` · `penalty = 0` |
| 좌표 저장 | 보낸 좌표가 그대로 저장됨 |
| 1,000명 · 50게임 · 140초 | 종료 이벤트 1,000건 전원 수신, GPS 도착 간격 p50 1,003ms · p99 1,034ms |

## 검증 방법

체포 성공을 받은 뒤 GPS가 한 번 더 처리될 때까지 기다렸다가 저장된 `status`를 확인해, `TRANSFER`가 아니면 덮어쓰기로 집계했다.

- **자연 발생률**: 도둑 19명이 1초 주기로 GPS를 보내는 동안 무작위 시점에 체포를 반복한다.
- **메커니즘**: `getPenalty` 직후에 지연을 주입해 틈을 200ms로 벌리고, GPS 직후에 체포를 보낸다. 지연 주입은 환경변수로만 동작하며 측정 후 되돌렸다.

## 한계

- 로컬 환경이라 앱과 Redis · DB 사이 왕복이 짧다. 운영 환경의 지연이 다르면 틈의 위치와 폭도 달라진다.
- 부하가 걸려 이벤트 루프가 밀리면 틈이 넓어질 수 있는데, 그 조건은 측정하지 않았다.
- 같은 구조라 `missionCompleted`와 `isConnected`도 덮어써질 수 있다. 이번 수정으로 함께 닫혔지만 별도로 재현하지는 않았다.

---

## 원본 로그

`docs/troubleshooting-logs/`

| 파일 | 내용 |
|---|---|
| `race-redis-monitor.log` | 덮어쓰기 발생 시점의 Redis 명령 순서 (`MONITOR`) |
| `race-websocket-delay-injected.log` | 지연 주입 상태의 핸들러 로그. 체포보다 GPS가 늦게 끝나며 덮어쓰는 구간 |
