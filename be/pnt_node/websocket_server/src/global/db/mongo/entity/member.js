import mongoose, { Schema } from "mongoose";

const memberSchema = new Schema({
    memberId: { type: Number, unique: true, index: true },
    nickname: { type: String, maxlength: 20 },
    avatarUrl: { type: String, default: "default.png" },
    isDeleted: { type: Boolean, default: false },
    createdAt: { type: Date },
    updatedAt: { type: Date }
}, {
    versionKey: false
});

export default mongoose.model("Member", memberSchema);