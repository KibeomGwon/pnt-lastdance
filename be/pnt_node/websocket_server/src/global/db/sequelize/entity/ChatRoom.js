import { Model, DataTypes } from "sequelize";

export default class ChatRoom extends Model {
  static initiate(sequelize) {
    return super.init(
      {
        // 1. ID
        id: {
          type: DataTypes.BIGINT,
          primaryKey: true,
          autoIncrement: true,
        },

        // 2. Title
        title: {
          type: DataTypes.STRING,
          allowNull: false,
        },

        // 3. Region Code (Integer)
        regionCode: {
          type: DataTypes.INTEGER,
          allowNull: false,
        },

        // 4. Description (TEXT)
        description: {
          type: DataTypes.TEXT,
          allowNull: true,
        },

        // 5. Max Members
        maxMembers: {
          type: DataTypes.INTEGER,
          allowNull: false,
        },

        // 6. Current Members
        currentMembers: {
          type: DataTypes.INTEGER,
          allowNull: false,
        },

        // 7. Owner ID
        ownerId: {
          type: DataTypes.BIGINT,
          allowNull: false,
        },

        // 8. isDeleted (boolean)
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
        modelName: 'ChatRoom',
        tableName: 'chat_room',
        paranoid: false,
        charset: 'utf8mb4',
        collate: 'utf8mb4_general_ci',
      }
    );
  }

  static associate(db) {
    db.ChatRoom.hasMany(db.MemberChatRoom, {
      foreignKey: 'chatRoomId',
      sourceKey: 'id'
    });
  }
}