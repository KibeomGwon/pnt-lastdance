import dotenv from "dotenv";

if (process.env.NODE_ENV !== 'production') {
    dotenv.config();
}

const HOST = process.env.MQ_HOST;
const PORT = process.env.MQ_PORT;
const USER = process.env.MQ_USER;
const PASSWORD = process.env.MQ_PASSWORD;

const URL = `amqp://${USER}:${PASSWORD}@${HOST}:${PORT}`;

export const MQConfig = {
    URL: URL,
    MQ_IMAGE: process.env.MQ_IMAGE,
    MQ_ALARM: process.env.MQ_ALARM,
    MQ_NEWS: process.env.MQ_NEWS,
    EXCHANGE_NAME: process.env.EXCHANGE_NAME
};