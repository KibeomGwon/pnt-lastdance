import { Router } from "express";
import { MissionController } from "./mission/controller/MissionController.js";
import { NewController } from "./news/controller/NewsController.js";

const router = Router();

export default router;

const missionController = new MissionController();

router.use("/mission", missionController.getRouter());

const newsController = new NewController();
router.use("/news", newsController.getRouter());
