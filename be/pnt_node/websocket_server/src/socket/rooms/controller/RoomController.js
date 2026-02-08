import { GameMemberService } from "../../games/application/GameMemberService.js";
import { GameService } from "../../games/application/GameService.js";
import { GameSettingService } from "../../games/application/GameSettingService.js";
import { RedisClient } from "../../utils/client/RedisClient.js";
import axios from "axios";
import { sendError } from "../../../global/util/SocketError.js";
import { resolveInController } from "../../../global/auth/JwtResolver.js";
import { generateMemberAccessToken } from "../../../global/auth/JwtProvider.js";
import { GameSkillService } from "../../games/application/GameSkillService.js";


export class RoomController {
    constructor(io, socket) {
        this.io = io;
        this.socket = socket;
        this.redisClient = new RedisClient();
        this.gameSettingService = new GameSettingService();
        this.gameMemberService = new GameMemberService();
        this.gameService = new GameService();
        this.gameSkillService = new GameSkillService();
    }

    /*
    * 게임 로비 입장 => db 입장 처리는 spring 에서 처리
    * 
    * 내려줄 정보 없음.
    * 
    */

    isActiveRoom = async (roomId) => {
        const room = await this.gameService.getGameById(roomId);

        if (room.status === "IN_GAME") {
            sendError(this.socket, { code: 404, message: "게임중인 방입니다." }, "NotFoundException");
            return false;
        }

        if (room.status === "ENDED") {
            sendError(this.socket, { code: 404, message: "종료 상태인 방입니다." }, "NotFoundException");
            return false;
        }

        return true;
    }

    joinRoom = async (data) => {
        const roomId = parseInt(data.roomId);

        if (!await this.isActiveRoom(roomId)) {
            return;
        }

        this.socket.data.roomId = roomId;
        this.socket.join(roomId);

        this.io.to(roomId).emit("get join room", { roomId });
    }

    updateRoomInfo = async (data) => {
        const roomId = this.socket.data.roomId;

        try {
            const accessToken = generateMemberAccessToken(this.socket.data.memberId, -3);

            const response = await axios.patch(`${process.env.SPRING_BOOT_URL}/rooms/${roomId}/settings`, data, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${accessToken}`
                }
            });
            this.io.to(roomId).emit("get update room info", response.data);
        } catch (error) {
            if (error.response) {
                this.io.to(roomId).emit("get update room info", error.response.data);
            } else {
                throw error;
            }
        }
    }

    updateReady = async (data) => {
        const roomId = this.socket.data.roomId;

        const accessToken = generateMemberAccessToken(this.socket.data.memberId, -3);

        try {
            const response = await axios.patch(`${process.env.SPRING_BOOT_URL}/rooms/${roomId}/ready`, data, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${accessToken}`
                }
            });
            this.io.to(roomId).emit("get update ready", response.data);
        } catch (error) {
            if (error.response) {
                this.io.to(roomId).emit("get update ready", error.response.data);
            } else {
                throw error;
            }
        }
    }

    updatePreferPosition = async (data) => {
        const roomId = this.socket.data.roomId;
        try {
            const accessToken = generateMemberAccessToken(this.socket.data.memberId, -3);

            const response = await axios.post(`${process.env.SPRING_BOOT_URL}/rooms/${roomId}/position`, data, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${accessToken}`
                }
            });

            this.io.to(roomId).emit("get update prefer position", response.data);
        } catch (error) {
            if (error.response) {
                this.io.to(roomId).emit("get update prefer position", error.response.data);
            } else {
                throw error;
            }
        }
    }

    nowReadyInfo = async (data) => {
        const roomId = this.socket.data.roomId;

        const gameMembers = await this.gameMemberService.getGameMembers(parseInt(roomId));
        const readyInfo = gameMembers.map((gameMember) => {
            return {
                memberId: gameMember.memberId,
                ready: gameMember.ready,
                preferPosition: gameMember.preferPosition
            }
        })

        this.io.to(roomId).emit("get now ready info", readyInfo);
    }

    nowRoomInfo = async (data) => {
        const roomId = this.socket.data.roomId;

        const room = await this.gameService.getGameById(roomId);
        const roomSetting = await this.gameSettingService.findGameSetting(roomId);
        const members = await this.gameMemberService.findMembersWithProfileByGameId(roomId);

        this.io.to(roomId).emit("get now room info", { room, roomSetting, members });
    }

    memberKick = async (data) => {
        const roomId = this.socket.data.roomId;

        try {
            const accessToken = generateMemberAccessToken(this.socket.data.memberId, -3);

            const response = await axios.post(`${process.env.SPRING_BOOT_URL}/rooms/${roomId}/members/kick`, data, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${accessToken}`
                }
            });
            this.io.to(roomId).emit("get member kick", response.data);
        } catch (error) {
            if (error.response) {
                this.io.to(roomId).emit("get member kick", error.response.data);
            } else {
                throw error;
            }
        }
    }

    delegateOwner = async (data) => {
        const roomId = this.socket.data.roomId;
        try {
            const accessToken = generateMemberAccessToken(this.socket.data.memberId, -3);

            const response = await axios.post(`${process.env.SPRING_BOOT_URL}/rooms/${roomId}/delegate-host`, {
                "targetMemberId": parseInt(data.targetMemberId)
            }, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${accessToken}`
                }
            });
            this.io.to(roomId).emit("get delegate owner", response.data);
        } catch (error) {
            if (error.response) {
                this.io.to(roomId).emit("get delegate owner", error.response.data);
            } else {
                throw error;
            }
        }
    }

    postUpdateAccessToken = async (data) => {
        const accessToken = data.accessToken;

        resolveInController(accessToken);

        this.redisClient.setAccessToken(this.socket.data.memberId, accessToken);
        this.socket.data.accessToken = accessToken;

        this.socket.emit("get update access token", { "accessToken": data.accessToken, "refreshToken": data.refreshToken });
    }

    updateRoomMap = async (data) => {
        const roomId = this.socket.data.roomId;

        const accessToken = generateMemberAccessToken(this.socket.data.memberId, -3);

        try {
            const response = await axios.post(`${process.env.SPRING_BOOT_URL}/rooms/${roomId}/map`, data, {
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${accessToken}`
                }
            });
            this.io.to(roomId).emit("get update room map", response.data);
        } catch (error) {
            if (error.response) {
                this.io.to(roomId).emit("get update room map", error.response.data);
            } else {
                throw error;
            }
        }
    }

    gameStart = async (data) => {
        const roomId = this.socket.data.roomId;

        const room = await this.gameService.getGameById(roomId);
        const roomSetting = await this.gameSettingService.findGameSetting(roomId);
        const members = await this.gameMemberService.findMembersWithProfileByGameId(roomId);
        const gameSkill = await this.gameSkillService.findGameSkillByGameId(roomId);

        this.io.to(roomId).emit("get game start", { room, roomSetting, chiefMemberId: gameSkill ? gameSkill.memberId : null, members });
    }

    disconnect = async (data) => {
        const roomId = this.socket.data.roomId;

        this.socket.data.isIntentionalExit = true; // 사용자의 요청에 의해서 소켓이 종료되었는지 판별하기 위한 변수
        const memberId = parseInt(this.socket.data.memberId);

        this.io.to(roomId).emit("get user left", { roomId: roomId, memberId: memberId });
        await this.redisClient.deleteAccessToken(memberId);
    }
}