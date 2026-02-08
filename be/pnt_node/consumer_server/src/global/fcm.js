import admin from "firebase-admin";
import serviceAccount from "../../serviceAccountKey.json";

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

async function sendChatPush(tokens, data) {
    const message = {
        data: {
            title: data.title,
            body: data.content,
            chatRoomId: String(data.chatRoomId)
        },
        tokens: tokens
    };

    const response = await admin.messaging().sendEachForMulticast(message);

    if (response.failureCount > 0) {
        const failedTokens = [];
        response.responses.forEach((res, idx) => {
            if (!res.success) {
                failedTokens.push(tokens[idx]);
            }
        });

        message.tokens = failedTokens;
        await admin.messaging().sendEachForMulticast(message);
    }
}

export { sendChatPush };