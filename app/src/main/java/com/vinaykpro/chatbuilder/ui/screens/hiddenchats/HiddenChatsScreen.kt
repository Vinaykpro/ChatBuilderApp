package com.vinaykpro.chatbuilder.ui.screens.hiddenchats

import android.app.Application
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.utils.DebounceClickHandler
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HiddenChatsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).chatDao()
    val chatsList: StateFlow<List<ChatEntity>> = dao.getAllHiddenChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Preview
@Composable
fun HiddenChatsScreen(
    navController: NavController = rememberNavController(),
) {
    val model: HiddenChatsViewModel = viewModel()
    val chats by model.chatsList.collectAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
            )
    ) {
        BasicToolbar("Hidden chats", onBackClick = {
            navController.popBackStack()
        })
        if (chats.isEmpty())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chats hidden from home screen will be listed here, no chats were hidden at this time",
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        else
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chats, key = { chat -> chat.chatid }) { chat ->
                    ChatListItem(
                        id = chat.chatid,
                        name = chat.name,
                        lastMessage = chat.lastmsg,
                        lastSeen = chat.lastmsgtime,
                        onClick = {
                            DebounceClickHandler.run { navController.navigate("chat/${chat.chatid}?messageId=${chat.lastOpenedMsgId ?: -1}&hidden=${chat.hidden}") }
                        }
                    )
                }
            }
    }
}