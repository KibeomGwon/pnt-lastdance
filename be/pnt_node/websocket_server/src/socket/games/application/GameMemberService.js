import GameMember from "../../../global/db/sequelize/entity/GameMember";
import Member from "../../../global/db/sequelize/entity/Member";
import MemberProfile from "../../../global/db/sequelize/entity/MemberProfile";
import { RedisClient } from "../../utils/client/RedisClient.js";
import { getPresignedUrl } from "../../utils/S3Service.js";

export class GameMemberService {
    constructor() {
        this.redisClient = new RedisClient();
    }

    getGameMembers = async (gameId) => {
        const res = await GameMember.findAll({
            where: {
                gameId: gameId,
                isDeleted: false
            },
            include: [
                {
                    model: Member,
                    attributes: ["id"],
                    include: [
                        {
                            model: MemberProfile,
                            attributes: ["nickname", "avatarUrl"]
                        }
                    ]
                }
            ]
        });

        return res;
    }

    findMemberGame = async (gameId, memberId) => {
        const res = await this.redisClient.getLocation(memberId, gameId);

        if (!res) {
            this.makeError("NotFoundException", "유저의 게임 접속 정보를 찾을 수 없습니다.", 404);
        }
        return res;
    }

    findMemberGameInDB = async (memberId, gameId) => {
        const res = await GameMember.findOne({
            where: {
                memberId: memberId,
                gameId: gameId,
                isDeleted: false
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "유저의 게임 접속 정보를 찾을 수 없습니다.", 404);
        }

        return res;
    }

    updateMemberStatus = async (gameId, memberId, status) => {
        const location = await this.redisClient.getLocation(memberId, gameId);

        if (!location) {
            this.makeError("NotFoundException", "유저의 게임 접속 정보를 찾을 수 없습니다.", 404);
        }

        location.status = status;
        await this.redisClient.setLocation(memberId, gameId, location);
        return location;
    }

    findAllByGameId = async (gameId) => {
        const res = await GameMember.findAll({
            where: {
                gameId: gameId,
                isDeleted: false,
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "게임 멤버 정보를 찾을 수 없습니다.", 404);
        }
        return res;
    }

    findMembersWithProfileByGameId = async (gameId) => {
        const res = await GameMember.findAll({
            where: {
                gameId: gameId,
                isDeleted: false
            },
            include: [
                {
                    model: Member,
                    attributes: ["id"],
                    include: [
                        {
                            model: MemberProfile,
                            attributes: ["nickname", "avatarUrl"]
                        }
                    ]
                }
            ]
        });

        if (!res) {
            this.makeError("NotFoundException", "게임 멤버 정보를 찾을 수 없습니다.", 404);
        }

        // Presigned URL 변환 로직 추가
        // 하지만 ES Module 환경이므로 상단 import가 맞음. 
        // 기존 코드 스타일을 유지하며, map을 통해 비동기 처리

        const resWithPresignedUrl = await Promise.all(res.map(async (gameMember) => {
            // Sequelize 인스턴스를 plain object로 변환 (필요시)
            const memberData = gameMember.toJSON();

            if (memberData.Member && memberData.Member.MemberProfile && memberData.Member.MemberProfile.avatarUrl) {
                memberData.Member.MemberProfile.avatarUrl = await getPresignedUrl(memberData.Member.MemberProfile.avatarUrl);
            }
            return memberData;
        }));

        return resWithPresignedUrl;
    }

    updateInGameConnected = async (gameId, memberId, isConnected) => {
        const location = await this.redisClient.getLocation(memberId, gameId);
        if (!location) {
            this.makeError("NotFoundException", "유저의 게임 접속 정보를 찾을 수 없습니다.", 404);
        }

        location.isConnected = isConnected;
        await this.redisClient.setLocation(memberId, gameId, location);

        return true;
    }

    updateThiefStats = async (gameId, memberId, walk, longestSurvived) => {
        const res = await GameMember.update({ walk: walk, longestSurvived: longestSurvived }, {
            where: {
                gameId: gameId,
                memberId: memberId,
                isDeleted: false
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "유저의 게임 접속 정보를 찾을 수 없습니다.", 404);
        }
        return res;
    }

    updatePoliceStats = async (gameId, memberId, arrestCount, walk) => {
        const res = await GameMember.update({ arrestCount: arrestCount, walk: walk }, {
            where: {
                gameId: gameId,
                memberId: memberId,
                isDeleted: false
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "유저의 게임 접속 정보를 찾을 수 없습니다.", 404);
        }
        return res;
    }

    makeError = (message, text, code) => {
        const error = new Error(message);
        error.code = code;
        error.text = text;
        throw error;
    }
}