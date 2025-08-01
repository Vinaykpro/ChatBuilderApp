package com.vinaykpro.chatbuilder.ui.screens.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.models.ChatMediaViewModel
import com.vinaykpro.chatbuilder.data.utils.DebounceClickHandler
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.FileListItem
import com.vinaykpro.chatbuilder.ui.components.Input
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
//@Preview
@Composable
fun SharedTransitionScope.ChatProfileScreen(
    navController: NavHostController = rememberNavController(),
    animatedScope: AnimatedVisibilityScope,
    model: ChatMediaViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val statusBarDp = with(density) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    val navBarDp = with(density) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    val heightWithoutToolbar =
        (LocalConfiguration.current.screenHeightDp.dp - statusBarDp - navBarDp - 90.dp)

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    var selectedTabIndex by remember(pagerState.currentPage) { mutableIntStateOf(pagerState.currentPage) }

    val types = setOf(FILETYPE.IMAGE, FILETYPE.VIDEO)
    val nonMediaFileMessages = model.allMediaMessages.filter { msg ->
        model.mediaMap[msg.fileId]?.type !in types
    }

    var refreshKey by remember { mutableIntStateOf(0) }
    var profilePicLoading by remember { mutableStateOf(false) }

    val profilePicPainter = rememberCustomProfileIconPainter(
        chatId = model.currentChat?.chatid,
        refreshKey = refreshKey,
        fallback = R.drawable.user
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                profilePicLoading = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val iconsDir = File(context.filesDir, "icons")
                            if (!iconsDir.exists()) iconsDir.mkdirs()

                            val inputStream = context.contentResolver.openInputStream(uri)
                            val iconFile = File(iconsDir, "icon${model.currentChat?.chatid}.jpg")

                            inputStream?.use { input ->
                                val originalBitmap = BitmapFactory.decodeStream(input)

                                // Crop to square
                                val size = minOf(originalBitmap.width, originalBitmap.height)
                                val xOffset = (originalBitmap.width - size) / 2
                                val yOffset = (originalBitmap.height - size) / 2

                                val squareBitmap = Bitmap.createBitmap(
                                    originalBitmap,
                                    xOffset,
                                    yOffset,
                                    size,
                                    size
                                )

                                FileOutputStream(iconFile).use { output ->
                                    squareBitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
                                }

                                originalBitmap.recycle()
                                squareBitmap.recycle()
                            }
                        } catch (e: Exception) {
                            Log.e("SaveIcon", "Failed to save icon", e)
                        }
                        profilePicLoading = false
                        refreshKey++
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 3.dp, horizontal = 8.dp)
//        ) {
//            IconButton(onClick = {
//                navController.popBackStack()
//            }) {
//                Icon(
//                    painter = painterResource(R.drawable.ic_back),
//                    contentDescription = "back",
//                    tint = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            }
//        }
        BasicToolbar(
            name = "",
            color = MaterialTheme.colorScheme.background,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onBackClick = { navController.popBackStack() }
        )

        // Edit Profile
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = profilePicPainter,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .alpha(if (profilePicLoading) 0f else 1f)
                    .sharedElement(
                        state = rememberSharedContentState(0),
                        animatedVisibilityScope = animatedScope
                    )
                    .graphicsLayer {
                        clip = true
                        shape = CircleShape
                    },
                contentScale = ContentScale.Crop
            )
            Row {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .border(
                            0.5.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { imagePicker.launch("image/*") }
                        .padding(8.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(10.dp))
                        .border(
                            0.5.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            val iconFile =
                                File(context.filesDir, "icons/icon${model.currentChat?.chatid}.jpg")
                            iconFile.delete()
                            refreshKey++
                        }
                        .padding(8.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Input(
                    value = model.currentChat?.name ?: "",
                    nonEmpty = true,
                    onUpdate = {
                        if (model.currentChat != null) {
                            val chat = model.currentChat!!.copy(name = it)
                            model.updateChat(chat)
                            model.currentChat = chat
                        }
                    })
                Input(
                    name = "Status/username",
                    value = model.currentChat?.status ?: "",
                    onUpdate = {
                        val chat = model.currentChat!!.copy(name = it)
                        if (model.currentChat != null) {
                            model.updateChat(model.currentChat!!.copy(status = it))
                            model.currentChat = chat
                        }
                    })
            }

            // Media
//        Text(
//            text = "Media and docs (23)",
//            fontSize = 17.sp,
//            fontWeight = FontWeight(500),
//            color = MaterialTheme.colorScheme.onPrimaryContainer,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 18.dp, horizontal = 18.dp)
//        )

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    "Media (${model.previewMediaMessages.size})",
                    "Docs (${nonMediaFileMessages.size})"
                ).forEachIndexed { index, title ->
                    val isSelected = index == selectedTabIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            lineHeight = 40.sp,
                            color = if (isSelected) LightColorScheme.primary else Color.Gray,
                            fontWeight = if (isSelected) FontWeight(600) else FontWeight(400)
                        )
                        if (isSelected)
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(2.dp)
                                    .background(LightColorScheme.primary)
                            )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heightWithoutToolbar),
            ) { page ->
                when (page) {
                    0 -> {
                        if (model.previewMediaMessages.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                items(model.previewMediaMessages) { m ->
                                    val file = model.mediaMap[m.fileId]
                                    val painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(context)
                                            .data(
                                                File(
                                                    context.getExternalFilesDir(null),
                                                    file?.filename ?: ""
                                                )
                                            )
                                            .build(),
                                        imageLoader = imageLoader
                                    )
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .padding(1.dp)
                                            .clickable {
                                                DebounceClickHandler.run { navController.navigate("mediapreview/${m.fileId}") }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painter,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .sharedElement(
                                                    state = rememberSharedContentState(
                                                        m.fileId ?: ""
                                                    ),
                                                    animatedVisibilityScope = animatedScope
                                                ),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (file?.type == FILETYPE.VIDEO) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_playbtn),
                                                contentDescription = null,
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(35.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No image/video files found",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    1 -> {
                        if (nonMediaFileMessages.isNotEmpty()) {
                            LazyColumn {
                                items(nonMediaFileMessages) { m ->
                                    val file = model.mediaMap[m.fileId]
                                    FileListItem(msg = m, file = file, onClick = {
                                        try {
                                            val filePath = File(
                                                context.getExternalFilesDir(null),
                                                file?.filename ?: ""
                                            )
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.provider",
                                                filePath
                                            )

                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(
                                                    uri,
                                                    context.contentResolver.getType(uri) ?: "*/*"
                                                )
                                                flags =
                                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                            }

                                            try {
                                                context.startActivity(intent)
                                            } catch (e: ActivityNotFoundException) {
                                                Toast.makeText(
                                                    context,
                                                    "No app found to open this file",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "This file doesn't exist in storage",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No files found",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}