package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.api.ProfileApiService
import com.d104.pnt.data.remote.model.request.UpdateNicknameRequest
import com.d104.pnt.data.remote.model.request.UpdateProfileImageRequest
import com.d104.pnt.data.remote.model.response.PoliceStatResponse
import com.d104.pnt.data.remote.model.response.ProfileResponse
import com.d104.pnt.data.remote.model.response.ThiefStatResponse
import com.d104.pnt.domain.model.common.BaseResult
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApiService: ProfileApiService
) : ProfileRepository, BaseRepository() {

    override suspend fun getMyProfile(memberId: Long): BaseResult<ProfileResponse> {
        return safeApiCall {
            profileApiService.getMyProfile(memberId)
        }
    }

    override suspend fun getThiefStat(memberId: Long): BaseResult<ThiefStatResponse> {
        return safeApiCall {
            profileApiService.getThiefStat(memberId)
        }
    }

    override suspend fun getPoliceStat(memberId: Long): BaseResult<PoliceStatResponse> {
        return safeApiCall {
            profileApiService.getPoliceStat(memberId)
        }
    }

    override suspend fun updateProfileImage(
        memberId: Long,
        avatarUrl: String
    ): BaseResult<ProfileResponse> {
        return safeApiCall {
            profileApiService.updateProfileImage(
                memberId = memberId,
                request = UpdateProfileImageRequest(avatarUrl)
            )
        }
    }

    override suspend fun updateNickname(
        memberId: Long,
        nickname: String
    ): BaseResult<ProfileResponse> {
        return safeApiCall {
            profileApiService.updateNickname(
                memberId = memberId,
                request = UpdateNicknameRequest(nickname)
            )
        }
    }
}