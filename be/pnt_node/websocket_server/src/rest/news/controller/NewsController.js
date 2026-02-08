import { Router } from "express";

import { Emitter } from "@socket.io/redis-emitter";
import { RedisClient } from "../../../socket/utils/client/RedisClient";
import logger from "../../../global/config/logger";

const GAME_NAMESPACE = "/game";

export class NewController {
    constructor() {
        this.redisClient = new RedisClient();
        this.router = Router();
        this.emitter = new Emitter(this.redisClient.pubClient);
        this.init();
    }

    init = () => {
        this.router.post("/complete", this.newsComplete);
    }

    getRouter = () => {
        return this.router;
    }

    newsComplete = async (req, res) => {
        try {
            const payload = req.body;
            logger.info(`[NewsController] payload: ${JSON.stringify(payload)}`);
            const { gameId, newsId, success } = payload;

            if (success) {
                logger.info(`[NewsController] emitting get news to ${gameId}`);
                this.emitter.of(GAME_NAMESPACE).to(gameId).emit("get news", { gameId, newsId });
                await this.redisClient.setNews(gameId, newsId);
            } else {
                logger.info(`[NewsController] success is false/missing. payload success: ${success}`);
            }

            res.status(200).json({ message: "News sent successfully" });
        } catch (error) {
            logger.error(`[NewsController] error: ${error}`);
            res.status(error.code || 500).json({ message: error.message });
        }
    }
}   