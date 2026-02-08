import MemberChatRoom from "../../../global/db/sequelize/entity/MemberChatRoom.js";
import FcmToken from "../../../global/db/sequelize/entity/FcmToken.js";
import ChatRoom from "../../../global/db/sequelize/entity/ChatRoom.js";
import { Op } from "sequelize";


export class ChatPushService {
    // 접속 안된 유저 가져오는 로직
    async getNotConnectedInRoom(params) {
        const joinRoomMembers = await MemberChatRoom.findAll({
            where: {
                isConnected: false,
                chatRoomId: parseInt(params.chatRoomId),
                isDeleted: false
            }
        });

        // 디버깅용: 조건 없이 해당 방의 모든 멤버 조회
        const allMembers = await MemberChatRoom.findAll({
            where: {
                chatRoomId: parseInt(params.chatRoomId),
                isDeleted: false
            }
        });

        const resData = [];

        joinRoomMembers.forEach((data) => resData.push(data.memberId));
        return resData;
    }

    // 유저 fcm 토큰 값 가져오는 로직
    async getUserTokens(params) {
        const fcmTokens = await FcmToken.findAll({
            where: {
                memberId: { [Op.in]: params },
                isActive: true,
                isDeleted: false
            }
        });
        const resData = [];

        fcmTokens.forEach((data) => resData.push(data.value));

        return resData;
    }

    async findChatRoomById(chatRoomId) {
        const chatRoom = await ChatRoom.findOne({
            where: {
                id: parseInt(chatRoomId),
                isDeleted: false
            }
        });

        return chatRoom;
    }
}