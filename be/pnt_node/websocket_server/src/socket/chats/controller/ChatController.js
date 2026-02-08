import { ChatService } from "../application/ChatService.js";
import { ChatRoomService } from "../application/ChatRoomService.js";
import mq from "../../../global/mq/MessagingQueue.js";
import { MQConfig } from "../../../global/mq/MQConfig.js";
import { sendError } from "../../../global/util/SocketError.js";
import { generateMemberAccessToken } from "../../../global/auth/JwtProvider.js";
import axios from "axios";
import logger from "../../../global/config/logger.js";

export class ChatController {

    constructor(io, socket) {
        this.io = io;
        this.socket = socket;
        this.chatService = new ChatService();
        this.chatRoomService = new ChatRoomService();
    }

    joinRoom = async (payload) => {
        // 채팅방 접속 db 처리 => is_connected = true로 처리
        const chatRoomId = parseInt(payload.chatRoomId);
        await this.chatRoomService.findChatRoom(chatRoomId);
        await this.chatRoomService.findMemberChatRoom(chatRoomId, this.socket.data.memberId);
        const strChatRoomId = String(chatRoomId);

        this.socket.join(strChatRoomId);
        this.socket.data.chatRoomId = strChatRoomId;

        const data = {
            "message": "joined room",
            "chatRoomId": chatRoomId
        }

        this.io.to(strChatRoomId).emit("get join room", data);
    }

    sendMessage = async (payload) => {
        if (!payload || !payload.content) {
            throw { code: 400, message: "Invalid payload: content is required" };
        }

        payload.memberId = this.socket.data.memberId;
        const chatRoomId = this.socket.data.chatRoomId;
        const strChatRoomId = String(chatRoomId);

        if (!chatRoomId) {
            throw { code: 400, message: "ChatRoomId is missing in socket data" };
        }

        const resData = await this.chatService.save({ chatRoomId, ...payload });

        // 푸시 알림 => 컨슈머에서 채팅방에 접속해 있지 않은 멤버를 확인후 푸시알림
        mq.sendMessage({ chatRoomId: strChatRoomId, ...payload }, MQConfig.MQ_ALARM);

        this.io.to(strChatRoomId).emit("get message", resData);
    }

    /**
     * payload = {
     *  "cursor" : 1,
     *  "limit" : 10
     * }
     */
    getPrevChat = async (payload) => {
        const { chatRoomId, memberId } = this.socket.data;

        if (!chatRoomId) {
            throw { code: 400, message: "ChatRoomId is missing in socket data" };
        }

        const strChatRoomId = String(chatRoomId);

        // 데이터 로딩 로직
        const data = await this.chatService.getPrevChat({ chatRoomId: strChatRoomId, memberId, ...payload });

        this.socket.emit("get prev chat", data);
    }

    /**
     * payload = {
     *  "cursor" : 1
     * }
     */
    syncChat = async (payload) => {
        const { chatRoomId, memberId } = this.socket.data;

        if (!chatRoomId) {
            throw { code: 400, message: "ChatRoomId is missing in socket data" };
        }

        const strChatRoomId = String(chatRoomId);

        // 데이터 로딩 로직
        const data = await this.chatService.syncChat({ chatRoomId: strChatRoomId, memberId, ...payload });

        this.socket.emit("get sync chat", data);
    }

    delegateOwer = async (payload) => {
        const chatRoomId = String(this.socket.data.chatRoomId);
        const targetMemberId = parseInt(payload.targetMemberId);
        const memberId = parseInt(this.socket.data.memberId);

        try {
            const accessToken = generateMemberAccessToken(memberId, -3);
            const response = await axios.post(`${process.env.SPRING_BOOT_URL}/chats/${chatRoomId}/owner`, { targetMemberId: targetMemberId }, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${accessToken}`
                }
            });
            this.io.to(chatRoomId).emit("get delegate owner", response.data);
        } catch (error) {
            if (error.response) {
                this.io.to(chatRoomId).emit("get delegate owner", error.response.data);
            } else {
                throw error;
            }
        }
    }

    kickMember = async (payload) => {
        logger.info("kick member", payload);
        const chatRoomId = String(this.socket.data.chatRoomId);
        const kickMemberId = parseInt(payload.kickMemberId);
        this.io.to(chatRoomId).emit("get kick member", { chatRoomId: parseInt(chatRoomId), kickMemberId: kickMemberId });
    }

    /**
     * 사용자 요청에 의한 채팅방 나가기 -> api 로 disconnect 호출
     * 
     * 웹소켓 연결 끊김 -> expiredChannel 에서 처리
     */
    disconnect = async () => {
        const memberId = this.socket.data.memberId;
        const chatRoomId = this.socket.data.chatRoomId;
        logger.info(`${memberId} 님이 소켓 연결을 종료하였습니다.`);

        this.socket.data.isIntentionalExit = true;

        this.socket.disconnect();
        this.io.to(chatRoomId).emit("get user left", { roomId: chatRoomId, memberId: memberId });
    }
}