package com.d104.pnt.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.navigation.BottomNavBar
import com.d104.pnt.navigation.BottomNavItem
import com.d104.pnt.navigation.NavArgs
import com.d104.pnt.navigation.Routes
import com.d104.pnt.ui.chatroom.chat.ChatRoomScreen
import com.d104.pnt.ui.chatroom.create.ChatRoomCreateScreen
import com.d104.pnt.ui.chatroomlist.ChatRoomListScreen
import com.d104.pnt.ui.component.KickedNoticeDialog
import com.d104.pnt.ui.game.create.GameCreateScreen
import com.d104.pnt.ui.game.end.GameResultScreen
import com.d104.pnt.ui.game.end.news.GameNewsScreen
import com.d104.pnt.ui.game.end.news.NewsLoadingScreen
import com.d104.pnt.ui.game.load.GameLoadingScreen
import com.d104.pnt.ui.game.play.GamePlayScreen
import com.d104.pnt.ui.game.play.GameRoleScreen
import com.d104.pnt.ui.game.play.mission.CameraScreen
import com.d104.pnt.ui.game.wait.GameRoomScreen
import com.d104.pnt.ui.game.wait.role.RoleSelectScreen
import com.d104.pnt.ui.home.HomeScreen
import com.d104.pnt.ui.profile.ProfileScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navigateToIntro: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = BottomNavItem.items.map { it.route }

    var backPressedTime by remember { mutableLongStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pendingChatId by viewModel.pendingChatRoomId.collectAsStateWithLifecycle()

    LaunchedEffect(pendingChatId) {
        pendingChatId?.let { chatId ->

            delay(100)

            navController.navigate(Routes.buildChatRoom(chatId)) {
                popUpTo(Routes.HOME) {
                    inclusive = false
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }

            viewModel.clearPendingChatRoomId()
        }
    }

    BackHandler(enabled = currentRoute in bottomBarRoutes) {

        if (System.currentTimeMillis() - backPressedTime <= 2000) {
            activity?.finish()
        } else {
            backPressedTime = System.currentTimeMillis()
            scope.launch {
                snackbarHostState.showSnackbar("한 번 더 누르면 종료됩니다")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // BottomNav 탭
            composable(Routes.HOME) { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val kickMessage = savedStateHandle.get<String>("kick_message")

                if (kickMessage != null) {
                    KickedNoticeDialog(
                        reason = kickMessage,
                        onConfirm = {
                            savedStateHandle.remove<String>("kick_message")
                        }
                    )
                }

                HomeScreen(
                    goToGameCreate = {
                        navController.navigate(Routes.GAME_CREATE)
                    },
                    navigateToGameRoom = { roomId ->
                        navController.navigate(Routes.buildRoleSelect(roomId))
                    },
                    navigateToIntro = { navigateToIntro() }
                )
            }

            composable(Routes.CHAT) {
                ChatRoomListScreen(
                    navigateToChatRoom = { chatRoomId ->
                        navController.navigate(Routes.buildChatRoom(chatRoomId))
                    },
                    navigateToChatCreate = {
                        navController.navigate(Routes.CHAT_CREATE)
                    }
                )
            }

            composable(Routes.CHAT_CREATE) {
                ChatRoomCreateScreen(
                    onCancel = { navController.popBackStack() },
                    onConfirm = { chatRoomId ->

                        navController.navigate(Routes.buildChatRoom(chatRoomId)) {
                            popUpTo(Routes.CHAT) { inclusive = false }
                        }
                    },
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen()
            }

            // 채팅방
            composable(
                route = "${Routes.CHAT_ROOM}/{${NavArgs.CHAT_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.CHAT_ID) { type = NavType.LongType }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "pnt://chat/{${NavArgs.CHAT_ID}}"
                    }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getLong(NavArgs.CHAT_ID) ?: 0L

                ChatRoomScreen(
                    modifier = Modifier.fillMaxSize(),
                    onBackPressed = {
                        navController.popBackStack(Routes.CHAT, inclusive = false)
                    },
                    navController = navController
                )
            }

            // 게임 대기방
            composable(
                route = "${Routes.GAME_ROOM}/{${NavArgs.ROOM_ID}}/{${NavArgs.ROLE}}",
                arguments = listOf(
                    navArgument(NavArgs.ROOM_ID) { type = NavType.LongType },
                    navArgument(NavArgs.ROLE) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getLong(NavArgs.ROOM_ID) ?: 0L
                val roleString = backStackEntry.arguments?.getString(NavArgs.ROLE) ?: "THIEF"

                GameRoomScreen(
                    roomId = roomId,
                    initialRole = GameRole.fromName(roleString),
                    onStartGame = { gameId, role ->
                        navController.navigate(Routes.buildGamePlay(gameId, role.name)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onChangeRole = {
                        val route = Routes.buildRoleSelect(roomId) + "?isEditMode=true"
                        navController.navigate(route)
                    },
                    onBackPressed = { navController.popBackStack() },

                    onNavigateRole = { rId, role, isChief ->
                        val route = Routes.buildGameIntro(rId, role.name) + "?isChief=$isChief"
                        navController.navigate(route)
                    },

                    onNavigateHome = { message ->
                        if (message != null) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("kick_message", message)
                        }
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                )
            }

            // 게임 플로우

            // 역할 선택
            composable(
                route = "${Routes.ROLE_SELECT}/{${NavArgs.ROOM_ID}}?isEditMode={isEditMode}",

                arguments = listOf(
                    navArgument(NavArgs.ROOM_ID) { type = NavType.LongType },

                    navArgument("isEditMode") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getLong(NavArgs.ROOM_ID) ?: 0L

                val isEditMode = backStackEntry.arguments?.getBoolean("isEditMode") ?: false

                RoleSelectScreen(
                    onRoleSelected = { role: GameRole ->
                        navController.navigate(Routes.buildGameRoom(roomId, role.name)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onBackPressed = { navController.popBackStack() },
                    isEditMode = isEditMode
                )
            }

            // 역할 안내
            composable(
                route = "${Routes.GAME_ROLE}/{${NavArgs.ROOM_ID}}/{${NavArgs.ROLE}}?isChief={isChief}",
                arguments = listOf(
                    navArgument(NavArgs.ROOM_ID) { type = NavType.LongType },
                    navArgument(NavArgs.ROLE) { type = NavType.StringType },
                    navArgument("isChief") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getLong(NavArgs.ROOM_ID) ?: 0L
                val roleName = backStackEntry.arguments?.getString(NavArgs.ROLE) ?: "THIEF"
                val isChief = backStackEntry.arguments?.getBoolean("isChief") ?: false
                val role = GameRole.fromName(roleName)

                GameRoleScreen(
                    role = role,
                    isChief = isChief,
                    onIntroFinished = {
                        navController.navigate(Routes.buildGameLoading(roomId, role.name)) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    }
                )
            }

            // 게임 로딩
            composable(
                route = "${Routes.GAME_LOADING}/{${NavArgs.ROOM_ID}}/{${NavArgs.ROLE}}",
                arguments = listOf(
                    navArgument(NavArgs.ROOM_ID) { type = NavType.LongType },
                    navArgument(NavArgs.ROLE) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getLong(NavArgs.ROOM_ID) ?: 0L
                val roleName = backStackEntry.arguments?.getString(NavArgs.ROLE) ?: "THIEF"
                val role = GameRole.fromName(roleName)

                GameLoadingScreen(
                    roomId = roomId,
                    role = role,
                    onLoadingComplete = { gameId ->
                        navController.navigate(Routes.buildGamePlay(gameId, role.name)) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    }
                )
            }

            composable(Routes.GAME_CREATE) {
                GameCreateScreen(
                    onCancel = {
                        navController.popBackStack()
                    },
                    onConfirm = { roomId ->
                        navController.navigate(Routes.buildRoleSelect(roomId)) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }

            composable(
                route = "${Routes.MISSION_CAMERA}/{${NavArgs.MISSION_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.MISSION_ID) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val missionId = backStackEntry.arguments?.getLong(NavArgs.MISSION_ID) ?: 0L
                CameraScreen(
                    onPhotoConfirmed = {
                        navController.popBackStack()
                    },
                    onClose = {
                        navController.popBackStack()
                    },
                    compressionQuality = 80,
                    maxWidth = 1280,
                    maxHeight = 720,
                    missionId = missionId
                )
            }


            // 게임 플레이
            composable(
                route = "${Routes.GAME_PLAY}/{${NavArgs.GAME_ID}}/{${NavArgs.ROLE}}",
                arguments = listOf(
                    navArgument(NavArgs.GAME_ID) { type = NavType.LongType },
                    navArgument(NavArgs.ROLE) {
                        type = NavType.StringType
                        defaultValue = "THIEF"
                    }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getLong(NavArgs.GAME_ID) ?: 0L
                val roleString = backStackEntry.arguments?.getString(NavArgs.ROLE) ?: "THIEF"

                GamePlayScreen(
                    gameId = gameId,
                    role = GameRole.fromName(roleString),
                    onBackToHome = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    },
                    onNavigateToLoading = { id ->
                        navController.navigate(Routes.buildNewsLoading(id)) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    },
                    goToCamera = { missionId ->
                        navController.navigate(
                            Routes.buildMissionCamera(
                                missionId
                            )
                        )
                    }
                )
            }

            composable(
                route = "${Routes.GAME_NEWS_LOADING}/{${NavArgs.GAME_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.GAME_ID) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getLong(NavArgs.GAME_ID) ?: 0L

                NewsLoadingScreen(
                    gameId = gameId,
                    onNewsReady = { gId, nId ->
                        navController.navigate(Routes.buildGameNews(gId, nId)) {
                            popUpTo(Routes.GAME_NEWS_LOADING) { inclusive = true }
                        }
                    }
                )
            }

            // 게임 뉴스
            composable(
                route = "${Routes.GAME_NEWS}/{${NavArgs.GAME_ID}}/{${NavArgs.NEWS_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.GAME_ID) { type = NavType.LongType },
                    navArgument(NavArgs.NEWS_ID) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getLong(NavArgs.GAME_ID) ?: 0L
                val newsId = backStackEntry.arguments?.getLong(NavArgs.NEWS_ID) ?: 0L

                GameNewsScreen(
                    gameId = gameId,
                    newsId = newsId,
                    onNextClick = {
                        navController.navigate(Routes.buildGameResult(gameId)) {
                            popUpTo(Routes.GAME_NEWS_LOADING) { inclusive = true }
                        }
                    }
                )
            }

            // 게임 결과
            composable(
                route = "${Routes.GAME_RESULT}/{${NavArgs.GAME_ID}}",
                arguments = listOf(
                    navArgument(NavArgs.GAME_ID) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getLong(NavArgs.GAME_ID) ?: 0L

                GameResultScreen(
                    gameId = gameId,
                    onBackToHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onBackToWaitingRoom = { roomId ->
                        navController.navigate(
                            Routes.buildGameRoom(
                                roomId,
                                GameRole.ANY.roleNameEn
                            )
                        ) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    }
                )
            }


            // 신고
            composable(
                route = "${Routes.REPORT}/{${NavArgs.NICKNAME}}",
                arguments = listOf(
                    navArgument(NavArgs.NICKNAME) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val nickname = backStackEntry.arguments?.getString(NavArgs.NICKNAME) ?: ""
            }

        }

        if (currentRoute in bottomBarRoutes) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BottomNavBar(navController)
            }
        }


        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(bottom = 80.dp)
        )
    }
}