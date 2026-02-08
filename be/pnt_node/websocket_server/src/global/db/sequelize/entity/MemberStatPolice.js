import { Model, DataTypes } from "sequelize";

export default class MemberStatPolice extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                memberId: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    allowNull: false,
                },
                gradePoliceId: {
                    type: DataTypes.BIGINT,
                    allowNull: true,
                },
                totalArrestCount: {
                    type: DataTypes.INTEGER,
                    allowNull: true,
                },
                mostArrestsInGame: {
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
                modelName: 'MemberStatPolice',
                tableName: 'member_stat_police',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.MemberStatPolice.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id'
        });
        db.MemberStatPolice.belongsTo(db.GradePolice, {
            foreignKey: 'gradePoliceId', // underscored: true -> grade_police_id in DB
            targetKey: 'id',
            as: 'grade'
        });
    }
}
