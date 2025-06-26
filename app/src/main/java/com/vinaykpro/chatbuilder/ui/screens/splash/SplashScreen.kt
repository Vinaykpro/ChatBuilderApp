package com.vinaykpro.chatbuilder.ui.screens.splash


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.DarkColorScheme
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme


@Composable
fun SplashScreen(navController: NavController? = null, isDarkTheme: Boolean = false) {
    val colors = if(isDarkTheme) DarkColorScheme else LightColorScheme
    Box(modifier = Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
        Icon(painter = painterResource(R.drawable.logo), contentDescription = null, tint = colors.onPrimaryContainer)
//        val composition by rememberLottieComposition(
//            LottieCompositionSpec.Asset("introt1.json")
//        )
//        val animatable = rememberLottieAnimatable()
//
//        LaunchedEffect(composition) {
//            composition?.let {
//                animatable.animate(it, iterations = 1)
//                // ⏱ After animation ends, navigate
//                navController?.navigate(Routes.Home) {
//                    popUpTo(Routes.Splash) { inclusive = true }
//                }
//            }
//        }
//        LottieAnimation(
//            composition = composition,
//            progress = { animatable.progress },
//            modifier = Modifier.width(250.dp).align(Alignment.Center)
//        )
    }
}