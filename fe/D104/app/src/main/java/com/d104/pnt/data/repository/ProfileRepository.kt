package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.model.response.ProfileResponse
import com.d104.pnt.data.remote.model.response.ThiefStatResponse
import com.d104.pnt.data.remote.model.response.PoliceStatResponse
import com.d104.pnt.domain.model.common.BaseResult

interface ProfileRepository {
    /**
     * 내 프로필 조회
     */
    suspend fun getMyProfile(memberId: Long): BaseResult<ProfileResponse>

    /**
     * 프로필 수정
     */
    suspend fun updateProfileImage(memberId: Long, imageKey: String): BaseResult<ProfileResponse>
    suspend fun updateNickname(memberId: Long, nickname: String): BaseResult<ProfileResponse>

    /**
     * 도둑 스탯 조회
     */
    suspend fun getThiefStat(memberId: Long): BaseResult<ThiefStatResponse>

    /**
     * 경찰 스탯 조회
     */
    suspend fun getPoliceStat(memberId: Long): BaseResult<PoliceStatResponse>
}