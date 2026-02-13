# 게임 서버 트러블슈팅

측정일: 2026-02-12 (문제 확인) / 2026-02-13 (수정 후 재측정)

WebSocket 서버를 다중 프로세스로 운영하면서 발견한 **중복 실행 문제 2건**을 정리한다.

> 이후 같은 환경에서 발견한 [체포 상태 덮어쓰기 문제](./troubleshooting-race-condition.md)는 별도 문서로 정리했다.
둘 다 기능은 정상 동작해서 눈에 띄지 않았고, 부하 테스트 중 외부 API 호출 로그를 보고 발견했다.

측정 환경: 1000명(50게임 × 20명), WebSocket 2 프로세스, 종료 60초 · 관찰 140초

---

## ① 게임 종료 후 방 초기화가 프로세스 수만큼 실행됨

### 증상

게임이 끝나고 1분 뒤 실행되는 방 초기화 API가 **게임당 두 번씩** 호출됐다.

```
[12/Feb/2026:23:41:25]  POST /spring/rooms/1/reset  200
[12/Feb/2026:23:41:25]  POST /spring/rooms/1/reset  200   ← 같은 게임, 두 번
[12/Feb/2026:23:41:25]  POST /spring/rooms/2/reset  200
[12/Feb/2026:23:41:25]  POST /spring/rooms/2/reset  200   ← 같은 게임, 두 번
```

게임 50개인데 호출은 **100회**. 소켓 브로드캐스트(`get game reset`)도 인원의 2배인 2,000회가 나갔다.

### 원인

게임 타이머와 종료 상태를 **Redis 키 TTL**로 관리하고, 만료 이벤트를 구독해 후처리한다.

```js
// RedisEvent.js — 프로세스마다 각자 구독한다
await this.subClient.subscribe("__keyevent@0__:expired");
```

Redis pub/sub은 **구독자 전원에게** 메시지를 보낸다. 프로세스가 2개면 한 번의 키 만료에 핸들러가 2번 실행된다.

같은 파일의 게임 종료 분기에는 이미 락이 있었다.

```js
// GameController.gameEnd — 락 있음
const isMine = await redisClient.setGameTimerLock(integerGameId);
if (!isMine) return;          // 한 프로세스만 진행
```

**그런데 방 초기화 분기에는 같은 조치가 빠져 있었다.**

```js
// ExpiredChannel.js:77 — 락 없음
else if (key.startsWith(redisClient.GAME_END_PREFIX)) {
    await redisClient.deleteGameEnd(gameId);
    const response = await axios.post(`${SPRING_BOOT_URL}/rooms/${gameId}/reset`, ...);
    await redisClient.deleteAllGameCachesByGameId(gameId);
    gameIo.to(gameId).emit("get game reset", { gameId });
}
```

캐시 삭제는 멱등이라 문제가 없었지만, **외부 API 호출과 브로드캐스트는 그대로 중복**됐다.

### 해결

`gameEnd`와 동일하게 `SET NX`로 한 프로세스만 진입하도록 막았다.

```diff
 else if (key.startsWith(redisClient.GAME_END_PREFIX)) {
     const parts = key.split(":");
     const gameId = parseInt(parts[3]);
 
+    // 키 만료 이벤트는 구독 중인 모든 프로세스가 받는다.
+    // 락이 없으면 방 초기화 API 호출과 브로드캐스트가 프로세스 수만큼 중복 실행된다.
+    if (!await redisClient.setGameResetLock(gameId)) {
+        return;
+    }
+
     await redisClient.deleteGameEnd(gameId);
```

```js
// RedisClient.js
setGameResetLock = async (gameId) => {
    return await this.pubClient.set(`room:game:reset:lock:${gameId}`, "locked", "NX", "EX", 60);
}
```

### 결과

| | Before | After |
|---|---|---|
| `POST /rooms/{id}/reset` | **100회** | **50회** |
| `get game reset` 수신 | **2,000** | **1,000** |

---

## ② 게임 결과를 참여자 수만큼 조회해서 인원수만큼 브로드캐스트

### 증상

게임 결과 조회 API가 **게임당 20회** 호출됐다. 게임 50개 기준 1,000회다.

```
[12/Feb/2026:23:41:25]  GET /spring/games/1/result  200
[12/Feb/2026:23:41:25]  GET /spring/games/1/result  200
[12/Feb/2026:23:41:26]  GET /spring/games/1/result  200
[12/Feb/2026:23:41:26]  GET /spring/games/1/result  200
        ... 같은 게임에 대해 20번 반복 ...
```

