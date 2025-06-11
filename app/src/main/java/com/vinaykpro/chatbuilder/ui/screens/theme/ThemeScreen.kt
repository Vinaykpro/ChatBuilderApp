package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar

@Preview
@Composable
fun ThemeScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        BasicToolbar(name = "Chat theme", icon1 = painterResource(R.drawable.ic_info))
        LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer)) {

        }
    }
}