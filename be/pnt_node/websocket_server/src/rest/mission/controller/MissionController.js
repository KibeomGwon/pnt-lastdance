import { Transaction } from "sequelize";
import db from "../../../global/db/sequelize/SequelizeDB.js";
import { Router } from "express";
import { Emitter } from "@socket.io/redis-emitter";
import { GameMissionService } from "../application/GameMissionService";
import { MissionStatus } from "../../../global/db/sequelize/status/MissionStatus";
import { RedisClient } from "../../../socket/utils/client/RedisClient";
import logger from "../../../global/config/logger.js";


const GAME_NAMESPACE = "/game";

export class MissionController {
    constructor() {
        this.router = new Router();
        this.redisClient = new RedisClient();
        this.emitter = new Emitter(this.redisClient.pubClient);
        this.init();
        this.gameMissionService = new GameMissionService();
    }

    init = () => {
        this.router.post("/complete", this.missionComplete);
    }

    getRouter = () => {
        return this.router;
    }

    missionComplete = async (req, res) => {
        try {
            const sequelize = db.getSequelize();
            await sequelize.transaction(async (t) => {
                const payload = req.body;
                const { gameId, missionId, memberId, success } = payload;
                const gameMission = await this.gameMissionService.findOne(missionId, {
                    transaction: t,
                    lock: Transaction.LOCK.UPDATE
                });
                logger.info(`missionComplete request ${JSON.stringify(payload)}`);

                const gameMember = await this.redisClient.getLocation(memberId, gameId);

                if (!gameMember) {
                    throw { code: 404, message: "Member not found" };
                }
                if (!gameMission) {
                    throw { code: 404, message: "GameMission not found" };
                }

                // 이미 성공했는지 여부 확인
                if (gameMission.status === MissionStatus.SUCCESS) {
                    const resData = {
                        gameId: gameId,
                        missionId: missionId,
                        thiefId: gameMission.completedBy,
                        success: false,
                        reason: "ALREADY_COMPLETED",
                        completedAt: gameMission.completedAt // 기존 완료 시간 사용
                    }
                    this.emitter.of(GAME_NAMESPACE).to(gameId).emit("get mission result", resData);
                    res.status(200).json({ message: "GameMission already completed" });
                    return;
                }

                // 미션 업데이트 시작
                const completedAt = new Date().toISOString();

                // 성공시에만 업데이트
                if (success) {
                    await this.gameMissionService.update(missionId, {
                        completedBy: memberId,
                        completedAt: completedAt,
                        status: MissionStatus.SUCCESS
                    }, {
                        transaction: t
                    });

                    await this.redisClient.setMission(gameId, memberId, missionId);

                    gameMember.missionCompleted = true;
                    await this.redisClient.setLocation(memberId, gameId, gameMember);
                    logger.info(`missionCompleted ${JSON.stringify({ gameId, missionId, memberId, success })}`);
                }

                res.status(200).json({ data: gameMember });

                const resData = {
                    gameId: gameId,
                    missionId: missionId,
                    thiefId: memberId,
                    success: success,
                    reason: "",
                    completedAt: completedAt
                }

                if (!success) {
                    resData.reason = "NOT_MATCHED";
                }

                this.emitter.of(GAME_NAMESPACE).to(gameId).emit("get mission result", resData);
            });
        } catch (error) {
            logger.error(`missionComplete error ${JSON.stringify(error)}`);
            if (!res.headersSent) {
                res.status(error.code || 500).json({ message: error.message });
            }
        }
    }
}