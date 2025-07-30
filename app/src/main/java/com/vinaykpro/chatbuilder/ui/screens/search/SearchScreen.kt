package com.vinaykpro.chatbuilder.ui.screens.search

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.GLobalSearchResultInfo
import com.vinaykpro.chatbuilder.ui.components.GlobalSearchItem
import com.vinaykpro.chatbuilder.ui.components.SearchBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val chatDao = AppDatabase.getInstance(application).chatDao()
    private val messageDao = AppDatabase.getInstance(application).messageDao()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchResultItems = MutableStateFlow<List<GLobalSearchResultInfo>>(emptyList())
    val searchResultItems: StateFlow<List<GLobalSearchResultInfo>> = _searchResultItems

    private val limit = 20
    private var offset = 0
    private var chats: Map<Int, ChatEntity>? = null

    private var searchTerm: String? = null

    fun search(text: String) {
        searchTerm = text
        offset = 0
        _isLoading.value = true
        _searchResultItems.update { emptyList() }
        loadResults(text)
    }

    fun nextPage() {
        if (_isLoading.value) return
        offset++
        if (searchTerm != null) {
            loadResults(searchTerm!!)
        }
    }

    fun loadResults(text: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (chats == null) chats = chatDao.getAllChatEntities().associateBy { it.chatid }
                val result = messageDao.search("%$text%", limit = limit, offset = offset)
                //Log.i("vkpro", "results length : ${result.size}")
                var temp = mutableListOf<GLobalSearchResultInfo>()
                result.forEach {
                    temp.add(
                        GLobalSearchResultInfo(
                            chatId = it.chatid,
                            messageId = it.messageId,
                            chatName = chats?.get(it.chatid)?.name ?: "",
                            senderName = it.username ?: "",
                            message = it.message ?: "",
                            date = it.date ?: "",
                            searchTerm = text
                        )
                    )
                }
                if (temp.isNotEmpty()) {
                    _searchResultItems.update { it + temp }
                }
                _isLoading.value = false
            }
        }
    }


}

@Preview
@Composable
fun SearchScreen(
    navController: NavHostController = rememberNavController(),
    isDark: Boolean = false
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = LocalContext.current as Activity
    val useDarkIcons = MaterialTheme.colorScheme.primary.luminance() > 0.5f
    SideEffect {
        val window = activity.window
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = useDarkIcons
    }

    val model: SearchViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )

    val listState = rememberLazyListState()

    val items by model.searchResultItems.collectAsState(initial = emptyList())
    val isLoading by model.isLoading.collectAsState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            Pair(totalItems, lastVisibleItem)
        }.collect { (totalItems, lastVisibleItem) ->
            if (totalItems > 0 && lastVisibleItem >= totalItems - 10) {
                model.nextPage()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        )
        SearchBar(
            backgroundColor = MaterialTheme.colorScheme.primary,
            color = Color.White,
            hint = "Search everywhere",
            autoSearch = true,
            onSearch = {
                if (it.trim().isNotEmpty()) model.search(it)
            },
            onExit = {
                navController.popBackStack()
            }
        )

        if (isLoading || items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading)
                    CircularProgressIndicator()
                else if (items.isEmpty())
                    Text(
                        text = "No results found",
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(items) { item ->
                    GlobalSearchItem(
                        chatName = item.chatName,
                        senderName = item.senderName,
                        message = item.message,
                        date = item.date,
                        searchTerm = item.searchTerm,
                        onClick = {
                            navController.navigate("chat/${item.chatId}?messageId=${item.messageId}")
                        }
                    )
                }
            }
        }
    }
}