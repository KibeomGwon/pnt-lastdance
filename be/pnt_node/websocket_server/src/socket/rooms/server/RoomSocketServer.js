import { RoomController } from "../controller/RoomController.js";
import { resolveInSocket } from "../../../global/auth/JwtResolver.js";
import { RedisClient } from "../../utils/client/RedisClient.js";
import { sendError } from "../../../global/util/SocketError.js";
import { withLogging } from "../../../global/util/socketWrapper.js";
import logger from "../../../global/config/logger.js";

const redisClient = new RedisClient();

const roomSocketServer = (io) => {
    io.use(resolveInSocket);

    // 연결 타이머가 만료되었을 때만, inGameConnected = false로 변경 : 이건 ExpiredChannel에서 수행
    io.on("connection", async (socket) => {
        try {
            const storedRoomId = await redisClient.getStoredRoomId(socket, "room");
            logger.info(`[RoomSocketServer] storedRoomId: ${storedRoomId}`);

            socket.data.isIntentionalExit = false; // 사용자의 요청에 의해서 소켓이 종료되었는지 판별하기 위한 변수
            const roomController = new RoomController(io, socket);

            let isActiveRoom = null;
            const integerRoomId = parseInt(storedRoomId);

            if (integerRoomId) {
                isActiveRoom = await roomController.isActiveRoom(integerRoomId);
            }

            // game 방이 유효한지 검사 로직 필요.
            if (isActiveRoom && integerRoomId) {
                await redisClient.deleteByCompletedReconnect(socket, "room", integerRoomId);
                socket.data.roomId = integerRoomId;

                socket.emit("reconnect", { roomId: socket.data.roomId });
            }

            // 로비 관련 이벤트
            socket.on("post join room", withLogging("joinRoom", roomController.joinRoom, socket, "RoomError"));

            socket.on("post update room info", withLogging("updateRoomInfo", roomController.updateRoomInfo, socket, "RoomError"));

            socket.on("post update ready", withLogging("updateReady", roomController.updateReady, socket, "RoomError"));

            socket.on('post now ready info', withLogging("nowReadyInfo", roomController.nowReadyInfo, socket, "RoomError"));

            socket.on('post update position', withLogging("updatePreferPosition", roomController.updatePreferPosition, socket, "RoomError"));

            socket.on("post now room info", withLogging("nowRoomInfo", roomController.nowRoomInfo, socket, "RoomError"));

            socket.on("post member kick", withLogging("memberKick", roomController.memberKick, socket, "RoomError"));

            socket.on("post disconnect", withLogging("disconnect", roomController.disconnect, socket, "RoomError"));

            socket.on("post update access token", withLogging("postUpdateAccessToken", roomController.postUpdateAccessToken, socket, "RoomError"));

            socket.on("post game start", withLogging("gameStart", roomController.gameStart, socket, "RoomError"));

            socket.on("post delegate owner", withLogging("delegateOwner", roomController.delegateOwner, socket, "RoomError"));

            socket.on("disconnect", async () => {
                if (socket.data.isIntentionalExit) {
                    return;
                } else {
                    // 비정상적인 소켓 종료 => 채팅방 퇴장 db 처리 X
                    logger.info(`socket의 연결이 비정상적으로 끊어졌습니다. (Room: ${socket.data.roomId})`);

                    if (socket.data.roomId) {
                        await redisClient.pubReconnectTimer("room", socket, socket.data.roomId);
                        logger.info("reconnect timer published");
                    }
                }
            });
        } catch (error) {
            logger.error("roomSocketServer error", error);
            sendError(socket, error, "RoomError");
        }
    });
}

export default roomSocketServer;