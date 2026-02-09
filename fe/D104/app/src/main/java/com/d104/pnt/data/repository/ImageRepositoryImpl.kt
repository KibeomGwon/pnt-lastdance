package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.api.ImageApiService
import com.d104.pnt.data.remote.model.response.MissionPresignedUrlResponse
import com.d104.pnt.data.remote.model.response.PresignedUrlResponse
import com.d104.pnt.domain.model.common.ApiError
import com.d104.pnt.domain.model.common.BaseResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageApiService: ImageApiService
): ImageRepository, BaseRepository() {
    override suspend fun uploadImage(presignedUrl: String, image: File): BaseResult<Unit> {
        val extension = image.extension.lowercase()
        val contentType = if (extension == "jpg") "image/jpeg" else "image/$extension"
        return try {
            val requestBody = image.asRequestBody(contentType.toMediaTypeOrNull())

            val response = imageApiService.uploadImage(
                url = presignedUrl,
                requestBody = requestBody
            )

            // 4. 결과 판단
            if (response.isSuccessful) {
                BaseResult.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "S3 업로드 실패"
                BaseResult.Error(ApiError(message = errorMsg))
            }
        } catch (e: Exception) {
            BaseResult.Error(ApiError(message = e.message?: "s3 업로드 실패"))
        }
    }

    override suspend fun getPresignedUrlToProfile(
        id: Long,
        fileName: String
    ): BaseResult<PresignedUrlResponse> {
        return safeApiCall {
            imageApiService.getPresignedUrlForProfile(id, fileName)
        }
    }

    override suspend fun getPresignedUrlToMission(fileName: String): BaseResult<MissionPresignedUrlResponse> {
        return safeApiCall {
            imageApiService.getPresignedUrlForMission(fileName)
        }
    }
}