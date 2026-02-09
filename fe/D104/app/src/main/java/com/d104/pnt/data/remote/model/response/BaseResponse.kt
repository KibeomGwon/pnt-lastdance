package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("success")
    val success: Boolean? = null
)