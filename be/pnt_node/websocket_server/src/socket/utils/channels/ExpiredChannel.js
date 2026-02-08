import { RedisClient } from "../client/RedisClient.js";
import { GameController } from "../../games/controller/GameController.js";
import { GameMemberPosition } from "../../../global/db/sequelize/status/GameMemberPosition.js";
import { GameMemberStatus } from "../../../global/db/sequelize/status/GameMemberStatus.js";
import axios from "axios";
import { withFunctionLogging } from "../../../global/util/genericWrapper.js";
import logger from "../../../global/config/logger.js"

const redisClient = new RedisClient();
const gameController = new GameController();

const expiredChannel = withFunctionLogging("ExpiredChannel", async (message, pubClient, chatIo, roomIo, gameIo) => {

    const key = message;
    logger.info(`[ExpiredChannel] Received expired key: ${key}`);

    // Key format: room:game:timer:${gameId}
    // Example: room:game:timer:123
    if (key.startsWith(redisClient.GAME_TIMER_PREFIX)) {
        const parts = key.split(":");
        const gameId = parseInt(parts[3]);

        // parts[3]가 'lock' 문자열이거나, 파싱된 gameId가 NaN이면 무시
        if (parts[3] === 'lock' || isNaN(gameId)) return;

        // 게임 종료 처리 => 컨트롤러에서 처리
        logger.info("game end", gameId);
        gameController.gameEnd(gameIo, redisClient, gameId, GameMemberPosition.THIEF);
    }

    // Key format: room:game:cctv:${gameId}
    // Example: room:game:cctv:123
    else if (key.startsWith(redisClient.CCTV_TIMER_PREFIX)) {
        const parts = key.split(":");
        const gameId = parseInt(parts[3]);

        // 게임 중인 유저들의 위치 정보 조회
        const locations = await redisClient.getAllLocations(gameId);

        // 1. 도둑만 필터링
        // 2. 상태가 FREE 인 유저만 필터링
        const potentialThieves = locations.filter(player =>
            player.position === GameMemberPosition.THIEF &&
            (!player.status || player.status === GameMemberStatus.FREE)
            && player.isConnected === true
        );

        const thieves = [];
        for (const thief of potentialThieves) {
            const hasMission = await redisClient.getMission(gameId, thief.memberId);
            if (!hasMission) {
                thieves.push(thief);
            }
        }

        // 도둑의 수가 적으면 CCTV를 보내지 않음. => 이건 정해야함.
        if (thieves.length > 0) {
            const randomIndex = Math.floor(Math.random() * thieves.length);
            const randomThief = thieves[randomIndex];

            await redisClient.setCctvUser(gameId, randomThief.memberId);
            logger.info(`[CCTV] Game ${gameId}: Sent CCTV data for thief ${randomThief.memberId}`);
        } else {
            await redisClient.deleteCctvUser(gameId);
            logger.info(`[CCTV] Game ${gameId}: No free thieves found.`);
        }

        const gameSetting = await redisClient.getGameSetting(gameId);

        if (gameSetting) {
            const cctvInterval = gameSetting.cctvInterval || 60;
            await redisClient.setCctvTimer(gameId, cctvInterval);
        }
    }
    // Key format: room:game:end:${gameId}
    // Example: room:game:end:123
    else if (key.startsWith(redisClient.GAME_END_PREFIX)) {
        const parts = key.split(":");
        const gameId = parseInt(parts[3]);

        await redisClient.deleteGameEnd(gameId);

        try {
            const token = await redisClient.getGameToken(gameId);
            const response = await axios.post(`${process.env.SPRING_BOOT_URL}/rooms/${gameId}/reset`, {}, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                }
            });
        } catch (apiError) {
            logger.error(`[ExpiredChannel] API call failed for game ${gameId}:`, apiError.message);
        }

        await redisClient.deleteAllGameCachesByGameId(gameId);

        gameIo.to(gameId).emit("get game reset", { gameId: parseInt(gameId) });
    }
});

export default expiredChannel;