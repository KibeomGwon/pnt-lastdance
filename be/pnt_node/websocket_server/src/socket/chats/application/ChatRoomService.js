import ChatRoom from "../../../global/db/sequelize/entity/ChatRoom.js";
import MemberChatRoom from "../../../global/db/sequelize/entity/MemberChatRoom.js";

export class ChatRoomService {
    findChatRoom = async (chatRoomId) => {
        const chatRoom = await ChatRoom.findOne({
            where: {
                id: chatRoomId,
                isDeleted: false
            }
        });

        if (!chatRoom) {
            this.makeError("NotFoundException", "채팅방을 찾을 수 없습니다.", 404);
        }
    }

    findMemberChatRoom = async (chatRoomId, memberId) => {
        const memberChatRoom = await MemberChatRoom.findOne({
            where: {
                chatRoomId: chatRoomId,
                memberId: memberId
            }
        });

        if (!memberChatRoom) {
            this.makeError("NotFoundException", "유저의 채팅방 접속 정보를 찾을 수 없습니다.", 404);
        }
    }

    makeError = (message, text, code) => {
        const error = new Error(message);
        error.code = code;
        error.text = text;
        throw error;
    }
}