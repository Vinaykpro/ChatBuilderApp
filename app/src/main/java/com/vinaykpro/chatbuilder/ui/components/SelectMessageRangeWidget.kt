package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun SelectMessageRangeWidget(
    rStart: Int = -1,
    rEnd: Int = -1,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(vertical = 1.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (rStart >= 0)
                    Modifier.border(
                        shape = RoundedCornerShape(5.dp),
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                else
                    Modifier.dashedBorder(1.5.dp, Color.LightGray, 4.dp, 2.dp, 5.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chats_unselected),
            contentDescription = "User profile icon",
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 13.dp)
                .padding(start = 5.dp, bottom = 4.dp)
                .size(30.dp),
            colorFilter = ColorFilter.tint(
                color = LightColorScheme.primary,
                blendMode = BlendMode.SrcIn
            )
        )
        Column() {
            Text(
                text = if (rStart < 0) "Select start to end range"
                else "Range (${rEnd - rStart + 1}) selected successfully!",
                fontSize = 15.sp,
                fontWeight = FontWeight(500),
                color = LightColorScheme.primary,
                modifier = Modifier.padding(top = 2.dp, bottom = 0.dp)
            )
            Text(
                text = if (rStart < 0) "Tap to choose messages from chat"
                else "You can tap again to modify range",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                lineHeight = 13.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(
                if (rStart < 0) R.drawable.ic_nextarrow
                else R.drawable.ic_edit
            ),
            contentDescription = "indicator",
            modifier = Modifier
                .padding(end = 20.dp)
                .size(18.dp)
                .alpha(0.3f)
        )
    }
}