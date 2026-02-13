import { GameService } from "../application/GameService.js";
import { GameMemberService } from "../application/GameMemberService.js";
import { sendError } from "../../../global/util/SocketError.js";
import { GameSettingService } from "../application/GameSettingService.js";
import { GameMemberPosition } from "../../../global/db/sequelize/status/GameMemberPosition.js";
import { GameMemberStatus } from "../../../global/db/sequelize/status/GameMemberStatus.js";
import { GameSkillService } from "../application/GameSkillService.js";
import { GameMemberStatService } from "../application/GameMemberStatService.js";
import { RedisClient } from "../../utils/client/RedisClient.js";
import { GameStatus } from "../../../global/db/sequelize/status/GameStatus.js";
import { GameMissionService } from "../application/GameMissionService.js";
import { TurfService } from "../application/TurfService.js";
import { MQConfig } from "../../../global/mq/MQConfig.js";
import { resolveInController } from "../../../global/auth/JwtResolver.js";
import { generateMemberAccessToken, generateToken } from "../../../global/auth/JwtProvider.js";
import axios from "axios";
import logger from "../../../global/config/logger.js";


export class GameController {
    constructor(io, socket, mq) {
        this.io = io;
        this.socket = socket;
        this.redisClient = new RedisClient();
        this.gameService = new GameService();
        this.gameMemberService = new GameMemberService();
        this.gameSettingService = new GameSettingService();
        this.gameSkillService = new GameSkillService();
        this.gameMemberStatService = new GameMemberStatService();
        this.gameMissionService = new GameMissionService();
        this.turfService = new TurfService();
        this.mq = mq;
    }

    /**
     * 게임 방 접속
     * {
     *  'gameId': 1
     * }
     */

    isActiveRoom = async (gameId) => {
        try {
            const integerGameId = parseInt(gameId);
            const game = await this.gameService.findGame(integerGameId);

            if (game.status === GameStatus.WAITING) {
                return false;
            }

            return true;
        } catch (error) {
            return false;
        }
    }

    /**
     * 게임 방 접속
     * {
     *  'gameId': 1
     * }
     */
    joinRoom = async (payload) => {
        const gameId = parseInt(payload.gameId);
        const memberId = parseInt(this.socket.data.memberId);

        logger.info(`[GameController] joinRoom: gameId: ${gameId}, memberId: ${memberId}`);

        const game = await this.gameService.findGame(gameId, GameStatus.IN_GAME);

        if (await this.redisClient.getGameEnd(gameId)) {
            this.makeError("GameEndException", "게임이 종료되었습니다.", 400);
        }

        const location = await this.redisClient.getLocation(memberId);
        if (!location) {
            const memberGame = await this.gameMemberService.findMemberGameInDB(memberId, gameId);
            if (!memberGame) {
                this.makeError("NotFoundException", "게임에 참여하지 않았습니다.", 404);
            }
            const locationData = {
                lat: 0,
                lng: 0,
                memberId: this.socket.data.memberId, // 클라이언트 편의를 위해 포함
                gameId: payload.gameId,
                walk: 0,
                longestSurvived: 0,
                position: memberGame.givenPosition,
                status: null,
                penalty: 0,
                missionCompleted: false,
                isConnected: true,
                timestamp: new Date().toISOString() // 중요: 갱신 시간 기록
            }
            const token = generateMemberAccessToken(memberId, game.gameSetting.timeLimit);
            await this.redisClient.setAccessToken(memberId, token, game.gameSetting.timeLimit);

            await this.redisClient.setLocation(memberId, gameId, locationData);
        }
        await this.gameMemberService.updateInGameConnected(gameId, memberId, true);
        const locations = await this.redisClient.getAllLocations(gameId);

        this.socket.join(gameId);
        this.socket.data.gameId = gameId;

        const data = {
            message: "joined room",
            gameId: payload.gameId,
            memberId: memberId,
            connectedMembers: locations.length,
        }

        await this.redisClient.setStartedCount(gameId, memberId);
        this.io.to(gameId).emit("get join room", data);

        // 게임 시작 시간 db에 저장
        if (!await this.redisClient.getGameTimer(gameId)) {
            await this.startGame();
        }
    }

