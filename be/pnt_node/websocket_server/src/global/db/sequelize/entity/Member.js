import { Model, DataTypes } from "sequelize";

export default class Member extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                // 1. ID 
                id: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    autoIncrement: true,
                },

                // 2. Login ID 
                loginId: {
                    type: DataTypes.STRING(20),
                    allowNull: false,
                    unique: true,
                },

                // 3. Password
                password: {
                    type: DataTypes.STRING,
                    allowNull: false,
                },

                // 4. Email
                email: {
                    type: DataTypes.STRING(50),
                    allowNull: true,
                    unique: true,
                },

                // 5. Birth (LocalDate -> DATEONLY)
                birth: {
                    type: DataTypes.DATEONLY,
                    allowNull: true,
                },

                // 6. Role (Enum -> ENUM or STRING)
                role: {
                    type: DataTypes.STRING,
                    allowNull: true,
                },

                // 7. isDeleted (boolean)
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
                modelName: 'Member',
                tableName: 'member',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }
    static associate(db) {
        // Member(1) : MemberProfile(1)
        db.Member.hasOne(db.MemberProfile, {
            foreignKey: 'memberId',
            sourceKey: 'id'
        });

        // Member(1) : MemberChatRoom(N)
        db.Member.hasMany(db.MemberChatRoom, {
            foreignKey: 'memberId',
            sourceKey: 'id'
        });
    }
}