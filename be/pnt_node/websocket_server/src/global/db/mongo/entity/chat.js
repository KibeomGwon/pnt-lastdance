import mongoose, { Schema } from "mongoose";
import Counter from "../utils/Counter.js";

const chatSchema = new Schema({
    _id: Number,
    content: String,
    memberId: Number,
    chatRoomId: String,
    avatarUrl: String,
    createdAt: String
}, {
    _id: false,
    versionKey: false
});

chatSchema.pre('save', async function (next) {
    if (!this.isNew) return;

    const counter = await Counter.findOneAndUpdate(
        { id: "chat_id_counter" },
        { $inc: { seq: 1 } },
        { new: true, upsert: true }
    );

    this._id = counter.seq;
});

export default mongoose.model("Chat", chatSchema);