    /**
     * redis에 게임 설정을 저장
     * redis에 게임 타이머를 저장
     * 게임 시작 시간을 db에 저장
     * 
     * 시작 이벤트 emit.
     * 
     * 현재 시간으로 부터 5초 뒤에 시작.
     */


    /**
     * GPS 위치 정보
     * 도둑의 탈옥 로직
     * 도둑의 이송 로직
     * 경계선 벗어남
     *  - 패널티 부여 및 초과시 체포
     * 
     * redis에 게임 설정을 저장
     * redis에 게임 타이머를 저장
     * 게임 시작 시간을 db에 저장
     * 
     * 시작 이벤트 emit.
     * 
     * 현재 시간으로 부터 5초 뒤에 시작.
     */
    startGame = async () => {
        const gameId = parseInt(this.socket.data.gameId);

        const gameTimer = await this.redisClient.getGameTimer(gameId);

        if (gameTimer) {
            return;
        }

        const gameSetting = await this.gameSettingService.findGameSetting(gameId);
        const startedCount = await this.redisClient.getStartedCount(gameId);

        if (startedCount < gameSetting.policeCount + gameSetting.thiefCount) {
            return;
        }

        const isMine = await this.redisClient.setGameSettingLock(gameId, 60);

        if (!isMine) {
            return;
        }

        await this.redisClient.setGameSetting(gameId, gameSetting);
        logger.info("will start game", gameId);
        this.io.to(gameId).emit("get will start game", {
            message: "start game",
            gameId: gameId,
            willStartAt: new Date(Date.now() + 5000).toISOString(),
        });

        if (gameSetting && gameSetting.cctvInterval > 0) {
            setTimeout(async () => {
                // cctv 작동
                await this.redisClient.setCctvTimer(gameId, gameSetting.cctvInterval);
            }, 4000);
        }

        setTimeout(async () => {
            await this.redisClient.setGameTimer(gameId, gameSetting.timeLimit);
            await this.redisClient.setGameToken(gameId, generateToken(gameId, gameSetting.timeLimit), gameSetting.timeLimit);

            await this.gameService.updateGame({ gameId: gameId, startTime: new Date().toISOString(), status: GameStatus.IN_GAME });

            logger.info("game started", gameId);
            this.io.to(gameId).emit("get start game", {
                message: "start game",
                gameId: gameId,
                startTime: new Date().toISOString(),
            });
        }, 5000);
    }


