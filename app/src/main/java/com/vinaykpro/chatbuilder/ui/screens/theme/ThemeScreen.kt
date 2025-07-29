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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ThemeItem

@Composable
fun ThemeScreen(
    themeViewModel: ThemeViewModel,
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    val themes by themeViewModel.themes.collectAsState()
    val selectedTheme by themeViewModel.selectedThemeId.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                )
        ) {
            BasicToolbar(
                name = "Chat theme",
                color = MaterialTheme.colorScheme.primary,
                icon1 = painterResource(R.drawable.ic_info)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 10.dp)
            ) {
                items(themes) { i ->
                    ThemeItem(
                        context = context,
                        selected = i.id == selectedTheme,
                        id = i.id,
                        name = i.name,
                        author = i.author,
                        iconColor = Color(i.appcolor.toColorInt()),
                        onClick = { themeViewModel.changeTheme(i.id) },
                        onNextClick = { navController.navigate("theme/${i.name}") })
                }
            }
        }
        FloatingActionButton(
            onClick = { /* TO-DO Add new theme */ },
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 40.dp, end = 16.dp),
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