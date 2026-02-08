import redisDB from "../../../global/db/redis/RedisDB.js";
import logger from "../../../global/config/logger.js";

export class RedisClient {
    constructor() {
        this.pubClient = redisDB.getPubClient();
        this.subClient = redisDB.getSubClient();
        this.RECONNECT_PREFIX = "websocket:reconnect:timer:";
        this.GAME_TIMER_PREFIX = "room:game:timer:";
        this.CCTV_TIMER_PREFIX = "room:game:cctv:";
        this.GAME_END_PREFIX = "room:game:end:";
    }

    /**
     * 웹소켓 연결 관련 레디스 캐시
     */
    setReconnectLock = async (memberId) => {
        return await this.pubClient.set(`websocket:reconnect:lock:${memberId}`, "locked", "NX", "EX", 5);
    }

    deleteKeys = async (namespace, roomId, memberId) => {
        await this.pubClient.del(`websocket:reconnect:lock:${memberId}`);
        await this.pubClient.del(`websocket:reconnect:info:${namespace}:${memberId}`);
        await this.pubClient.del(`websocket:reconnect:timer:${namespace}:${roomId}:${memberId}`);
    }

    deleteByCompletedReconnect = async (socket, namespace, storedRoomId) => {
        logger.info(`[Reconnect] Restoring user ${socket.data.memberId} to room ${storedRoomId}`);
        socket.join(storedRoomId);

        if (namespace === 'chat') {
            socket.data.chatRoomId = storedRoomId;
        } else if (namespace === 'game') {
            socket.data.gameId = storedRoomId;
        } else if (namespace === 'room') {
            socket.data.roomId = storedRoomId;
        }

        // Delete keys to cancel expiration event
        const timerKey = `websocket:reconnect:timer:${namespace}:${storedRoomId}:${socket.data.memberId}`;
        const infoKey = `websocket:reconnect:info:${namespace}:${socket.data.memberId}`;

        await this.pubClient.del(infoKey);
        await this.pubClient.del(timerKey);
    }

    getStoredRoomId = async (socketOrMemberId, namespace) => {
        let memberId = socketOrMemberId;
        if (socketOrMemberId.data && socketOrMemberId.data.memberId) {
            memberId = socketOrMemberId.data.memberId;
        }

        const infoKey = `websocket:reconnect:info:${namespace}:${memberId}`;
        const storedRoomId = await this.pubClient.get(infoKey);
        return parseInt(storedRoomId);
    }

    pubReconnectTimer = async (namespace, socket, roomId) => {
        const timerKey = `websocket:reconnect:timer:${namespace}:${roomId}:${socket.data.memberId}`;
        const infoKey = `websocket:reconnect:info:${namespace}:${socket.data.memberId}`;

        // 1. Timer Key: Expiration event trigger (Value not important)
        await this.pubClient.set(timerKey, "timer", "EX", 60);

        // 2. Info Key: Data storage for reconnection (Value = chatRoomId)
        // Set to 61s to ensure it survives slightly longer than the timer (race condition safety)
        await this.pubClient.set(infoKey, roomId, "EX", 60);
    }

    setAccessToken = async (memberId, accessToken, timeLimit) => {
        const infoKey = `websocket:access:token:${memberId}`;
        await this.pubClient.set(infoKey, accessToken, "EX", timeLimit ? (parseInt(timeLimit) + 5) * 60 : 30 * 60);
    }

    getAccessToken = async (memberId) => {
        const infoKey = `websocket:access:token:${memberId}`;
        const accessToken = await this.pubClient.get(infoKey);
        return accessToken;
    }

    deleteAccessToken = async (memberId) => {
        const infoKey = `websocket:access:token:${memberId}`;
        logger.info(`[RedisClient] Deleting access token for member ${memberId}`);
        await this.pubClient.del(infoKey);
    }


    /**
     * 게임 관련 레디스 캐시
     * 
     * 게임 타이머
     * gps + 멤버스탯
     * 패널티
     * 게임 세팅
     * 
     */

    /**
     * 게임세팅
     */
    setGameSetting = async (gameId, gameSetting) => {
        await this.pubClient.set(this.getGameSettingString(gameId), JSON.stringify(gameSetting));
    }

