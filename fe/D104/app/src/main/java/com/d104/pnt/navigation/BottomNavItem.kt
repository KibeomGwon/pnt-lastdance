package com.d104.pnt.navigation

import com.d104.pnt.R


sealed class BottomNavItem(
    val route: String,
    val icon: Int,
    val label: String
) {
    data object Chat : BottomNavItem("chat", R.drawable.ic_chat, "채팅방")
    data object Home : BottomNavItem("home", R.drawable.ic_logo, "home")
    data object Profile : BottomNavItem("profile", R.drawable.ic_user, "프로필")

    companion object {
        val items = listOf(Chat, Home, Profile)
    }
}