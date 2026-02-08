import { Sequelize } from "sequelize";
import Member from "./entity/Member.js";
import ChatRoom from "./entity/ChatRoom.js";
import MemberChatRoom from "./entity/MemberChatRoom.js";
import FcmToken from "./entity/FcmToken.js";
import dotenv from "dotenv";

if (process.env.NODE_ENV !== 'production') {
    dotenv.config();
}

class SequelizeDB {
    create = async () => {
        let retries = 5;
        while (retries) {
            try {
                const sequelize = new Sequelize(process.env.POSTGRES_DB_NAME, process.env.POSTGRES_USER, process.env.POSTGRES_PASSWORD, {
                    host: process.env.POSTGRES_HOST,
                    dialect: "postgres",
                    port: process.env.POSTGRES_PORT,
                    logging: false
                });

                // 연결 테스트
                await sequelize.authenticate();

                Member.initiate(sequelize);
                ChatRoom.initiate(sequelize);
                MemberChatRoom.initiate(sequelize);
                FcmToken.initiate(sequelize);

                Member.associate(sequelize.models);
                ChatRoom.associate(sequelize.models);
                MemberChatRoom.associate(sequelize.models);
                FcmToken.associate(sequelize.models);

                await this.sync(sequelize);
                return; // 성공 시 종료
            } catch (error) {
                console.error(`Database connection failed. Retrying... (${5 - retries + 1}/5)`, error);
                retries -= 1;
                if (!retries) throw error;
                await new Promise(res => setTimeout(res, 2000));
            }
        }
    }

    sync = async (sequelize) => {
        await sequelize.sync({ force: false }).then(() => {
            console.log("Database synchronized successfully.");
        }).catch((error) => {
            console.error("Failed to synchronize database:", error);
        });
    }
}

const db = new SequelizeDB();

export default db;
