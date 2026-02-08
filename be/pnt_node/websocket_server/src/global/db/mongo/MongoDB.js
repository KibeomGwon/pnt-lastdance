import mongoose from "mongoose";
import dotenv from "dotenv";

dotenv.config();

const HOST = process.env.MONGO_HOST;
const PASSWORD = process.env.MONGO_PASSWORD;
const DB_NAME = process.env.MONGO_DB_NAME;
const USER = process.env.MONGO_USER;
const PORT = process.env.MONGO_PORT;

const DB_URL = `mongodb://${USER}:${PASSWORD}@${HOST}:${PORT}/${DB_NAME}?authSource=admin`;
class MongoDB {
    async create() {
        mongoose.set('debug', false);

        await mongoose.connect(DB_URL).then(() => {
            console.log("MongoDB is connected!!");
        }).catch((err) => {
            console.error("MongoDB connection failed", err);
        });

        this.setEventListener();
    }

    setEventListener() {
        mongoose.connection.on('error', (error) => {
            console.error('몽고디비 연결 에러', error);
        });

        mongoose.connection.on('disconnected', () => {
            console.error('몽고디비 연결이 끊겼습니다. 연결을 재시도합니다.');
            this.connect(); // 재연결 시도
        });
    }
}

const db = new MongoDB();

export default () => db.create();