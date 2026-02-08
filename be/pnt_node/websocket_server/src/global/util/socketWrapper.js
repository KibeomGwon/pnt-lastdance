import logger from "../config/logger.js";
import { sendError } from "./SocketError.js";
import util from "util";

/**
 * WebSocket 이벤트 핸들러를 감싸는 AOP 래퍼 함수
 * 
 * @param {string} handlerName - 로그에 표시할 핸들러 이름
 * @param {Function} handlerFn - 실제 실행할 핸들러 함수
 * @param {Object} socket - 소켓 객체 (에러 전송용)
 * @param {string} errorType - SendError에 사용할 에러 타입 (예: "RoomError")
 * @returns {Function} 래핑된 핸들러 함수
 */
export const withLogging = (handlerName, handlerFn, socket, errorType = "CommonError") => {
    return async (data, ack) => {
        const startTime = Date.now();

        try {
            // 1. [수신 로그]
            logger.info(`[RECV] ${handlerName} - MemberId: ${socket.data?.memberId || 'Guest'} - Params: ${util.inspect(data, { depth: 0, compact: true, breakLength: Infinity })}`);

            // 2. [비즈니스 로직 실행]
            await handlerFn(data);

            const duration = Date.now() - startTime;

            // 3. [성공 로그]
            logger.info(`[DONE] ${handlerName} - Duration: ${duration}ms`);

            // Ack가 필요한 경우 (추후 확장성을 위해 남겨둠)
            if (ack && typeof ack === 'function') {
                ack({ status: 'success' });
            }

        } catch (error) {
            const duration = Date.now() - startTime;

            // 4. [에러 로그]
            logger.error(`[ERROR] ${handlerName} - ${error.message} (Duration: ${duration}ms)`);
            if (error.stack) {
                logger.error(error.stack);
            }

            // 5. [공통 에러 처리] 기존의 catch 블록 로직 대체
            sendError(socket, error, errorType);
        }
    };
};
