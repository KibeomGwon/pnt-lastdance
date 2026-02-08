import { min } from "moment-timezone";
import { RedisClient } from "../client/RedisClient.js";

export class GpsChannel {
    constructor(gameIo) {
        this.gameIo = gameIo;
        this.redisClient = new RedisClient();
    }

    // 게임 타이머도 담아서 전송
    start = () => {
        setInterval(async () => {
            const rooms = this.gameIo.adapter.rooms;

            for (const [roomId, sockets] of rooms) {
                // 숫자로만 구성된 roomId만 처리
                if (!/^\d+$/.test(roomId)) continue;

                const gameId = parseInt(roomId);

                const startTime = await this.redisClient.getGameTimer(gameId);
                const endTime = await this.redisClient.getGameEnd(gameId);

                if (endTime || !startTime) {
                    continue;
                }

                const locations = await this.redisClient.getAllLocations(gameId);

                if (locations.length === 0) continue;

                const skillUsedAt = await this.redisClient.getSkillUsedAt(gameId);
                const cctvUser = await this.redisClient.getCctvUser(gameId);

                // 게임 시작시간

                const data = {
                    gameId: gameId,
                    sec: 0,
                    skillUsedAt: skillUsedAt ? skillUsedAt : null,
                    cctvThiefId: cctvUser ? cctvUser : null,
                    locations: locations
                }

                if (startTime) {
                    data.sec = Math.round((Date.now() - startTime) / 1000);
                }
                // volatile: 클라이언트가 연결을 유지하지 않는 경우에도 데이터를 전송 -> tcp 보장 X
                // local: redis를 거치지 않고, 현재 연결되어있는 소켓에만 데이터를 전송

                this.gameIo.to(roomId).volatile.local.emit("get gps", data);

            }
        }, 1000);
    }
}