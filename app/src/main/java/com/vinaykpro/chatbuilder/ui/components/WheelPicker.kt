package com.vinaykpro.chatbuilder.ui.components

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.lib.kmp.compose_wheelpicker.FVerticalWheelPicker
import com.sd.lib.kmp.compose_wheelpicker.rememberFWheelPickerState
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Composable
fun WheelPicker(
    items: List<String>,
    visibleExtrasCount: Int = 2,
    selectedIndex: Int = 0,
    onItemChange: (Int) -> Unit
) {
    val state = rememberFWheelPickerState()

    LaunchedEffect(selectedIndex) {
        state.animateScrollToIndex(selectedIndex)
    }

    LaunchedEffect(state) {
        snapshotFlow { state.snappedIndex }
            .collect {
                Log.i("Vkpro", "Swapped Sender index $it ; ${items.size}")
                if(it >= 0) onItemChange(it)
            }
    }

    FVerticalWheelPicker(
        state = state,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        count = items.size,
        unfocusedCount = visibleExtrasCount,
        focus = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = LightColorScheme.primary,
                        shape = RoundedCornerShape(15.dp)
                    )
            )
        }

    ) { index ->
        Text(
            text = items[index],
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
