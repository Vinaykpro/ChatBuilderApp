package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ThemeItem

@Preview
@Composable
fun ThemeScreen() {
    var selectedTheme by remember { mutableIntStateOf(0) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding())
        ) {
            BasicToolbar(name = "Chat theme", icon1 = painterResource(R.drawable.ic_info))
            LazyColumn(modifier = Modifier.weight(1f).padding(bottom = 10.dp)) {
                items(5) { i ->
                    if (i == 0)
                        Text(
                            text = "Select or customize themes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight(500),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 18.dp, start = 20.dp, bottom = 6.dp)
                        )
                    ThemeItem(selected = i == selectedTheme, name = "Theme $i", onClick = { selectedTheme = i }, onNextClick = {})
                }
            }
        }
        FloatingActionButton(
            onClick = { /* TO-DO Add new theme */ },
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 40.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Theme", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Theme", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}