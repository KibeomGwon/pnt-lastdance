package com.d104.pnt.navigation

import com.d104.pnt.domain.model.GameRole

/**
 * 앱 전체 라우트 정의
 */
object Routes {
    // 로그인 전 화면
    const val INTRO = "intro"
    const val LOGIN = "login"
    const val SIGNUP = "signup"

    // BottomNav 탭
    const val HOME = "home"
    const val CHAT = "chat"
    const val PROFILE = "profile"

    // 채팅
    const val CHAT_CREATE = "chat_create"

    const val CHAT_ROOM = "chat_room"
    fun buildChatRoom(chatId: Long) = "$CHAT_ROOM/$chatId"

    // 게임 생성
    const val GAME_CREATE = "game_create"

    const val ROLE_SELECT = "role_select"
    fun buildRoleSelect(roomId: Long) = "$ROLE_SELECT/$roomId"

    // 게임 대기방
    const val GAME_ROOM = "game_room"
    fun buildGameRoom(roomId: Long, role: String) = "$GAME_ROOM/$roomId/$role"

    const val GAME_ROOM_SETTINGS = "game_room_settings"
    fun buildGameRoomSettings(roomId: Long) = "$GAME_ROOM_SETTINGS/$roomId"

    // 게임 플로우
    const val GAME_ROLE = "game_role"
    fun buildGameIntro(roomId: Long, role: String) = "$GAME_ROLE/$roomId/$role"

    const val GAME_LOADING = "game_loading"
    fun buildGameLoading(roomId: Long, role: String) = "$GAME_LOADING/$roomId/$role"

    const val GAME_PLAY = "game_play"
    fun buildGamePlay(gameId: Long, role: String): String {
        return "$GAME_PLAY/$gameId/$role"
    }

    const val MISSION_CAMERA = "mission_camera"
    fun buildMissionCamera(missionId: Long) = "$MISSION_CAMERA/$missionId"

    const val GAME_NEWS_LOADING = "game_news_loading"
    fun buildNewsLoading(gameId: Long) = "$GAME_NEWS_LOADING/$gameId"

    const val GAME_NEWS = "game_news"
    fun buildGameNews(gameId: Long, newsId: Long) = "$GAME_NEWS/$gameId/$newsId"

    const val GAME_RESULT = "game_result"
    fun buildGameResult(gameId: Long) = "$GAME_RESULT/$gameId"

    // 프로필
    const val PROFILE_DETAIL = "profile_detail"
    fun buildProfileDetail(memberId: String) = "$PROFILE_DETAIL/$memberId"

    const val PROFILE_EDIT = "profile_edit"

    // 신고하기
    const val REPORT = "report"
    fun buildReport(nickname: String) = "$REPORT/$nickname"

    // 게임 플레이 경로 생성
    fun buildGamePlay(gameId: Long, role: GameRole): String {
        return "$GAME_PLAY/$gameId/${role.roleNameEn}"
    }
}