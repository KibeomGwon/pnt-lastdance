import worker from "./worker";
import mq from "./global/mq/MessagingQueue.js";
import { database } from "./global/db/database.js";

(async () => {
    try {
        await database();
        await mq.create();
        await worker();
    } catch (error) {
        console.error(error);
    }
})();