    /**
     * GPS 위치 정보
     * 도둑의 탈옥 로직
     * 도둑의 이송 로직
     * 경계선 벗어남
     *  - 패널티 부여 및 초과시 체포
     * 
     * {
     *   "gameId": 10,
     *   "lat": 35.0,
     *   "lng": 129.0,
     *   "walk: 1 (걸음수),
     *   "longestSurvived" : 1 (초단위)
     * }
     */
    postGps = async (payload) => {
        if (!payload || !payload.lat || !payload.lng) {
            sendError(this.socket, { code: 400, message: "Invalid payload: lat and lng are required" }, "GameError");
            return;
        }

        const gameId = parseInt(this.socket.data.gameId);
        const gameTimer = await this.redisClient.getGameTimer(gameId);
        if (!gameTimer) {
            sendError(this.socket, { code: 400, message: "Game is not started or is finished" }, "GameError");
            return;
        }

        const { lat, lng, walk, longestSurvived } = payload;


        const memberId = this.socket.data.memberId; // 미들웨어에서 가져온 ID
        const gameMember = await this.gameMemberService.findMemberGame(gameId, memberId);
        const position = gameMember.position;
        const status = gameMember.status;
        const penalty = await this.redisClient.getPenalty(memberId, gameId) || 0;
        const missionCompleted = gameMember.missionCompleted;

        const locationData = {
            lat,
            lng,
            memberId, // 클라이언트 편의를 위해 포함
            gameId,
            walk,
            longestSurvived,
            position,
            status,
            penalty,
            missionCompleted,
            isConnected: true,
            timestamp: new Date().toISOString() // 중요: 갱신 시간 기록
        };

        if (position === GameMemberPosition.THIEF && status === GameMemberStatus.FREE) {
            locationData.longestSurvived++;
        }

        // Hash에 저장 (이미 있으면 덮어쓰기됨 -> 자동 최신화)
        await this.redisClient.setLocation(memberId, gameId, locationData);

        // 경계선 확인 => turf.js 사용
        const gameSetting = await this.redisClient.getGameSetting(gameId);
        const isInBoundary = await this.turfService.checkUserInBoundary([lng, lat], gameSetting.boundaryGeo.coordinates[0]);

        // 도둑이고, 상태가 PRISON 일때 탈옥 판별
        // 감옥에서 10m 이상 벗어났을때 탈옥 (오차범위 5m) 
        if (position === GameMemberPosition.THIEF && status === GameMemberStatus.PRISON
            && !(await this.turfService.checkUserInPrison([lng, lat], [gameSetting.prisonLng, gameSetting.prisonLat]))) {

            await this.gameMemberService.updateMemberStatus(gameId, memberId, GameMemberStatus.FREE);

            const res = {
                gameId: gameId,
                thiefId: memberId,
                escapePointId: 3,
                escapedAt: new Date().toISOString(),
            };
            this.io.to(gameId).emit("get escape", res);
            return;
        }


        if (!isInBoundary && position === GameMemberPosition.THIEF) {
            const res = {
                gameId: gameId,
                memberId: memberId,
                message: "out of boundary",
                type: "outOfBoundary",
                createdAt: new Date().toISOString(),
            };

            // 도둑일 때 경계선을 벗어났으면 패널티 부여 -> redis에서 관리.
            // 만약 3번 이상 벗어나면 자동으로 감옥으로 이송. -> 다음 패널티 체크 활성화까지 10초
            const count = await this.redisClient.increasePenalty(memberId, gameId);

            await this.redisClient.setLocation(memberId, gameId, locationData);

            res.penalty = count || penalty;
            locationData.penalty = res.penalty;
            await this.redisClient.setLocation(memberId, gameId, locationData);

            if (count >= 3) {
                locationData.status = GameMemberStatus.TRANSFER;
                await this.redisClient.deletePenalty(memberId, gameId);

                locationData.penalty = 0;
                await this.redisClient.setLocation(memberId, gameId, locationData);

                const isGameEnd = await this.gameService.checkGameHaveToFinish(gameId);

                if (isGameEnd) {
                    await this.gameEnd(this.io, this.redisClient, gameId, GameMemberPosition.POLICE);
                    return;
                }

                this.io.to(gameId).emit("get arrest", {
                    gameId: gameId,
                    policeId: null,
                    thiefId: memberId,
                    result: "SUCCESS",
                    reason: "PENALTY",
                    arrestedAt: new Date().toISOString(),
                });
                return;
            }
            this.socket.emit("out of boundary", res);
        }

        // 이송 중이고, 감옥 범위 안에 들어왔을때
        if (position === GameMemberPosition.THIEF && status === GameMemberStatus.TRANSFER
            && await this.turfService.checkUserInPrison([lng, lat], [gameSetting.prisonLng, gameSetting.prisonLat])) {

            await this.gameMemberService.updateMemberStatus(gameId, memberId, GameMemberStatus.PRISON);
            const res = {
                gameId: gameId,
                thiefId: memberId,
                status: GameMemberStatus.PRISON,
                arrestedAt: new Date().toISOString(),
            };
            this.io.to(gameId).emit("modify member status", res);
            return;
        }

        if (position === GameMemberPosition.POLICE) {
            return;
        }

        // 비프음
        const locationDatas = await this.redisClient.getAllLocations(gameId);
        let polices;
        if (locationDatas) {
            polices = locationDatas
                .filter(data => data.position === GameMemberPosition.POLICE && data.isConnected === true)
                .map(({ memberId, lng, lat }) => ({ policeId: memberId, lng, lat }));

        }

        if (polices && polices.length > 0) {
            const nearPolice = this.turfService.checkNearPolice([lng, lat], polices);
            if (nearPolice) {
                this.socket.emit("get beep use", {
                    gameId: gameId,
                    policeId: nearPolice.policeId,
                    thiefId: memberId,
                    distance: nearPolice.distance,
                });
            }
        }
    }

