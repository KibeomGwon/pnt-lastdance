import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';
import logger from '../config/logger';


dotenv.config();

const secretKeyString = process.env.JWT_SECRET;
const secretKey = Buffer.from(secretKeyString, 'base64');

export const generateToken = (gameId, time) => {
    const payload = {
        sub: `game${gameId}`,
        memberId: gameId,
        role: "ROLE_USER"
    };

    const options = {
        algorithm: 'HS512',
        expiresIn: `${time + 5}m`,
        issuer: 'pnt-websocket'
    };

    try {
        return jwt.sign(payload, secretKey, options);
    } catch (error) {
        logger.error("토큰 생성 실패:", error);
        throw new Error("Token generation failed");
    }
};

export const generateMemberAccessToken = (memberId, timeLimit) => {
    const payload = {
        sub: "memberId",
        memberId: memberId,
        role: "ROLE_USER"
    };

    const options = {
        algorithm: 'HS512',
        expiresIn: timeLimit ? `${timeLimit + 5}m` : '30m',
        issuer: 'pnt-websocket'
    };

    try {
        return jwt.sign(payload, secretKey, options);
    } catch (error) {
        logger.error("토큰 생성 실패:", error);
        throw new Error("Token generation failed");
    }
};