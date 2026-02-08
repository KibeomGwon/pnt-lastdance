import { Model, DataTypes } from "sequelize";

export default class GameSetting extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                gameId: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    allowNull: false,
                },
                timeLimit: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                playerCount: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                policeCount: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                thiefCount: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                boundaryGeo: {
                    type: DataTypes.GEOMETRY('POLYGON'),
                    allowNull: true,
                },
                prisonLat: {
                    type: DataTypes.DOUBLE,
                    allowNull: true,
                },
                prisonLng: {
                    type: DataTypes.DOUBLE,
                    allowNull: true,
                },
                cctvInterval: {
                    type: DataTypes.INTEGER,
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
                modelName: 'GameSetting',
                tableName: 'game_setting',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.GameSetting.belongsTo(db.Game, {
            foreignKey: 'gameId',
            targetKey: 'id',
            as: 'game'
        });
    }
}