    syncGameInfo = async (payload) => {
        const gameId = parseInt(payload.gameId) || parseInt(this.socket.data.gameId);

        const game = await this.gameService.findGame(gameId);
        const gameMissions = await this.gameMissionService.findAllByGameId(gameId);

        // 1. DB에서 프로필 정보 가져오기
        const gameMembers = await this.gameMemberService.findMembersWithProfileByGameId(gameId);
        // 2. Redis에서 실시간 정보 가져오기
        const locationDatas = await this.redisClient.getAllLocations(gameId);

        const res = {
            gameId: gameId,
            gameStatus: game.status,
            members: gameMembers.map(dbMember => {
                // Redis 데이터 매칭 (memberId는 문자열/숫자 차이 있을 수 있으므로 파싱 후 비교)
                const redisMember = locationDatas.find(r => parseInt(r.memberId) === parseInt(dbMember.memberId));
                return {
                    memberId: dbMember.memberId,
                    nickname: dbMember.Member?.MemberProfile?.nickname,
                    avatarUrl: dbMember.Member?.MemberProfile?.avatarUrl,
                    // Redis 데이터가 있으면 우선 사용, 없으면 DB 데이터 사용
                    position: redisMember?.position || null,
                    status: redisMember?.status || null,
                    penalty: redisMember?.penalty || 0,
                    missionCompleted: redisMember?.missionCompleted || false,
                    isConnected: redisMember?.isConnected || false,
                };
            }),
            missions: gameMissions
        };
        this.socket.emit("get sync game info", res);
    }

    postArrest = async (payload) => {
        const gameId = parseInt(payload.gameId) || parseInt(this.socket.data.gameId);
        const policeId = parseInt(payload.policeId) || parseInt(this.socket.data.memberId);
        const thiefId = parseInt(payload.thiefId);

        const thief = await this.gameMemberService.findMemberGame(gameId, thiefId);

        // 도둑이 아닐때 체포 실패
        if (thief.position !== GameMemberPosition.THIEF) {
            const res = {
                gameId: gameId,
                policeId: policeId,
                thiefId: thiefId,
                result: "FAIL",
                reason: "NOT_THIEF",
                arrestedAt: new Date().toISOString(),
            };
            this.io.to(gameId).emit("get arrest", res);
            return;
        }

        // 이미 체포가 됐을때 다시 체포하면 실패
        if (thief.status && thief.status !== GameMemberStatus.FREE) {
            const res = {
                gameId: gameId,
                policeId: policeId,
                thiefId: thiefId,
                result: "FAIL",
                reason: "ALREADY_CAUGHT",
                arrestedAt: new Date().toISOString(),
            };
            this.io.to(gameId).emit("get arrest", res);
            return;
        }

        const police = await this.gameMemberService.findMemberGame(gameId, policeId);

        if (police.position !== GameMemberPosition.POLICE) {
            const res = {
                gameId: gameId,
                policeId: policeId,
                thiefId: thiefId,
                result: "FAIL",
                reason: "ASSERTER_NOT_POLICE",
                arrestedAt: new Date().toISOString(),
            };

            this.io.to(gameId).emit("get arrest", res);
            return;
        }

        await this.gameService.processArrest(gameId, thiefId, policeId);

        // 정상 체포 로직
        const res = {
            gameId: gameId,
            policeId: policeId,
            thiefId: thiefId,
            result: "SUCCESS",
            reason: null,
            arrestedAt: new Date().toISOString(),
        };

        // 게임 종료 조건 검사
        const isGameEnd = await this.gameService.checkGameHaveToFinish(gameId);

        if (isGameEnd) {
            await this.gameEnd(this.io, this.redisClient, gameId, isGameEnd);
            return;
        }

        this.io.to(gameId).emit("get arrest", res);
    }

