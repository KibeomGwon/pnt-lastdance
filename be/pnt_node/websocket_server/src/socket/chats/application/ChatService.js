import chatEntity from "../../../global/db/mongo/entity/chat.js";
import members from "../../../global/db/mongo/entity/member.js";
import moment from "moment-timezone";
import { getPresignedUrl } from "../../utils/S3Service.js";

export class ChatService {
    async save(chat) {
        const now = moment().tz("Asia/Seoul").format('YYYY-MM-DDTHH:mm:ssZ');
        chat.createdAt = now;

        /**
         * 정책을 정해서 avatarUrl이 없을 경우 기본 이미지 설정.
         */
        if (!chat.avatarUrl) {
            chat.avatarUrl = "default.png";
        }

        const chatModel = new chatEntity(chat);

        const data = await chatModel.save();

        const member = await members.findOne({ memberId: chat.memberId }).exec();

        data.member = member;

        return await this.parseChat(data);
    }

    getAggregationPipeline(matchStage, sortVariable, limit) {
        const pipeline = [
            { $match: matchStage },
            { $sort: { _id: sortVariable } },
        ];

        if (limit !== null && limit !== undefined) {
            pipeline.push({ $limit: limit });
        }

        pipeline.push(
            {
                $lookup: {
                    from: "members",
                    localField: "memberId",
                    foreignField: "memberId",
                    as: "memberInfo"
                }
            },
            {
                $unwind: {
                    path: "$memberInfo",
                    preserveNullAndEmptyArrays: true
                }
            },
            {
                $project: {
                    _id: 1,
                    content: 1,
                    memberId: 1,
                    chatRoomId: 1,
                    avatarUrl: 1,
                    createdAt: 1,
                    member: "$memberInfo"
                }
            }
        );

        return pipeline;
    }

    // {chatRoomId, cursor, limit}
    /**
     * 메시지의 _id를 기준으로 
     * 이전의 메시지들을 가져오기 위한 로직
     */
    async getPrevChat(payload) {
        const matchStage = {
            chatRoomId: payload.chatRoomId
        };

        if (payload.cursor && payload.cursor !== 0) {
            matchStage._id = { $lt: payload.cursor };
        }

        const pipeline = this.getAggregationPipeline(matchStage, -1, payload.limit);
        const chats = await chatEntity.aggregate(pipeline);

        return { items: await this.parseChats(chats), count: chats.length };
    }

    // {chatRoomId, cursor, limit}
    /**
     * 웹소켓이 끊기고 다시 연결되었을 때 
     * 마지막으로 받은 메시지의 _id를 기준으로 
     * 이후의 메시지들을 가져오기 위한 로직
     */
    async syncChat(payload) {
        const matchStage = {
            chatRoomId: payload.chatRoomId,
            _id: { $gt: payload.cursor }
        };

        const pipeline = this.getAggregationPipeline(matchStage, 1, payload.limit);
        const chats = await chatEntity.aggregate(pipeline);

        return { items: await this.parseChats(chats), count: chats.length };
    }

    parseChat = async (chat) => {

        let avatarUrl = chat.member && chat.member.avatarUrl ? chat.member.avatarUrl : chat.avatarUrl;

        if (avatarUrl) {
            avatarUrl = await getPresignedUrl(avatarUrl);
        }

        // S3 Presigned URL 생성 실패 또는 유효하지 않은 키(string 등)일 경우 기본 이미지 사용
        if (!avatarUrl) {
            // S3에 default.png가 있다고 가정하고 다시 시도하거나, 그냥 클라이언트가 처리하게 null로 둘 수도 있음.
            // 여기서는 유저 요청 맥락상 "이미지가 안 뜬다"를 해결해야 하므로 유효한 URL이 없으면 null보다는 기본값이나 처리가 필요.
            // 하지만 default.png도 S3에 있다면 signed url이 필요할 수 있음.
            // 일단 null일 경우 null로 반환하여 프론트에서 기본 이미지를 띄우도록 유도하거나, 
            // "default.png" 문자열을 반환하여 프론트가 assets에서 찾도록 할 수 있음.
            // 기존 코드 라인 15에서 "default.png"를 할당하는 로직이 있으므로, 여기서도 null이면 "default.png"로 복구하는게 안전.
            avatarUrl = "default.png";
        }


        return {
            id: chat._id,
            memberId: chat.memberId,
            avatarUrl: avatarUrl,
            senderNickname: chat.member ? chat.member.nickname : "Unknown", // 안전한 접근
            chatRoomId: chat.chatRoomId,
            content: chat.content,
            createdAt: chat.createdAt,
        }
    }

    parseChats = async (chats) => {
        return await Promise.all(chats.map(this.parseChat));
    }
}