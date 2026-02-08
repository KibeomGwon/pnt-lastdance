import { ChatController } from "../controller/ChatController.js";
import { resolveInSocket } from "../../../global/auth/JwtResolver.js";
import { RedisClient } from "../../utils/client/RedisClient.js";
import { sendError } from "../../../global/util/SocketError.js";
import { withLogging } from "../../../global/util/socketWrapper.js";
import logger from "../../../global/config/logger.js";

const redisClient = new RedisClient();

const chatSocketServer = (io) => {
    io.use(resolveInSocket);

    io.on("connection", async (socket) => {
        try {
            const storedChatRoomId = parseInt(await redisClient.getStoredRoomId(socket, "chat"));

            if (storedChatRoomId) {
                await redisClient.deleteByCompletedReconnect(socket, "chat", storedChatRoomId);

                socket.emit("reconnect", { chatRoomId: storedChatRoomId });
            }

            logger.info("websocket is connected!");

            socket.data.isIntentionalExit = false; // 사용자의 요청에 의해서 소켓이 종료되었는지 판별하기 위한 변수
            const chatController = new ChatController(io, socket);

            // 채팅방 관련 이벤트
            socket.on("post join room", withLogging("joinRoom", chatController.joinRoom, socket, "ChatError"));
            socket.on("post message", withLogging("sendMessage", chatController.sendMessage, socket, "ChatError"));
            socket.on("post prev chat", withLogging("getPrevChat", chatController.getPrevChat, socket, "ChatError"));
            socket.on("post sync chat", withLogging("syncChat", chatController.syncChat, socket, "ChatError"));
            socket.on("post delegate owner", withLogging("delegateOwer", chatController.delegateOwer, socket, "ChatError"));
            socket.on("post kick member", withLogging("kickMember", chatController.kickMember, socket, "ChatError"));

            socket.on("post disconnect", withLogging("disconnect", chatController.disconnect, socket, "ChatError"));

            socket.on("disconnect", async () => {
                if (socket.data.isIntentionalExit) {
                    logger.info("socket의 연결이 정상적으로 끊어졌습니다.");
                    return;
                } else {
                    // 비정상적인 소켓 종료 => 채팅방 퇴장 db 처리 X
                    logger.info(`socket의 연결이 비정상적으로 끊어졌습니다. (Room: ${socket.data.chatRoomId})`);

                    if (socket.data.chatRoomId) {
                        await redisClient.pubReconnectTimer("chat", socket, socket.data.chatRoomId);
                        logger.info("[CHAT] pubReconnectTimer", socket.data.chatRoomId);
                    }
                }
            });
        } catch (error) {
            logger.error("chatSocketServer error", error);
            sendError(socket, error, "ChatError");
        }
    });
}

export default chatSocketServer;