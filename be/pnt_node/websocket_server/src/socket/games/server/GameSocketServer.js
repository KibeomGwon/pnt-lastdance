import { GameController } from "../controller/GameController.js";
import { resolveInSocket } from "../../../global/auth/JwtResolver.js";
import { RedisClient } from "../../utils/client/RedisClient.js";
import MessagingQueue from "../../../global/mq/MessagingQueue.js";
import { sendError } from "../../../global/util/SocketError.js";
import { withLogging } from "../../../global/util/socketWrapper.js";
import logger from "../../../global/config/logger.js";

const redisClient = new RedisClient();

const gameSocketServer = (io) => {
    io.use(resolveInSocket);

    // 연결 타이머가 만료되었을 때만, inGameConnected = false로 변경 : 이건 ExpiredChannel에서 수행
    io.on("connection", async (socket) => {
        try {
            const storedGameId = await redisClient.getStoredRoomId(socket, "game");

            const mq = await MessagingQueue.create();

            socket.data.isIntentionalExit = false; // 사용자의 요청에 의해서 소켓이 종료되었는지 판별하기 위한 변수
            const gameController = new GameController(io, socket, mq);

            let isActiveRoom = null;
            let integerGameId = null;

            if (storedGameId) {
                integerGameId = parseInt(storedGameId);
                isActiveRoom = await gameController.isActiveRoom(integerGameId);
            }
            // game 방이 유효한지 검사 로직 필요.
            if (isActiveRoom) {
                try {
                    await redisClient.deleteByCompletedReconnect(socket, "game", storedGameId);
                    await gameController.gameMemberService.updateInGameConnected(integerGameId, socket.data.memberId, true);
                    logger.info("reconnect", storedGameId);

                    socket.emit("reconnect", { gameId: storedGameId });
                } catch (error) {
                    logger.error(`[GameSocketServer] Reconnect failed partially for user ${socket.data.memberId}:`, error.message);
                    // 진행을 막지 않음. 소켓은 이미 룸에 조인되어 있음(deleteByCompletedReconnect 내부에서).
                }
            } else {
                logger.info(`[GameSocketServer] Reconnect failed for user ${socket.data.memberId}:`, "Game not found");
            }

            // 게임 관련 이벤트
            socket.on("post join room", withLogging("joinRoom", gameController.joinRoom, socket, "GameError"));
            socket.on("post gps", withLogging("postGps", gameController.postGps, socket, "GameError"));
            socket.on("post arrest", withLogging("postArrest", gameController.postArrest, socket, "GameError"));
            socket.on("post skill use", withLogging("postSkillUse", gameController.postSkillUse, socket, "GameError"));
            socket.on("post mission image", withLogging("postMissionImage", gameController.postMissionImage, socket, "GameError"));
            socket.on("post after game end", withLogging("postGameEndAfter", gameController.postGameEndAfter, socket, "GameError"));
            socket.on("post sync game info", withLogging("syncGameInfo", gameController.syncGameInfo, socket, "GameError"));

            socket.on("post reset game", withLogging("gameReset", gameController.gameReset, socket, "GameError"));

            socket.on("post disconnect", withLogging("disconnect", gameController.disconnect, socket, "GameError"));

            socket.on("post retry end game", withLogging("retryEndGame", gameController.retryEndGame, socket, "GameError"));

            socket.on("post update access token", withLogging("postUpdateAccessToken", gameController.postUpdateAccessToken, socket, "GameError"));

            socket.on("post radio", withLogging("postRadio", gameController.postRadio, socket, "GameError"));

            socket.on("disconnect", async () => {
                if (socket.data.isIntentionalExit) {
                    logger.info("socket의 연결이 정상적으로 끊어졌습니다.");
                    return;
                } else {
                    // 비정상적인 소켓 종료 => 채팅방 퇴장 db 처리 X
                    logger.info(`socket의 연결이 비정상적으로 끊어졌습니다. (Room: ${socket.data.gameId})`);

                    if (socket.data.gameId) {
                        await redisClient.pubReconnectTimer("game", socket, socket.data.gameId);
                        logger.info("[GAME] pubReconnectTimer", socket.data.gameId);
                    }
                }
            });
        } catch (error) {
            logger.error("gameSocketServer error", error);
            sendError(socket, error, "GameError");
        }
    });

}

export default gameSocketServer;