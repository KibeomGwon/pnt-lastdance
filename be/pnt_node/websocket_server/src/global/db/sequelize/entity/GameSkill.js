import { Model, DataTypes } from "sequelize";

export default class GameSkill extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                id: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    autoIncrement: true,
                },
                memberId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                gameId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                isUsed: {
                    type: DataTypes.BOOLEAN,
                    allowNull: true,
                },
                usedAt: {
                    type: DataTypes.DATE,
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
                modelName: 'GameSkill',
                tableName: 'game_skill',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
                indexes: [
                    {
                        unique: true,
                        fields: ['game_id', 'member_id'],
                    }
                ]
            }
        );
    }

    static associate(db) {
        db.GameSkill.belongsTo(db.Game, {
            foreignKey: 'gameId',
            targetKey: 'id'
        });
        db.GameSkill.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id'
        });
    }
}
