package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.data.utils.DebounceClickHandler
import com.vinaykpro.chatbuilder.ui.components.BannerAdView
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ThemeItem

@Composable
fun ThemeScreen(
    themeViewModel: ThemeViewModel,
    navController: NavController = rememberNavController()
) {
    val themes by themeViewModel.themes.collectAsState()
    val selectedTheme by themeViewModel.selectedThemeId.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BasicToolbar(
                name = "Chat theme",
                color = MaterialTheme.colorScheme.primary,
                icon1 = painterResource(R.drawable.ic_info),
                onBackClick = {
                    navController.popBackStack()
                }
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 10.dp)
            ) {
                items(themes) { i ->
                    ThemeItem(
                        selected = i.id == selectedTheme,
                        id = i.id,
                        name = i.name,
                        author = i.author,
                        iconColor = Color(i.appcolor.toColorInt()),
                        onClick = { themeViewModel.changeTheme(i.id) },
                        onNextClick = {
                            DebounceClickHandler.run { navController.navigate("theme/${i.name}") }
                        }
                    )
                }
            }
            BannerAdView(adId = "ca-app-pub-2813592783630195/8283590134")
        }
        //BannerAdView(adId = "ca-app-pub-2813592783630195/8283590134")
    }
}