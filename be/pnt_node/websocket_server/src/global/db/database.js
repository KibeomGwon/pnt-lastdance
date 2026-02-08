import connectMongo from "./mongo/MongoDB.js";
import sequelizeDB from "./sequelize/SequelizeDB.js";
import redisDB from "./redis/RedisDB.js";

export const database = async () => {
    await connectMongo();
    await sequelizeDB.create();
    redisDB;
}

export const sequelize = sequelizeDB;