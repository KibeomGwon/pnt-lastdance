import amqplib from "amqplib";
import dotenv from "dotenv";
import { MQConfig } from "./MQConfig.js";
import logger from "../config/logger.js";

dotenv.config();

const MQ_URL = MQConfig.URL;

class MessagingQueue {
    connection = null;
    channel = null;
    queue = [MQConfig.MQ_ALARM, MQConfig.MQ_MISSION, MQConfig.MQ_NEWS];
    exchangeName = MQConfig.EXCHANGE_NAME;

    create = async () => {
        if (this.connection) {
            logger.info("MQ is already connected.");
            return this;
        }

        this.connection = await amqplib.connect(MQ_URL);
        this.connection.on("error", (err) => {
            logger.error("MQ Connection Error:", err);
        });

        this.channel = await this.connection.createChannel();
        this.channel.on("error", (err) => {
            logger.error("MQ Channel Error:", err);
        });

        await this.createQueue();
        logger.info("MQ is connected!");
        return this;
    }

    createQueue = async () => {
        await this.channel.assertExchange(this.exchangeName, "direct", { durable: true });

        for (let i = 0; i < this.queue.length; i++) {
            await this.channel.assertQueue(this.queue[i], { durable: true });
            await this.channel.bindQueue(this.queue[i], this.exchangeName, this.queue[i]);
        }
    }

    sendMessage = (message, queueKey) => {
        this.channel.publish(this.exchangeName, queueKey, Buffer.from(JSON.stringify(message)), (error) => {
            if (error) {
                logger.error("Failed to send mq message:", error);
            }
        });
    }
}

const mq = new MessagingQueue();

export default mq;