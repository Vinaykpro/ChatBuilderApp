package com.vinaykpro.chatbuilder.ui.screens.profile

import android.util.Log
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.models.ChatMediaViewModel
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.FileListItem
import com.vinaykpro.chatbuilder.ui.components.Input
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomIconPainter
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
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
    val theme = LocalThemeEntity.current
    val customProfilePicPainter =
        rememberCustomIconPainter(theme.id, "ic_profile.png", 0, R.drawable.user)

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

    val profilePicPainter = rememberCustomProfileIconPainter(
        chatId = model.currentChat?.chatid ?: 0,
        refreshKey = refreshKey,
        fallback = customProfilePicPainter
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val themeDir = File(context.filesDir, "icons")
                            if (!themeDir.exists()) themeDir.mkdirs()
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val iconFile = File(themeDir, "icon${model.currentChat?.chatid}.jpg")

                            inputStream?.use { input ->
                                FileOutputStream(iconFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("SaveIcon", "Failed to save icon", e)
                        }
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
                .fillMaxWidth(0.65f)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = profilePicPainter,
                contentDescription = null,
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .size(100.dp)
                    .sharedElement(
                        state = rememberSharedContentState(0),
                        animatedVisibilityScope = animatedScope
                    ),
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
                        .clickable { }
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
            Input(
                value = "Vinaykpro",
                onUpdate = {

                })
            Input(
                name = "Status/username",
                value = "online",
                onUpdate = {

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
            listOf("Media", "Docs").forEachIndexed { index, title ->
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when (selectedTabIndex) {
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
                                            navController.navigate("mediapreview/${m.fileId}")
                                        }
                                ) {
                                    Image(
                                        painter = painter,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .sharedElement(
                                                state = rememberSharedContentState(m.fileId ?: ""),
                                                animatedVisibilityScope = animatedScope
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
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
                                FileListItem(msg = m, file = model.mediaMap[m.fileId])
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