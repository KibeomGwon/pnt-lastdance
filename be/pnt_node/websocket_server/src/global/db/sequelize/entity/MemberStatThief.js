import { Model, DataTypes } from "sequelize";

export default class MemberStatThief extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                memberId: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    allowNull: false,
                },
                gradeThiefId: {
                    type: DataTypes.BIGINT,
                    allowNull: true,
                },
                escapeCount: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                totalMissionCount: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                longestSurvivalSec: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                averageSurvivalSec: {
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
                modelName: 'MemberStatThief',
                tableName: 'member_stat_thief',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.MemberStatThief.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id'
        });
        db.MemberStatThief.belongsTo(db.GradeThief, {
            foreignKey: 'gradeThiefId',
            targetKey: 'id',
            as: 'grade'
        });
    }
}
