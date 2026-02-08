import GameMemberStat from "../../../global/db/sequelize/entity/GameMemberStat.js";
import { literal } from "sequelize";

export class GameMemberStatService {
    findGameMemberStat = async (gameMemberId) => {
        const res = await GameMemberStat.findOne({
            where: {
                gameMemberId: gameMemberId,
                isDeleted: false
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "게임 멤버 스탯을 찾을 수 없습니다.", 404);
        }

        return res;
    }

    updateArrestCount = async (gameMemberId) => {
        const res = await GameMemberStat.update({ arrestCount: literal('arrest_count + 1') }, {
            where: {
                gameMemberId: gameMemberId,
                isDeleted: false
            }
        });

        if (!res) {
            this.makeError("NotFoundException", "게임 멤버 스탯을 찾을 수 없습니다.", 404);
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