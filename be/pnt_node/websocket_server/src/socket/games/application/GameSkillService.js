import GameSkill from "../../../global/db/sequelize/entity/GameSkill.js";

export class GameSkillService {
    findGameSkill = async (gameId, memberId) => {
        const res = await GameSkill.findOne({
            where: {
                gameId: gameId,
                memberId: memberId,
                isDeleted: false
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "게임 스킬을 찾을 수 없습니다.", 404);
        }

        return res;
    }

    findGameSkillByGameId = async (gameId) => {
        const res = await GameSkill.findOne({
            where: {
                gameId: gameId,
                isDeleted: false
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "게임 스킬을 찾을 수 없습니다.", 404);
        }

        return res;
    }

    useSkill = async (gameSkillId) => {
        const res = await GameSkill.update({ isUsed: true }, {
            where: {
                id: gameSkillId,
                isDeleted: false,
                isUsed: false
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "게임 스킬을 찾을 수 없습니다.", 404);
        }

        return res;
    }

    makeError = (message, text, code) => {
        const error = new Error(message);
        error.code = code;
        error.text = text;
        throw error;
    }
}