    getGameSetting = async (gameId) => {
        const gameSetting = await this.pubClient.get(this.getGameSettingString(gameId));
        return JSON.parse(gameSetting);
    }

    deleteGameSetting = async (gameId) => {
        await this.pubClient.del(this.getGameSettingString(gameId));
    }

    setGameSettingLock = async (gameId, time) => {
        const duration = parseInt(time);
        if (isNaN(duration)) {
            logger.warn(`[RedisClient] Invalid duration for GameSettingLock: ${time}. Defaulting to 3600s.`);
            return await this.pubClient.set(`room:game:setting:lock:${gameId}`, "locked", "NX", "EX", 5);
        }
        return await this.pubClient.set(`room:game:setting:lock:${gameId}`, "locked", "NX", "EX", duration * 60);
    }

    deleteGameSettingLock = async (gameId) => {
        await this.pubClient.del(`room:game:setting:lock:${gameId}`);
    }

    /**
     * 게임 토큰
     * 
     * 게임 종료 및 초기화 api 호출 시 사용할 토큰
     */
    setGameToken = async (gameId, token, timeLimit) => {
        await this.pubClient.set(this.getGameTokenString(gameId), token, "EX", (timeLimit + 5) * 60);
    }

    getGameToken = async (gameId) => {
        const token = await this.pubClient.get(this.getGameTokenString(gameId));
        return token;
    }

    deleteGameToken = async (gameId) => {
        await this.pubClient.del(this.getGameTokenString(gameId));
    }

    getGameTokenString = (gameId) => {
        return `room:game:token:${gameId}`;
    }

    /**
     * 게임 시작
     */
    setStartedCount = async (gameId, memberId) => {
        return parseInt(await this.pubClient.sadd(this.getStartedString(gameId), memberId));
    }

    getStartedCount = async (gameId) => {
        const startedCount = await this.pubClient.scard(this.getStartedString(gameId));
        return startedCount;
    }

    deleteStartedCount = async (gameId) => {
        await this.pubClient.del(this.getStartedString(gameId));
    }

    /**
     * GPS + 멤버스텟
     */
    setLocation = async (memberId, gameId, location) => {
        await this.pubClient.hset(this.getLocationKeyString(gameId), memberId, JSON.stringify(location));
    }

    getLocation = async (memberId, gameId) => {
        const location = await this.pubClient.hget(this.getLocationKeyString(gameId), memberId);
        return JSON.parse(location);
    }

    getAllLocations = async (gameId) => {
        const locations = await this.pubClient.hgetall(this.getLocationKeyString(gameId));
        return Object.entries(locations).map(([memberId, location]) => {
            return {
                memberId,
                gameId,
                ...JSON.parse(location)
            };
        });
    }

    deleteLocation = async (memberId, gameId) => {
        await this.pubClient.hdel(this.getLocationKeyString(gameId), memberId);
    }

    deleteAllLocations = async (gameId) => {
        await this.pubClient.del(this.getLocationKeyString(gameId));
    }

    /**
     * 패널티
     */
    /**
     * 경계선 밖으로 나갔을 시에 10초 동안 패널티 부여 안함.
     */
    increasePenalty = async (memberId, gameId) => {
        const lockKey = this.getPenaltyLockString(gameId, memberId);
        const isLocked = await this.pubClient.get(lockKey);

        if (isLocked) {
            return;
        }

        const res = await this.pubClient.hincrby(this.getPenaltyKeyString(gameId), memberId, 1);
        await this.pubClient.set(lockKey, "1", "EX", 10);
        logger.info(`패널티 부여, member=${memberId}`);
        return res;
    }

    getPenalty = async (memberId, gameId) => {
        const penalty = await this.pubClient.hget(this.getPenaltyKeyString(gameId), memberId);
        return parseInt(penalty);
    }

    deletePenalty = async (memberId, gameId) => {
        await this.pubClient.hdel(this.getPenaltyKeyString(gameId), memberId);
    }

    deleteAllPenalties = async (gameId) => {
        await this.pubClient.del(this.getPenaltyKeyString(gameId));
    }

    getLocationKeyString = (gameId, memberId) => {
        if (memberId) {
            return `room:game:${gameId}:locations:${memberId}`;
        }
        return `room:game:${gameId}:locations`;
    }

