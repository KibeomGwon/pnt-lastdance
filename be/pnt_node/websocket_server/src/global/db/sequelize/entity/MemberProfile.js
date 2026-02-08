import { Model, DataTypes } from "sequelize";

export default class MemberProfile extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                // member_id is PK and FK
                memberId: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    allowNull: false,
                },
                nickname: {
                    type: DataTypes.STRING(20),
                    allowNull: true,
                },
                avatarUrl: {
                    type: DataTypes.STRING,
                    allowNull: true,
                    defaultValue: "default.png",
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
                modelName: 'MemberProfile',
                tableName: 'member_profile',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }

    static associate(db) {
        db.MemberProfile.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id'
        });
    }
}
