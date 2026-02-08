import chatPushMq from "./alarm/controller/ChatPushController.js";

async function worker() {
    const chatMq = await chatPushMq.create();
    await chatMq.consume();
}

export default worker;