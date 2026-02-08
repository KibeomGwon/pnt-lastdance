import { Model, DataTypes } from "sequelize";

export default class Game extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                id: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    autoIncrement: true,
                },
                hostMemberId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                startTime: {
                    type: DataTypes.DATE,
                    allowNull: true,
                },
                endTime: {
                    type: DataTypes.DATE,
                    allowNull: true,
                },
                status: {
                    type: DataTypes.STRING(20),
                    allowNull: true,
                },
                winTeam: {
                    type: DataTypes.STRING(20),
                    allowNull: true,
                },
                roomCode: {
                    type: DataTypes.STRING(10),
                    allowNull: true,
                },
                isDeleted: {
                    type: DataTypes.BOOLEAN,
                    allowNull: false,
                    defaultValue: false,
                },
                caughtCount: {
                    type: DataTypes.INTEGER,
                    allowNull: false,
                    defaultValue: 0,
                },
            },
            {
                sequelize,
                timestamps: true,
                underscored: true,
                modelName: 'Game',
                tableName: 'game',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.Game.belongsTo(db.Member, {
            foreignKey: 'hostMemberId',
            targetKey: 'id',
            as: 'host'
        });
        db.Game.hasMany(db.GameMember, {
            foreignKey: 'gameId',
            sourceKey: 'id'
        });
        db.Game.hasOne(db.GameSetting, {
            foreignKey: 'gameId',
            sourceKey: 'id',
            as: 'gameSetting'
        });
    }
}
