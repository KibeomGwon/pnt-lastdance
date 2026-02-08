import { Model, DataTypes } from "sequelize";

export default class GameMember extends Model {
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
                memberId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                serialCode: {
                    type: DataTypes.STRING,
                    allowNull: true,
                },
                givenPosition: {
                    type: DataTypes.STRING(10),
                    allowNull: true,
                },
                ready: {
                    type: DataTypes.BOOLEAN,
                    allowNull: true,
                },
                status: {
                    type: DataTypes.STRING(10),
                    allowNull: true,
                },
                preferPosition: {
                    type: DataTypes.STRING(10),
                    allowNull: true,
                },
                inGameConnected: {
                    type: DataTypes.BOOLEAN,
                    allowNull: true,
                    defaultValue: false,
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
                modelName: 'GameMember',
                tableName: 'game_member',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
                indexes: [
                    {
                        unique: true,
                        fields: ['game_id', 'member_id'],
                    },
                ]
            }
        );
    }

    static associate(db) {
        db.GameMember.belongsTo(db.Game, {
            foreignKey: 'gameId',
            targetKey: 'id'
        });
        db.GameMember.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id'
        });
    }
}
