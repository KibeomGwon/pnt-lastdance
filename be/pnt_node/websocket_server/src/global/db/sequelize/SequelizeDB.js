import { Sequelize } from "sequelize";
import Member from "./entity/Member.js";
import ChatRoom from "./entity/ChatRoom.js";
import MemberChatRoom from "./entity/MemberChatRoom.js";
import Game from "./entity/Game.js";
import GameMember from "./entity/GameMember.js";
import GameSetting from "./entity/GameSetting.js";
import ChatRoomBan from "./entity/ChatRoomBan.js";
import MemberAuthProvider from "./entity/MemberAuthProvider.js";
import MemberProfile from "./entity/MemberProfile.js";
import MemberStat from "./entity/MemberStat.js";
import GradePolice from "./entity/GradePolice.js";
import GradeThief from "./entity/GradeThief.js";
import MemberStatPolice from "./entity/MemberStatPolice.js";
import MemberStatThief from "./entity/MemberStatThief.js";
import Mission from "./entity/Mission.js";
import GameMission from "./entity/GameMission.js";
import GameNews from "./entity/GameNews.js";
import GameSkill from "./entity/GameSkill.js";
import GameMemberStat from "./entity/GameMemberStat.js";
import dotenv from "dotenv";

dotenv.config();

class SequelizeDB {
    create = async () => {
        const sequelize = new Sequelize(process.env.POSTGRES_DB_NAME, process.env.POSTGRES_USER, process.env.POSTGRES_PASSWORD, {
            host: process.env.POSTGRES_HOST,
            dialect: "postgres",
            port: process.env.POSTGRES_PORT,
            logging: false,
        });

        // Initialize Models
        Member.initiate(sequelize);
        ChatRoom.initiate(sequelize);
        MemberChatRoom.initiate(sequelize);
        Game.initiate(sequelize);
        GameMember.initiate(sequelize);
        GameSetting.initiate(sequelize);
        ChatRoomBan.initiate(sequelize);
        MemberAuthProvider.initiate(sequelize);
        MemberProfile.initiate(sequelize);
        MemberStat.initiate(sequelize);
        GradePolice.initiate(sequelize);
        GradeThief.initiate(sequelize);
        MemberStatPolice.initiate(sequelize);
        MemberStatThief.initiate(sequelize);
        Mission.initiate(sequelize);
        GameMission.initiate(sequelize);
        GameNews.initiate(sequelize);
        GameSkill.initiate(sequelize);
        GameMemberStat.initiate(sequelize);

        // Associate Models
        Member.associate(sequelize.models);
        ChatRoom.associate(sequelize.models);
        MemberChatRoom.associate(sequelize.models);
        Game.associate(sequelize.models);
        GameMember.associate(sequelize.models);
        GameSetting.associate(sequelize.models);
        ChatRoomBan.associate(sequelize.models);
        MemberAuthProvider.associate(sequelize.models);
        MemberProfile.associate(sequelize.models);
        MemberStat.associate(sequelize.models);
        GradePolice.associate(sequelize.models);
        GradeThief.associate(sequelize.models);
        MemberStatPolice.associate(sequelize.models);
        MemberStatThief.associate(sequelize.models);
        Mission.associate(sequelize.models);
        GameMission.associate(sequelize.models);
        GameNews.associate(sequelize.models);
        GameSkill.associate(sequelize.models);
        GameMemberStat.associate(sequelize.models);

        await this.sync(sequelize);

        this.sequelize = sequelize;
        return sequelize;
    }

    getSequelize = () => {
        return this.sequelize;
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
