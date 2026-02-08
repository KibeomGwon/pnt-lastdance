import sequelizeDB from "./sequelize/SequelizeDB.js";

export const database = async () => {
    await sequelizeDB.create();
}