package com.vinaykpro.chatbuilder.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vinaykpro.chatbuilder.ui.navigation.Routes


@Composable
fun SplashScreen(navController: NavController? = null, isDarkTheme: MutableState<Boolean> = mutableStateOf(false)) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Asset("introt1.json")
        )
        val animatable = rememberLottieAnimatable()

        LaunchedEffect(composition) {
            composition?.let {
                animatable.animate(it, iterations = 1)
                // ⏱ After animation ends, navigate
                navController?.navigate(Routes.Home) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            }
        }
        LottieAnimation(
            composition = composition,
            progress = { animatable.progress },
            modifier = Modifier.width(250.dp).align(Alignment.Center)
        )
    }
}