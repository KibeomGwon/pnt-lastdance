import mq from "../../../global/mq/MessagingQueue.js";
import { MQConfig } from "../../../global/mq/MQConfig.js";
import { ChatPushService } from "../application/ChatPushService.js";
import { sendChatPush } from "../../../global/fcm.js";

class ChatPushMq {
    channel;
    chatPushService;

    create = async () => {
        try {
            this.channel = await mq.createChannel(MQConfig.MQ_ALARM);
            // 안전 장치 추가: 큐가 없으면 생성하고, 있으면 넘어감
            await this.channel.assertQueue(MQConfig.MQ_ALARM, { durable: true });
            this.chatPushService = new ChatPushService();
            return this;
        } catch (err) {
            console.error(err);
        }
    }

    consume = async () => {
        this.channel.consume(MQConfig.MQ_ALARM, async (msg) => {
            try {
                const data = JSON.parse(msg.content.toString());

                const memberIds = await this.chatPushService.getNotConnectedInRoom(data);

                const tokens = await this.chatPushService.getUserTokens(memberIds);

                const chatRoom = await this.chatPushService.findChatRoomById(data.chatRoomId);

                data.title = chatRoom.title;

                if (!tokens || tokens.length === 0) {
                    this.channel.ack(msg);
                    return;
                }

                await sendChatPush(tokens, data);
                this.channel.ack(msg);

            } catch (err) {
                console.error(err);
                this.channel.nack(msg, false, true);
            }
        });
    }
}

const chatPushMq = new ChatPushMq();

export default chatPushMq;