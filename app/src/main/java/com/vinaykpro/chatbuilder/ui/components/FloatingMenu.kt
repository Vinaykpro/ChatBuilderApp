package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun FloatingMenu(
    color: Color = MaterialTheme.colorScheme.primary,
    page: Int = 0,
    onClick1: () -> Unit = {},
    onClick2: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val (b1, b2, icon) = when (page) {
        0 -> Triple("New Chat", "Import Chat", R.drawable.ic_newchat)
        else -> Triple("New theme", "Import theme", R.drawable.ic_newtheme)
    }
    // Rotation animation for the main FAB icon
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "Rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Dimmed background with fade in/out
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x65000000))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = false })
            )
        }

        // Animated menu items
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 160.dp, end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { onClick1(); expanded = false },
                    shape = RoundedCornerShape(30.dp),
                    containerColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = b1,
                            tint = if(MaterialTheme.colorScheme == LightColorScheme) LightColorScheme.primary else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            b1,
                            fontSize = 16.sp,
                            color = if(MaterialTheme.colorScheme == LightColorScheme) LightColorScheme.primary else Color.White
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { onClick2(); expanded = false },
                    shape = RoundedCornerShape(30.dp),
                    containerColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_import),
                            contentDescription = b2,
                            tint = if(MaterialTheme.colorScheme == LightColorScheme) LightColorScheme.primary else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            b2,
                            fontSize = 16.sp,
                            color = if(MaterialTheme.colorScheme == LightColorScheme) LightColorScheme.primary else Color.White
                        )
                    }
                }
            }
        }

        // Main FAB with rotation
        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 90.dp, end = 24.dp
                ),
            containerColor = color,
            contentColor = Color.White,
            shape = RoundedCornerShape(100.dp)
        ) {
            Icon(
                painter = painterResource(if(expanded) R.drawable.ic_add else icon),
                contentDescription = "Add chat",
                modifier = Modifier.size(24.dp).rotate(rotation)
            )
        }
    }
}