    /**
     * 미션 관련
     */
    setMission = async (gameId, memberId, missionId) => {
        await this.pubClient.hset(this.getMissionKeyString(gameId), memberId, missionId);
    }

    getMission = async (gameId, memberId) => {
        const missionId = await this.pubClient.hget(this.getMissionKeyString(gameId), memberId);
        return missionId;
    }

    deleteAllMissions = async (gameId) => {
        await this.pubClient.del(this.getMissionKeyString(gameId));
    }

    getMissionKeyString = (gameId) => {
        return `room:game:mission:${gameId}`;
    }

    /**
     * news 관련
     */
    setNews = async (gameId, newsId) => {
        await this.pubClient.set(this.getNewsKeyString(gameId), newsId, "EX", 60);
    }

    getNews = async (gameId) => {
        const newsId = await this.pubClient.get(this.getNewsKeyString(gameId));
        return newsId ? parseInt(newsId) : null;
    }

    deleteNews = async (gameId) => {
        await this.pubClient.del(this.getNewsKeyString(gameId));
    }

    getNewsKeyString = (gameId) => {
        return `room:game:news:${gameId}`;
    }

    /**
     * skill use lock
     */
    setSkillUsedAt = async (gameId, timeLimit) => {
        logger.info(`skill use lock set, game=${gameId}, timeLimit=${new Date().toISOString()}`);
        await this.pubClient.set(this.getSkillUseLockString(gameId), new Date().toISOString(), "EX", timeLimit * 60);
    }

    getSkillUsedAt = async (gameId) => {
        const skillUseLock = await this.pubClient.get(this.getSkillUseLockString(gameId));
        return skillUseLock;
    }

    deleteSkillUsedAt = async (gameId) => {
        await this.pubClient.del(this.getSkillUseLockString(gameId));
    }

    getSkillUseLockString = (gameId) => {
        return `room:game:skill:use:lock:${gameId}`;
    }

    /**
     * cctv user 
     */

    setCctvUser = async (gameId, memberId) => {
        await this.pubClient.set(this.getCctvUserString(gameId), memberId);
    }

    getCctvUser = async (gameId) => {
        const memberId = await this.pubClient.get(this.getCctvUserString(gameId));
        return memberId;
    }

    deleteCctvUser = async (gameId) => {
        await this.pubClient.del(this.getCctvUserString(gameId));
    }

    getCctvUserString = (gameId) => {
        return `room:game:cctv:user:${gameId}`;
    }

    // 이 함수는 게임이 종료됐을때만 실행.
    deleteGameCachesByMemberId = async (memberId, gameId) => {
        // 게임에서 쓰는 redis cache들 삭제 (내 위치 정보 삭제, 경계 벗어남 패널티 횟수 관리 정보 삭제)
        await this.pubClient.hdel(this.getLocationKeyString(gameId), memberId);
        await this.pubClient.hdel(this.getPenaltyKeyString(gameId), memberId);
    }

    deleteAllInGameCachesByGameId = async (gameId) => {
        await this.deleteAllLocations(gameId);
        await this.deleteAllPenalties(gameId);
        await this.deleteCctvTimer(gameId);
        await this.deleteStartedCount(gameId);
        await this.deleteGameToken(gameId);
        await this.deleteGameSetting(gameId);
        await this.deleteGameTimer(gameId);
        await this.deleteGameTimerLock(gameId);
        await this.deleteGameSettingLock(gameId);
        await this.deleteSkillUsedAt(gameId);
        await this.deleteCctvUser(gameId);
    }
    /**
     * 게임 타이머 => 게임 진행 시간 관리
     */
    setGameTimerLock = async (gameId) => {
        // 키가 존재할땐 false 반환, 키가 존재하지 않을땐 생성후 true 반환
        return await this.pubClient.set(this.getGameTimerLockKeyString(gameId), "locked", "NX", "EX", 5);
    }

    deleteGameTimerLock = async (gameId) => {
        await this.pubClient.del(this.getGameTimerLockKeyString(gameId));
    }

