package com.vinaykpro.chatbuilder.ui.screens.mediapreview

import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.models.ChatMediaViewModel
import com.vinaykpro.chatbuilder.ui.components.VideoPlayer
import com.vinaykpro.chatbuilder.ui.components.ZoomableImage
import java.io.File

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MediaPreviewScreen(
    fileid: Int?,
    navController: NavHostController,
    scope: AnimatedContentScope,
    chatMediaViewModel: ChatMediaViewModel
) {
    val context = LocalContext.current

    val startIndex = chatMediaViewModel.previewMediaMessages.indexOfFirst { it.fileId == fileid }
        .coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        initialPageOffsetFraction = 0f,
        pageCount = { chatMediaViewModel.previewMediaMessages.size }
    )
    Log.i(
        "vkpro",
        "fileId: $fileid, filename: ${chatMediaViewModel.mediaMap[startIndex]}, index: $startIndex, size: ${chatMediaViewModel.previewMediaMessages.size}, ${chatMediaViewModel.mediaMap.size}"
    )

    var isZoomed by remember { mutableStateOf(false) }
    var detailsVisible by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !isZoomed
        ) { page ->
            val item = chatMediaViewModel.previewMediaMessages[page]
            val media = chatMediaViewModel.mediaMap[item.fileId]

            val sharedModifier = if (item.fileId == fileid) {
                Modifier.sharedElement(
                    state = rememberSharedContentState(fileid ?: ""),
                    animatedVisibilityScope = scope
                )
            } else Modifier

            if (media!!.type == FILETYPE.VIDEO) {
                VideoPlayer(
                    file = File(context.getExternalFilesDir(null), media.filename),
                    text = item.message,
                    sharedModifier = sharedModifier,
                    onVisibilityChange = { detailsVisible = it }
                )
            } else {
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(File(context.getExternalFilesDir(null), media.filename))
                        .build()
                )

                ZoomableImage(
                    painter = painter,
                    text = item.message,
                    onScaleChanged = { scale ->
                        isZoomed = scale > 1.1f
                    },
                    sharedModifier = sharedModifier,
                    modifier = Modifier.fillMaxSize(),
                    showInfo = detailsVisible,
                    onVisibilityChange = { detailsVisible = it }
                )
            }
        }
        AnimatedVisibility(
            visible = detailsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x773D3D3D))
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .padding(vertical = 5.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = (chatMediaViewModel.previewMediaMessages[pagerState.currentPage].username)
                            ?: "You",
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = (chatMediaViewModel.previewMediaMessages[pagerState.currentPage].date) + ", " + chatMediaViewModel.previewMediaMessages[pagerState.currentPage].time,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}