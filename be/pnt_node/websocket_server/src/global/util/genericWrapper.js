import logger from '../config/logger.js';
import util from 'util';

/**
 * AOP wrapper for generic functions (non-socket event handlers).
 * Logs execution start, duration, and errors.
 * 
 * @param {string} handlerName - Name of the function for logging
 * @param {Function} handlerFn - The function to wrap
 * @param {boolean} rethrowError - Whether to rethrow the error after logging (default: false)
 */
export const withFunctionLogging = (handlerName, handlerFn, rethrowError = false) => {
    return async (...args) => {
        const startTime = Date.now();

        try {
            // args might be large objects, so we use util.inspect to safely stringify them and avoid circular reference errors.
            logger.info(`[EXEC] ${handlerName} - Args: ${util.inspect(args, { depth: 0, compact: true, breakLength: Infinity })}`);

            const result = await handlerFn(...args);

            const duration = Date.now() - startTime;
            logger.info(`[DONE] ${handlerName} - Duration: ${duration}ms`);

            return result;
        } catch (error) {
            const duration = Date.now() - startTime;

            logger.error(`[ERROR] ${handlerName} - ${error.message} (Duration: ${duration}ms)`);
            if (error.stack) {
                logger.error(error.stack);
            }

            if (rethrowError) {
                throw error;
            }
        }
    };
};
