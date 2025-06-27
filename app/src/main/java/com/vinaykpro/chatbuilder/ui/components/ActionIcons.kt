package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@Composable
fun ActionIcons() {
    val data = remember { mutableStateOf(List(3) { "Icon $it" }) }
    var debug by remember { mutableStateOf("") }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data.value = data.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        debug = data.value.toString()
    })
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .padding(4.dp)
            .height(200.dp)
            .reorderable(state)
            .detectReorderAfterLongPress(state)
    ) {
        items(data.value, { it }) { item ->
            ReorderableItem(state, key = item) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                Box(
                    modifier = Modifier
                        .shadow(elevation.value)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ActionIconItem(item)
                }
            }
        }
    }
    //Text(text = debug, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight(500), fontSize = 14.sp, lineHeight = 14.sp, modifier = Modifier.padding(start = 12.dp))
}

@Preview
@Composable
fun ActionIconItem(
    name: String = "Icon 1"
) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_move),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .padding(14.dp)
                .size(24.dp)
        )
        Image(painter = painterResource(R.drawable.ic_videocall), contentDescription = null, modifier = Modifier
            .size(50.dp)
            .clip(shape = RoundedCornerShape(15.dp))
            .background(Color(0xFF31ABBB)))
        Text(text = name, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight(500), fontSize = 14.sp, lineHeight = 14.sp, modifier = Modifier.padding(start = 12.dp))
        Spacer(modifier = Modifier.weight(1f))
        IconButton( onClick = {} ) {
            Icon(
                painter = painterResource(R.drawable.ic_replace),
                contentDescription = "replace",
                tint = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .padding(5.dp)
                    .size(18.dp)
            )
        }

        IconButton( onClick = {} ) {
            Icon(
                painter = painterResource(R.drawable.ic_eye),
                contentDescription = null,
                tint = LightColorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
            )
        }

    }
}
