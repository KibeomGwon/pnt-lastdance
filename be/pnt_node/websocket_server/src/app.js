import { createServer } from "http";
import express from "express";
import cors from "cors";
import { socketServer } from "./socket/SocketServer.js";
import router from "./rest/Router.js";
import { database } from "./global/db/database.js";
import mq from "./global/mq/MessagingQueue.js";
import swaggerUi from "swagger-ui-express";
import specs from "./global/swagger/swagger.js";
import morgan from "morgan";
import logger from "./global/config/logger.js";

const port = process.env.PORT || 8090;

process.on('uncaughtException', (err) => {
    logger.error('Uncaught Exception:', err);
    // 프로세스 종료 방지 (하지만 상태가 불안정할 수 있음)
});

process.on('unhandledRejection', (reason, promise) => {
    logger.error('Unhandled Rejection at:', promise, 'reason:', reason);
    // 프로세스 종료 방지
});

(async () => {
    const app = express();
    const server = createServer(app);

    app.use(cors());
    app.use(express.json());
    app.use(morgan('combined', { stream: { write: (message) => logger.info(message.trim()) } }));
    app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(specs));

    await mq.create();
    await database();
    await socketServer(server);

    app.use("/", router);

    app.get("/", (req, res) => {
        logger.info("request in root");
        res.status(200).json({ "message": "ok" });
    });

    server.listen(port, () => {
        logger.info(`Listen in port: ${port}`);
    });
})();