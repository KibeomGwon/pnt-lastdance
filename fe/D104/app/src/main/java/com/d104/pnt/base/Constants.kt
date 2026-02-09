package com.d104.pnt.base

object Constants {

    // API
    const val BASE_URL = "https://i14d104.p.ssafy.io/"
    const val SPRING_SERVER_URL = "${BASE_URL}spring/"
    const val LIVEKIT_URL = "wss://ssafy-d104-f7ty2iv4.livekit.cloud"

    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // DataStore
    const val PREF_NAME = "cop_and_thief_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_MEMBER_ID = "member_id"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_LAST_REFRESH = "last_refresh"

    const val CHANNEL_CHAT_ROOM = "chat_notification_channel"

}