같은 로그 파일에서 **결과 저장(`POST /games/result`)은 50회로 정상**이었다.
저장에는 락이 있고 조회에는 없다는 차이가 그대로 드러난다.

소켓 메시지는 더 심했다. 참여자 20명이 각자 요청하고 그 결과를 **매번 룸 전체에 브로드캐스트**하니, 게임당 400개(20 × 20)가 오갔다.

```
"get end game after": 20000      ← 1000명이 각자 20번씩 수신
```

### 원인

종료 후처리는 **참여자마다 호출되는 구조**다. 각자의 위치·패널티 캐시를 지워야 하기 때문이다.

```js
// GameController.postGameEndAfter — 참여자 수만큼 실행된다
postGameEndAfter = async (payload) => {
    const res = await axios.get(`${SPRING_BOOT_URL}/games/${gameId}/result`, ...);  // 20번
    await this.redisClient.deleteGameCachesByMemberId(memberId, gameId);            // 20번 (정상)
    this.io.to(gameId).emit("get end game after", res.data);                        // 20번 × 20명
}
```

**개인별로 필요한 건 캐시 삭제뿐인데, 결과 조회와 브로드캐스트까지 같이 반복**되고 있었다.

### 해결

관심사를 분리했다. 캐시 삭제는 그대로 참여자마다, 조회와 브로드캐스트는 락으로 게임당 1회.

```diff
-        const res = await axios.get(`${SPRING_BOOT_URL}/games/${gameId}/result`, ...);
-
-        // 사용자의 게임 캐시 삭제
-        await this.redisClient.deleteGameCachesByMemberId(memberId, gameId);
-
-        this.io.to(gameId).emit("get end game after", res.data);
+        // 사용자의 게임 캐시 삭제 (참여자마다 필요)
+        await this.redisClient.deleteGameCachesByMemberId(memberId, gameId);
+
+        // 결과 조회와 브로드캐스트는 게임당 한 번이면 충분하다.
+        if (!await this.redisClient.setEndGameAfterLock(gameId)) {
+            return;
+        }
+
+        const res = await axios.get(`${SPRING_BOOT_URL}/games/${gameId}/result`, ...);
+        this.io.to(gameId).emit("get end game after", res.data);
```

### 결과

| | Before | After |
|---|---|---|
| `GET /games/{id}/result` | **1,000회** | **50회** |
| `get end game after` 수신 | **20,000** | **1,000** |
| `[RECV] postGameEndAfter` | 1,000 | **1,000** (변화 없음) |

마지막 줄이 수정의 성격을 보여준다. **핸들러 호출 자체는 그대로 1,000회를 받되, 락이 걸러서 실제 조회만 50회로 줄었다.** 개인 캐시 삭제는 여전히 1,000회 수행된다.

---

## 전체 요약

| 지표 | Before | After | |
|---|---|---|---|
| `POST /games/result` (결과 저장) | 50 | 50 | 원래 정상 — 대조군 |
| `GET /games/{id}/result` (결과 조회) | **1,000** | **50** | 20배 → 1배 |
| `POST /rooms/{id}/reset` (방 초기화) | **100** | **50** | 2배 → 1배 |
| `get end game after` | **20,000** | **1,000** | 20배 → 1배 |
| `get game reset` | **2,000** | **1,000** | 2배 → 1배 |

## 배운 것

**다중 프로세스에서 "이벤트를 받았다"와 "내가 처리해야 한다"는 다르다.**
Redis pub/sub은 구독자 전원에게 보내고, 소켓 이벤트 핸들러는 연결마다 등록된다.
둘 다 **한 번만 실행되어야 하는 작업**을 담고 있으면 프로세스 수 또는 참여자 수만큼 중복된다.

게임 종료(`gameEnd`)에는 이미 락이 있었는데 나머지 두 곳에 빠져 있었다.
**같은 패턴이 필요한 자리를 놓친 것**이고, 기능이 정상 동작해서 드러나지 않았다.
외부 API 호출 로그를 켜고 나서야 보였다.

---

## 원본 로그

`docs/troubleshooting-logs/`

| 파일 | 내용 |
|---|---|
| `spring-access-before.log` / `-after.log` | Tomcat access log (HTTP 호출 전량) |
| `websocket-before.log` / `-after.log` | 핸들러 호출 로그 발췌 |

소켓 이벤트 수신 집계(`get end game after`, `get game reset`)는 부하 생성기 출력에서 얻었다.

access log는 `server.tomcat.accesslog` 설정으로 수집했다. 응답시간 필드(`%D`)의 단위는 마이크로초다.
