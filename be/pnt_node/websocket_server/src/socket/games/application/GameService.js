import Game from "../../../global/db/sequelize/entity/Game.js"
import GameSetting from "../../../global/db/sequelize/entity/GameSetting.js";
import { GameMemberService } from "./GameMemberService.js";
import { GameMemberStatService } from "./GameMemberStatService.js";
import { GameMemberStatus } from "../../../global/db/sequelize/status/GameMemberStatus.js";
import { GameMemberPosition } from "../../../global/db/sequelize/status/GameMemberPosition.js";
import { GameStatus } from "../../../global/db/sequelize/status/GameStatus.js";
import { RedisClient } from "../../utils/client/RedisClient.js";
import logger from "../../../global/config/logger.js";

export class GameService {
    constructor() {
        this.gameMemberService = new GameMemberService();
        this.gameMemberStatService = new GameMemberStatService();
        this.redisClient = new RedisClient();
    }

    getGameById = async (gameId) => {
        const game = await Game.findOne({
            where: {
                id: gameId,
                isDeleted: false
            }
        });

        if (!game) {
            this.makeError("NotFoundException", "게임을 찾을 수 없습니다.", 404);
        }

        return game;
    }

    findGame = async (gameId, status) => {
        const whereCondition = {
            id: gameId,
            isDeleted: false
        };

        if (status !== undefined && status !== null) {
            whereCondition.status = status;
        }

        const res = await Game.findOne({
            where: whereCondition,
            include: [
                {
                    model: GameSetting,
                    as: 'gameSetting',
                    where: {
                        isDeleted: false
                    }
                }
            ]
        });

        logger.info(`[GameService] findGame: gameId: ${gameId}, status: ${status}, res: ${JSON.stringify(res)}`);

        if (!res) {
            this.makeError("NotFoundException", "게임을 찾을 수 없습니다.", 404);
        }

        return res;
    }

    processArrest = async (gameId, thiefId, policeId) => {
        try {

            const police = await this.gameMemberService.findMemberGameInDB(policeId, gameId);
            // thief 상태 변경 (TRANSFER)
            // police 스탯 업데이트 (체포 횟수 증가)
            await this.gameMemberStatService.updateArrestCount(police.id);

            await this.gameMemberService.updateMemberStatus(gameId, thiefId, GameMemberStatus.TRANSFER);
        
            return true;
        } catch (error) {
            logger.error("processArrest Error", error);
            throw error;
        }
    }

    // 게임 종료 조건 확인 => 모든 도둑이 잡혔을때 종료.
    // 게임 승리팀 상태를 비관적 락으로 처리해야할 수도 있음.
    checkGameHaveToFinish = async (gameId) => {
        const game = await this.redisClient.getGameTimer(gameId);

        if (!game) {
            return false;
        }

        const gameMembers = await this.redisClient.getAllLocations(gameId);

        const thiefMembers = gameMembers
            .filter(member => member.position === GameMemberPosition.THIEF &&
                (!member.status || member.status === GameMemberStatus.FREE) &&
                member.isConnected
            );

        const policeMembers = gameMembers
            .filter(member => member.position === GameMemberPosition.POLICE &&
                member.isConnected
            );

        logger.info(`[GameService] checkGameHaveToFinish: gameId: ${gameId}, thiefMembers: ${thiefMembers.length}, policeMembers: ${policeMembers.length}`);

        let isGameEnd = false;

        if (thiefMembers.length === 0) {
            isGameEnd = GameMemberPosition.POLICE;
        } else if (policeMembers.length === 0) {
            isGameEnd = GameMemberPosition.THIEF;
        }

        return isGameEnd;
    }

    updateGame = async (payload) => {
        try {
            await Game.update(payload, {
                where: {
                    id: payload.gameId,
                    isDeleted: false,
                },
            });
        } catch (error) {
            logger.error("updateGame Error", error);
            throw error;
        }
    }

    // 게임 종료 처리
    endGame = async (gameId, winnerTeam) => {
        try {
            // 게임 상태 변경 (END)
            await Game.update({
                status: GameStatus.ENDED,
                winTeam: winnerTeam,
                endedAt: new Date().toISOString(),
            }, {
                where: {
                    id: gameId,
                },
            });

            return true;
        } catch (error) {
            logger.error("endGame Error", error);
            throw error;
        }
    }

    makeError = (message, text, code) => {
        const error = new Error(message);
        error.code = code;
        error.text = text;
        throw error;
    }
}