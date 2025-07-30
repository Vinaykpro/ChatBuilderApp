package com.vinaykpro.chatbuilder.ui.screens.mediapreview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.vinaykpro.chatbuilder.R
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

    val activity = LocalContext.current as Activity
    val view = LocalView.current
    val window = activity.window
    val controller = WindowInsetsControllerCompat(window, view)

    SideEffect {
        val window = activity.window
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
    }

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

    LaunchedEffect(detailsVisible) {
        if (detailsVisible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
            controller.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

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
                        .size(coil.size.Size.ORIGINAL)
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
                val msg = chatMediaViewModel.previewMediaMessages[pagerState.currentPage]
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
                        text = (msg.username)
                            ?: "You",
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = (msg.date) + ", " + msg.time,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    chatMediaViewModel.showInChat =
                        chatMediaViewModel.previewMediaMessages[pagerState.currentPage].messageId
                    navController.popBackStack(
                        "chat/${chatMediaViewModel.currentChat?.chatid}",
                        inclusive = false
                    )
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_eye),
                        contentDescription = "Show in chat",
                        tint = Color.White
                    )
                }
                IconButton(onClick = {
                    try {
                        val fileName = chatMediaViewModel.mediaMap[msg.fileId]?.filename ?: ""
                        shareMediaFile(context, File(context.getExternalFilesDir(null), fileName))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Unable to share: $e", Toast.LENGTH_SHORT).show()
                        throw e
                    }
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = "back",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

fun shareMediaFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = getMimeType(file) ?: "*/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share file via"))
}

fun getMimeType(file: File): String? {
    val extension = file.extension.lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}
