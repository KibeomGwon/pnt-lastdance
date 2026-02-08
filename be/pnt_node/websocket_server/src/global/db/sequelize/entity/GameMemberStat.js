import { Model, DataTypes } from "sequelize";

export default class GameMemberStat extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                id: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    autoIncrement: true,
                },
                gameMemberId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                    unique: true,
                },
                position: {
                    type: DataTypes.STRING(10),
                    allowNull: true,
                },
                walk: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                    defaultValue: 0,
                },
                arrestCount: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                    defaultValue: 0,
                },
                longestSurvived: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                    defaultValue: 0,
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
                modelName: 'GameMemberStat',
                tableName: 'game_member_stat',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.GameMemberStat.belongsTo(db.GameMember, {
            foreignKey: 'gameMemberId',
            targetKey: 'id',
            as: 'gameMember'
        });
    }
}
