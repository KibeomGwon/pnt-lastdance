import { Model, DataTypes } from "sequelize";

export default class ChatRoomBan extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                id: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    autoIncrement: true,
                },
                chatRoomId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                memberId: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                bannedBy: {
                    type: DataTypes.BIGINT,
                    allowNull: false,
                },
                bannedAt: {
                    type: DataTypes.DATE,
                    allowNull: false,
                },
                bannedUntil: {
                    type: DataTypes.DATE,
                    allowNull: false,
                },
                reason: {
                    type: DataTypes.STRING(255),
                    allowNull: true,
                },
            },
            {
                sequelize,
                timestamps: false, // Spring entity didn't show BaseEntity inheritance, but has bannedAt. Wait, Spring used 'bannedAt' explicitly.
                underscored: true,
                modelName: 'ChatRoomBan',
                tableName: 'chat_room_ban',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
                indexes: [
                    {
                        name: 'uq_chat_room_ban_room_member',
                        unique: true,
                        fields: ['chat_room_id', 'member_id'],
                    },
                    {
                        name: 'idx_chat_room_ban_lookup',
                        fields: ['chat_room_id', 'member_id', 'banned_until'],
                    }
                ]
            }
        );
    }

    static associate(db) {
        db.ChatRoomBan.belongsTo(db.ChatRoom, {
            foreignKey: 'chatRoomId',
            targetKey: 'id',
            as: 'chatRoom'
        });
        db.ChatRoomBan.belongsTo(db.Member, {
            foreignKey: 'memberId',
            targetKey: 'id',
            as: 'bannedMember'
        });
        db.ChatRoomBan.belongsTo(db.Member, {
            foreignKey: 'bannedBy',
            targetKey: 'id',
            as: 'adminMember'
        });
    }
}
