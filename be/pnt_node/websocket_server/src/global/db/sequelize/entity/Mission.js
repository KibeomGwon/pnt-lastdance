import { Model, DataTypes } from "sequelize";

export default class Mission extends Model {
    static initiate(sequelize) {
        return super.init(
            {
                id: {
                    type: DataTypes.BIGINT,
                    primaryKey: true,
                    autoIncrement: true,
                },
                title: {
                    type: DataTypes.STRING(20),
                    allowNull: true,
                },
                description: {
                    type: DataTypes.STRING(255),
                    allowNull: true,
                },
                keyword: {
                    type: DataTypes.STRING(20),
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
                modelName: 'Mission',
                tableName: 'mission',
                paranoid: false,
                charset: 'utf8mb4',
                collate: 'utf8mb4_general_ci',
            }
        );
    }
    static associate(db) { }
}
