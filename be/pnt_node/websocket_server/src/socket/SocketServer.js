import { Server } from "socket.io";
import { createAdapter } from "@socket.io/redis-adapter";
import chatSocketServer from "./chats/server/ChatSocketServer.js";
import gameSocketServer from "./games/server/GameSocketServer.js";
import roomSocketServer from "./rooms/server/RoomSocketServer.js";
import redisDB from "../global/db/redis/RedisDB.js";
import { RedisEvent } from "./utils/RedisEvent.js";

export const socketServer = async (httpServer) => {

    const pubClient = redisDB.getPubClient();
    const subClient = redisDB.getSubClient();

    const io = new Server(httpServer, {
        adapter: createAdapter(pubClient, subClient),
        cors: {
            origin: "*"
        }
    });

    const chatIo = io.of("/chat");
    const gameIo = io.of("/game");
    const roomIo = io.of("/room");

    const redisEvent = new RedisEvent(chatIo, roomIo, gameIo);
    await redisEvent.listen();

    chatSocketServer(chatIo, pubClient);
    gameSocketServer(gameIo, pubClient);
    roomSocketServer(roomIo, pubClient);
} 