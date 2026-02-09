package com.d104.pnt.data.remote.model.request

import com.google.gson.annotations.SerializedName

/**
 * 프로필 수정 요청
 */
data class UpdateProfileImageRequest(
    @SerializedName("avatarUrl")
    val avatarUrl: String?
)

data class UpdateNicknameRequest(
    @SerializedName("nickname")
    val nickname: String
)