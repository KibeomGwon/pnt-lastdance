import { Redis } from "ioredis";
import dotenv from "dotenv";
import logger from "../../config/logger.js";

dotenv.config();

class RedisDB {
    constructor() {
        this.pubClient = new Redis({
            host: process.env.REDIS_HOST,
            port: process.env.REDIS_PORT
        });

        this.pubClient.on('error', (err) => {
            logger.error('Redis Pub Client Error:', err);
        });

        this.subClient = this.pubClient.duplicate();

        this.subClient.on('error', (err) => {
            logger.error('Redis Sub Client Error:', err);
        });

        logger.info("RedisDB is connected!");
    }

    getPubClient = () => {
        return this.pubClient;
    }

    getSubClient = () => {
        return this.subClient;
    }
}

const redisDB = new RedisDB();

export default redisDB;