import { Model, DataTypes } from "sequelize";

export default class GameNews extends Model {
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
                title: {
                    type: DataTypes.STRING,
                    allowNull: true,
                },
                contents: {
                    type: DataTypes.STRING,
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
                modelName: 'GameNews',
                tableName: 'game_news',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.GameNews.belongsTo(db.Game, {
            foreignKey: 'gameId',
            targetKey: 'id'
        });
    }
}
