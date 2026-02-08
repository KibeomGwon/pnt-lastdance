import redisDB from "../../global/db/redis/RedisDB.js";
import expiredChannel from "./channels/ExpiredChannel.js";
import { GpsChannel } from "./channels/GpsChannel.js";
import { ChatRoomService } from "../chats/application/ChatRoomService.js";
import { WebSocketReconnect } from "./reconnect/ReconnectCacheExpired.js";

export class RedisEvent {
    constructor(chatIo, readyRoomIo, gameIo) {
        this.pubClient = redisDB.getPubClient();
        this.subClient = redisDB.getSubClient();
        this.chatIo = chatIo;
        this.readyRoomIo = readyRoomIo;
        this.gameIo = gameIo;
        this.chatRoomService = new ChatRoomService();
        this.gpsChannel = new GpsChannel(this.gameIo);
        this.webSocketReconnect = new WebSocketReconnect(chatIo, readyRoomIo, gameIo);
    }

    listen = async () => {
        // 1. GPS Channel
        this.gpsChannel.start();

        // 2. 만료 이벤트 설정
        await this.pubClient.config("SET", "notify-keyspace-events", "Ex");
        await this.webSocketReconnect.listen();

        // 3. 만료 이벤트 리스너
        // __keyevent@0__:expired 채널을 구독 (0번 DB 기준)
        await this.subClient.subscribe("__keyevent@0__:expired");

        this.subClient.on("message", async (channel, message) => {
            if (channel === "__keyevent@0__:expired") {
                expiredChannel(message, this.pubClient, this.chatIo, this.readyRoomIo, this.gameIo);
            }
        });
    }
}