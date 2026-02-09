package com.d104.pnt.ui.game.wait.role

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.pnt.R
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.DarkBackground

@Composable
fun RoleSelectScreen(
    onRoleSelected: (GameRole) -> Unit,
    onBackPressed: () -> Boolean,
    viewModel: RoleSelectViewModel = hiltViewModel(),
    isEditMode: Boolean = false
) {

    BackHandler {
        if (!isEditMode) {
            viewModel.leaveRoom()
        }
        onBackPressed()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.img_game_bg_1),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "선호 진영을 선택해주세요",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                color = Color.White
            )

            Spacer(Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoleCard(
                    role = GameRole.POLICE,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectRole(GameRole.POLICE) { role ->
                            onRoleSelected(role)
                        }
                    }
                )
                RoleCard(
                    role = GameRole.THIEF,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectRole(GameRole.THIEF) { role ->
                            onRoleSelected(role)
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            PixelContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectRole(GameRole.ANY) { role ->
                            onRoleSelected(role)
                        }
                    },
                backgroundColor = DarkBackground,
                borderColor = Color.White.copy(0.8f),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "상관없음",
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}
