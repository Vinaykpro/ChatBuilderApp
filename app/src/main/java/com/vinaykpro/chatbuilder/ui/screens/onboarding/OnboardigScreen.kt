package com.vinaykpro.chatbuilder.ui.screens.onboarding

import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    navController: NavController = rememberNavController(),
    prefs: SharedPreferences,
    isDarkTheme: Boolean = false
) {
    val pageCount = 4
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    val headings = listOf(
        Triple("Import in seconds", "No manual extraction required", R.drawable.helps1),
        Triple("Beautiful Statistics", "Discover Insights & fun moments", R.drawable.helps2),
        Triple("Export to PDF/HTML", "Easy for sharing & printing", R.drawable.helps3),
        Triple(
            "Best Chat Features",
            "View, search, navigate & Play chats",
            R.drawable.helps4
        ),
    )

    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF121212)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = if (isDarkTheme) Color(44, 44, 44, 1) else Color(0xFFBDBDBD)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .background(backgroundColor)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = if (pagerState.currentPage < pageCount - 1) "Skip" else "",
                color = activeColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        if (pagerState.currentPage < pageCount - 1)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pageCount - 1)
                            }
                    }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 20.dp)
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = headings[page].first,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight(900),
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = headings[page].second,
                    color = textColor.copy(0.5f),
                    fontWeight = FontWeight(900),
                    fontSize = 15.sp
                )
                Image(
                    painter = painterResource(headings[page].third),
                    contentDescription = "Help screen 1",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until pageCount) {
                    val pageOffset =
                        (pagerState.currentPage - i) + pagerState.currentPageOffsetFraction
                    val absoluteOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                    val size = 15.dp - (8.dp * absoluteOffset)

                    val color = androidx.compose.ui.graphics.lerp(
                        activeColor,
                        inactiveColor,
                        absoluteOffset
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(size)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pageCount - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            prefs.edit { putBoolean("isOnboarding", false) }
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = activeColor)
            ) {
                Text(
                    text = if (pagerState.currentPage == pageCount - 1) "Get Started" else "Next",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
