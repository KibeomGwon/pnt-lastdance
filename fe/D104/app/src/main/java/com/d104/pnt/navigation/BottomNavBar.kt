package com.d104.pnt.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.d104.pnt.ui.theme.DeepDark

@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    fun navigateToTab(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(Routes.HOME) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun navigateToHome() {
        if (currentRoute != BottomNavItem.Home.route) {
            navController.navigate(BottomNavItem.Home.route) {
                popUpTo(Routes.HOME) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(DeepDark.copy(0.8f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavItem(
                label = BottomNavItem.Chat.label,
                iconRes = BottomNavItem.Chat.icon,
                isSelected = currentRoute == BottomNavItem.Chat.route,
                modifier = Modifier.weight(1f)
            ) {
                navigateToTab(BottomNavItem.Chat.route)
            }

            Spacer(modifier = Modifier.width(40.dp))

            NavItem(
                label = BottomNavItem.Profile.label,
                iconRes = BottomNavItem.Profile.icon,
                isSelected = currentRoute == BottomNavItem.Profile.route,
                modifier = Modifier.weight(1f)
            ) {
                navigateToTab(BottomNavItem.Profile.route)
            }
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .offset(y = (-20).dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    navigateToHome()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = BottomNavItem.Home.icon),
                contentDescription = "HOME",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun NavItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Gray,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}