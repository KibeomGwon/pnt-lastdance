import GameSetting from "../../../global/db/sequelize/entity/GameSetting.js"
import { Op, fn, col, where } from "sequelize";

export class GameSettingService {

    findGameSetting = async (gameId) => {
        return await GameSetting.findOne({
            where: { gameId: gameId, isDeleted: false }
        });
    }

    checkUserInBoundary = async (gameId, longitude, latitude) => {
        // 1. 입력받은 좌표를 POINT 객체로 생성
        const userLocation = fn('ST_GeomFromText', `POINT(${longitude} ${latitude})`, 4326);

        const setting = await GameSetting.findOne({
            where: {
                gameId: gameId,
                [Op.and]: [
                    // 2. ST_Contains를 사용하여 폴리곤 안에 포인트가 있는지 확인
                    // ST_Buffer를 사용하여 boundaryGeo를 약 5미터(0.000045도)만큼 확장합니다.
                    where(
                        fn('ST_Contains',
                            fn('ST_Buffer', col('boundary_geo'), 0.000045), // 5m 오차 보정
                            userLocation
                        ),
                        true
                    )
                ]
            }
        });

        return setting !== null;
    }

    checkUserInPrison = async (gameId, longitude, latitude) => {
        const gameSetting = await GameSetting.findOne({
            attributes: [
                [
                    fn('ST_DistanceSphere',
                        col('prison_location'),
                        fn('ST_SetSRID', fn('ST_MakePoint', longitude, latitude), 4326)
                    ),
                    'distance'
                ]
            ],
            where: { gameId },
            raw: true
        });

        // 감옥 반경 10m + 오차범위 5m = 15m 이내에 있으면 true
        return gameSetting && gameSetting.distance <= 15;
    }
}