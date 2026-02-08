import { Model, DataTypes } from "sequelize";

export default class MemberAuthProvider extends Model {
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
                provider: {
                    type: DataTypes.STRING(10),
                    allowNull: false,
                },
                providerUserKey: {
                    type: DataTypes.STRING,
                    allowNull: false,
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
                modelName: 'MemberAuthProvider',
                tableName: 'member_auth_providers', // Matches Spring Table name
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
                indexes: [
                    {
                        unique: true,
                        fields: ['provider', 'provider_user_key'],
                    }
                ]
            }
        );
    }

    static associate(db) {
        db.MemberAuthProvider.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id'
        });
    }
}