    // time은 초단위
    setGameTimer = async (gameId, time) => {
        const duration = parseInt(time);
        if (isNaN(duration)) {
            logger.warn(`[RedisClient] Invalid duration for GameTimer: ${time}. Defaulting to 300s.`);
            return await this.pubClient.set(this.getGameTimerKeyString(gameId), Date.now().toString(), "EX", 300);
        }
        logger.info(`[RedisClient] Set GameTimer: ${gameId}, ${duration * 60}초`);
        return await this.pubClient.set(this.getGameTimerKeyString(gameId), Date.now().toString(), "EX", duration * 60);
    }

    getGameTimer = async (gameId) => {
        const gameTimer = await this.pubClient.get(this.getGameTimerKeyString(gameId));
        return parseInt(gameTimer);
    }

    deleteGameTimer = async (gameId) => {
        await this.pubClient.del(this.getGameTimerKeyString(gameId));
    }

    /**
     * CCTV
     */
    setCctvTimer = async (gameId, time) => {
        const duration = parseInt(time);
        if (isNaN(duration)) {
            logger.warn(`[RedisClient] Invalid duration for CctvTimer: ${time}. Defaulting to 60s.`);
            return await this.pubClient.set(this.getCctvTimerKeyString(gameId), "timer", "EX", 60);
        }
        return await this.pubClient.set(this.getCctvTimerKeyString(gameId), "timer", "EX", duration * 60);
    }

    deleteCctvTimer = async (gameId) => {
        await this.pubClient.del(this.getCctvTimerKeyString(gameId));
    }

    getCctvTimerKeyString = (gameId) => {
        return `room:game:cctv:${gameId}`;
    }

    /**
     * 게임에 관한 모든 캐시 삭제
     * 
     * 개발때 쓸
     * 
     * 게임이 종료되고 
     */
    deleteAllGameCachesByGameId = async (gameId) => {
        await this.deleteAllLocations(gameId);
        await this.pubClient.del(this.getPenaltyKeyString(gameId));
        await this.pubClient.del(this.getGameSettingString(gameId));
        await this.pubClient.del(this.getGameSettingLockString(gameId));
        await this.deleteGameTimer(gameId);
        await this.deleteGameTimerLock(gameId);
        await this.pubClient.del(this.getStartedString(gameId));
        await this.deleteStartedCount(gameId);
        await this.deleteCctvTimer(gameId);
    }

    /**
     * 게임 종료 후 1분 동안만 유지
     */
    setGameEnd = async (gameId) => {
        await this.pubClient.set(this.getGameEndKeyString(gameId), "end", "EX", 60);
    }

    getGameEnd = async (gameId) => {
        const gameEnd = await this.pubClient.get(this.getGameEndKeyString(gameId));
        return gameEnd;
    }

    deleteGameEnd = async (gameId) => {
        await this.pubClient.del(this.getGameEndKeyString(gameId));
    }

    getGameEndKeyString = (gameId) => {
        return `room:game:end:${gameId}`;
    }


    getGameTimerLock = async (gameId) => {
        return await this.pubClient.get(this.getGameTimerLockKeyString(gameId));
    }

    getGameTimerKeyString = (gameId) => {
        return `room:game:timer:${gameId}`;
    }

    getGameTimerLockKeyString = (gameId) => {
        return `room:game:timer:lock:${gameId}`;
    }

    /**
     * Active Game Management for GPS Worker
     */
    addActiveGame = async (gameId) => {
        await this.pubClient.sadd(this.getActiveGameKeyString(), gameId);
    }

    removeActiveGame = async (gameId) => {
        await this.pubClient.srem(this.getActiveGameKeyString(), gameId);
    }

    getActiveGames = async () => {
        return await this.pubClient.smembers(this.getActiveGameKeyString());
    }

    getActiveGameKeyString = () => {
        return "room:game:active:list";
    }



    getPenaltyKeyString = (gameId, memberId) => {
        if (memberId) {
            return `room:game:penalty:${gameId}:${memberId}`;
        }
        return `room:game:penalty:${gameId}`;
    }

    getPenaltyLockString = (gameId, memberId) => {
        return `room:game:penalty:lock:${gameId}:${memberId}`;
    }

    getGameSettingString = (gameId) => {
        return `room:game:setting:${gameId}`;
    }

    getGameSettingLockString = (gameId) => {
        return `room:game:setting:lock:${gameId}`;
    }

    getStartedString = (gameId) => {
        return `room:game:started:${gameId}`;
    }
}