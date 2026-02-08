import { Model, DataTypes } from "sequelize";

export default class MemberChatRoom extends Model {
  static initiate(sequelize) {
    return super.init(
      {
        // 1. ID (PK)
        id: {
          type: DataTypes.BIGINT,
          primaryKey: true,
          autoIncrement: true,
        },

        // 2. Member FK 
        memberId: {
          type: DataTypes.BIGINT,
          allowNull: false, 
        },

        // 3. ChatRoom FK 
        chatRoomId: {
          type: DataTypes.BIGINT,
          allowNull: false, 
        },

        isConnected: {
          type: DataTypes.BOOLEAN,
          allowNull: false,
          defaultValue: false,
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
        modelName: 'MemberChatRoom',
        tableName: 'member_chat_room',
        paranoid: false,
        charset: 'utf8mb4',
        collate: 'utf8mb4_general_ci',
        
        indexes: [
          {
            unique: true, 
            fields: ['member_id', 'chat_room_id'], 
          },
        ],
      }
    );
  }

  static associate(db) {
    // MemberChatRoom -> Member (N:1)
    db.MemberChatRoom.belongsTo(db.Member, { 
      foreignKey: 'memberId', 
      targetKey: 'id',
      onUpdate: 'CASCADE'
    });

    // MemberChatRoom -> ChatRoom (N:1)
    db.MemberChatRoom.belongsTo(db.ChatRoom, { 
      foreignKey: 'chatRoomId', 
      targetKey: 'id',
      onUpdate: 'CASCADE'
    });
  }
}