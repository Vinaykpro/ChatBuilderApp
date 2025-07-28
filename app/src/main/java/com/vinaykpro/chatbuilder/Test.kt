package com.vinaykpro.chatbuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vinaykpro.chatbuilder.ui.components.ImportChatWidget

class Test : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImportChatWidget(onMediaSave = {})
        }
    }
}


















