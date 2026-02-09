package com.d104.pnt.ui.game.wait.role

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.DarkBackground


@Composable
fun RoleCard(role: GameRole, modifier: Modifier, onClick: () -> Unit) {
    PixelContainer(
        modifier = modifier.clickable(onClick = {
            onClick()
        }),
        backgroundColor = DarkBackground,
        borderColor = role.color
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(role.emoji),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Text(
                text = role.roleName,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                color = Color.White
            )

            Text(
                text = role.description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                color = Color.White
            )

        }
    }
}