package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.model.response.MissionPresignedUrlResponse
import com.d104.pnt.data.remote.model.response.PresignedUrlResponse
import com.d104.pnt.domain.model.common.BaseResult
import java.io.File

interface ImageRepository {
    suspend fun uploadImage(presignedUrl: String, image: File): BaseResult<Unit>

    suspend fun getPresignedUrlToProfile(id: Long, fileName: String): BaseResult<PresignedUrlResponse>

    suspend fun getPresignedUrlToMission(fileName: String): BaseResult<MissionPresignedUrlResponse>
}