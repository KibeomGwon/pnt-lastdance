import { Model, DataTypes } from "sequelize";

export default class GameMission extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                id: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    autoIncrement: true,
                },
                gameId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                missionId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                completedBy: {
                    type: DataTypes.BIGINT,
                    allowNull: true,
                },
                completedAt: {
                    type: DataTypes.DATE,
                    allowNull: true,
                },
                status: {
                    type: DataTypes.STRING, /* ENUM in Spring, STRING here */
                    allowNull: true,
                },
                isDeleted: {
                    type: DataTypes.BOOLEAN,
                    allowNull: false,
                    defaultValue: false,
                },
            },
            {
                sequelize,
                timestamps: true,
                underscored: true,
                modelName: 'GameMission',
                tableName: 'game_mission',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.GameMission.belongsTo(db.Game, {
            foreignKey: 'gameId',
            targetKey: 'id'
        });
        db.GameMission.belongsTo(db.Mission, {
            foreignKey: 'missionId',
            targetKey: 'id'
        });
        db.GameMission.belongsTo(db.Member, {
            foreignKey: 'completedBy',
            targetKey: 'id'
        });
    }
}