    postSkillUse = async (payload) => {
        let { gameId, policeId } = payload;

        policeId = parseInt(policeId) || parseInt(this.socket.data.memberId);
        gameId = parseInt(gameId);

        // 게임 스킬 정보 조회
        const skill = await this.gameSkillService.findGameSkill(parseInt(gameId), parseInt(policeId));

        // 이미 사용된 스킬이면 실패
        if (skill.isUsed) {
            const res = {
                gameId: gameId,
                policeId: policeId,
                result: "FAIL",
                reason: "ALREADY_USED",
                usedAt: new Date().toISOString(),
            };
            this.io.to(gameId).emit("get skill use", res);
            return;
        }

        await this.gameSkillService.useSkill(skill.id);
        const gameSetting = await this.gameSettingService.findGameSetting(gameId);
        await this.redisClient.setSkillUsedAt(gameId, gameSetting.timeLimit);

        const res = {
            gameId: gameId,
            policeId: policeId,
            result: "SUCCESS",
            reason: null,
            startedAt: new Date(Date.now() + 5000).toISOString(),
        };

        this.io.to(gameId).emit("get skill use", res);
    }

    /**
     * 
     */
    postMissionImage = async (payload) => {
        const gameId = parseInt(payload.gameId) || parseInt(this.socket.data.gameId);
        const memberId = parseInt(payload.memberId) || parseInt(this.socket.data.memberId);
        const missionId = parseInt(payload.missionId) || parseInt(payload.gameMissionId);
        const image = String(payload.image);

        const gameMission = await this.gameMissionService.findMission(missionId);

        this.mq.sendMessage({
            gameId: gameId,
            memberId: memberId,
            gameMissionId: gameMission.id,
            keyword: gameMission.Mission.keyword,
            image: image,
        }, MQConfig.MQ_MISSION);
    }

    /**
     * gameId, winnerPosition
     * 
     * 게임 종료 로직
     * 1. 게임 타이머 삭제
     * 2. redis에서 멤버 스탯들 받아오기
     * 3. spring boot 서버로 요청보내기
     * 
     * 게임 상태 변경, 멤버 스탯 저장은 모두 스프링에서 진행
     */
    gameEnd = async (io, redisClient, gameId, winTeam) => {
        const integerGameId = parseInt(gameId);
        // 게임 종료 1분 타이머가 있으면 게임이 이미 종료된 것으로 판별하여 종료
        const isEnded = await redisClient.getGameEnd(integerGameId);
        if (isEnded) {
            return;
        }

        const isMine = await redisClient.setGameTimerLock(integerGameId);

        // 한 프로세스에서만 게임 종료 로직이 실행될 수 있게 락 설정
        if (!isMine) {
            return;
        }

        // redis에서 게임 타이머 삭제
        await redisClient.deleteGameTimer(integerGameId);

        // 게임 종료 처리 => spring boot에 요청을 보내야함.
        const gameMembers = await redisClient.getAllLocations(integerGameId);

        const memberStats = gameMembers.map(member => ({
            memberId: member.memberId,
            position: member.position,
            walk: member.walk,
            longestSurvived: member.longestSurvived,
            isConnected: member.isConnected,
            status: member.status
        }));

        const gameToken = await this.redisClient.getGameToken(integerGameId);

        logger.info(`[GameController] gameId: ${integerGameId}, winTeam: ${winTeam}, memberStats: ${JSON.stringify(memberStats)}`);

        let res = await axios.post(`${process.env.SPRING_BOOT_URL}/games/result`, {
            gameId: integerGameId,
            winTeam: winTeam,
            memberStats: memberStats
        }, {
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${gameToken}`
            }
        });

        if (res.data.code !== 200) {
            io.to(gameId).emit("get end game", {
                gameId: gameId,
                winTeam: winTeam,
                message: "게임이 종료에 실패하였습니다.",
                reason: "SERVER_ERROR",
                code: 500
            });
            return;
        }

        // 게임 종료 후 1분 동안만 유지
        await redisClient.setGameEnd(integerGameId);
        await redisClient.deleteAllGameCachesByGameId(integerGameId);

        // 게임 종료 알림
        io.to(integerGameId).emit("get end game", {
            gameId: integerGameId,
            winTeam: winTeam,
            message: winTeam === GameMemberPosition.THIEF
                ? "도둑 승!!"
                : "경찰 승!!",
            reason: null,
            code: 200
        });
    }

    retryEndGame = async (payload) => {
        const gameId = parseInt(payload.gameId);
        const winTeam = payload.winTeam;

        const isGameHaveToFinish = await this.gameService.checkGameHaveToFinish(gameId);
        if (!isGameHaveToFinish) {
            sendError(this.socket, {
                gameId: gameId,
                winTeam: winTeam,
                message: "게임이 종료되지 않았습니다.",
                reason: "GAME_NOT_FINISHED",
                code: 400
            }, "GameError");
            return;
        }

        await this.gameEnd(this.io, this.redisClient, gameId, winTeam);
    }

    /**
     * 게임 종료 후 개인이 보내는 이벤트
     * 
     * 이 이벤트가 일어나야만 redis gps 삭제 로직과 패널티 삭제로직이 실행됨.
     * 
     * {
     *   'gameId': 1,
     *   'memberId': 1,
     *   'position': 'thief',
     *   'walk': 1,
     *   'longestSurvived': 1
     * }
     */
    postGameEndAfter = async (payload) => {
        const gameId = this.socket.data.gameId;
        const memberId = this.socket.data.memberId;

        // 사용자의 게임 캐시 삭제 (참여자마다 필요)
        await this.redisClient.deleteGameCachesByMemberId(memberId, gameId);

        // 결과 조회와 브로드캐스트는 게임당 한 번이면 충분하다.
        // 이 핸들러는 참여자 수만큼 호출되므로, 락이 없으면
        // 같은 결과를 인원수만큼 조회해 룸 전체에 인원수만큼 뿌리게 된다.
        if (!await this.redisClient.setEndGameAfterLock(gameId)) {
            return;
        }

        const res = await axios.get(`${process.env.SPRING_BOOT_URL}/games/${gameId}/result`, {
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${await this.redisClient.getAccessToken(memberId)}`
            }
        });

