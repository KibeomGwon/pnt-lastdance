import { Model, DataTypes } from "sequelize";

export default class MemberStat extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                memberId: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    allowNull: false,
                },
                totalGames: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                wins: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                loses: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                policeGame: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                thiefGame: {
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
                modelName: 'MemberStat',
                tableName: 'member_stat',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.MemberStat.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id'
        });
    }
}
