import GameMission from "../../../global/db/sequelize/entity/GameMission";

export class GameMissionService {

    findOne = async (id, options = {}) => {
        return await GameMission.findOne({
            where: {
                id: id,
                isDeleted: false
            },
            ...options
        });
    }

    update = async (id, payload, options = {}) => {
        return await GameMission.update(payload, {
            where: {
                id: id,
                isDeleted: false
            },
            ...options
        });
    }
}