        this.io.to(gameId).emit("get end game after", res.data);
    }

    gameReset = async () => {
        const gameId = this.socket.data.gameId;
        await this.redisClient.deleteAllGameCachesByGameId(gameId);
        this.io.to(gameId).emit("get reset game", {
            gameId: gameId,
            message: "게임관련 캐시가 모두 삭제되었습니다."
        });
    }

    postUpdateAccessToken = async (data) => {
        const accessToken = data.accessToken;

        resolveInController(accessToken);

        this.socket.data.accessToken = data.accessToken;

        this.socket.emit("get update access token", { "accessToken": data.accessToken });
    }

    postCheckNews = async (payload) => {
        const gameId = parseInt(payload.gameId) || parseInt(this.socket.data.gameId);

        const res = await this.redisClient.getNews(gameId);

        if (!res) {
            this.io.to(gameId).emit("get check news", {
                gameId: gameId,
                newsId: null,
                message: "뉴스 정보가 없습니다.",
                code: 404
            });
            return;
        }

        this.io.to(gameId).emit("get check news", {
            gameId: gameId,
            newsId: res,
            message: "뉴스 정보가 있습니다.",
            code: 200
        });
    }

    // custom disconnect
    disconnect = async () => {
        this.socket.data.isIntentionalExit = true;
        const gameId = parseInt(this.socket.data.gameId);
        const memberId = parseInt(this.socket.data.memberId);

        await this.gameMemberService.updateInGameConnected(gameId, memberId, false);

        const isGameEnd = await this.gameService.checkGameHaveToFinish(gameId);

        if (isGameEnd) {
            await this.gameEnd(this.io, this.redisClient, gameId, isGameEnd);
        }

        await this.redisClient.deleteAccessToken(memberId);
        this.socket.disconnect();
    }

    postRadio = async () => {
        const gameId = parseInt(this.socket.data.gameId);
        const memberId = parseInt(this.socket.data.memberId);

        this.io.to(gameId).emit("get radio", {
            gameId: gameId,
            memberId: memberId
        });
    }

    makeError = (message, text, code) => {
        const error = new Error(message);
        error.code = code;
        error.text = text;
        throw error;
    }
}