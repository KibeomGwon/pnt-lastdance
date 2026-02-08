import { Model, DataTypes } from "sequelize";

export default class FcmToken extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                // 1. ID 
                id: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    autoIncrement: true,
                },

                memberId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },

                value: {
                    type: DataTypes.STRING,
                    allowNull: false
                },

                isActive: {
                    type: DataTypes.BOOLEAN,
                    allowNull: false,
                    defaultValue: false
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
                modelName: 'FcmToken',
                tableName: 'fcm_token',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }
    static associate(db) {
        // Member(1) : FcmToken(1)
        db.FcmToken.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id',
            onUpdate: 'CASCADE'
        });
    }
}