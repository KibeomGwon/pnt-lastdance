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
          allowNull: true,        
        },

        // 3. Sido (length = 20)
        sido: {
          type: DataTypes.STRING(20),
          allowNull: true,
        },

        // 4. Description
        description: {
          type: DataTypes.STRING, 
          allowNull: true,
        },
        
        // 5. isDeleted (boolean)
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