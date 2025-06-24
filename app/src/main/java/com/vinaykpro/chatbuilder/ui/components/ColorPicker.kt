package com.vinaykpro.chatbuilder.ui.components


import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.vinaykpro.chatbuilder.R

@Preview(showBackground = true)
@Composable
fun ColorPicker(
    initialColor: Color = Color.Red,
    onColorPicked: (Color) -> Unit = {
    },
) {
    var state by remember {
        mutableStateOf(ColorPickerState(0f, 1f, 1f, 1f).apply {
            updateFromColor(initialColor)
        })
    }

    var hexInputText by remember(state) { mutableStateOf(state.toHex()) }
    Box(modifier = Modifier.fillMaxSize().background(Color(0x77000000))
        .padding(bottom =  WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
        Column(
            modifier = Modifier.clip(RoundedCornerShape(20.dp, 20.dp))
                .background(MaterialTheme.colorScheme.background)
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.padding(top = 8.dp, bottom = 12.dp, start = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Color picker",
                    fontSize = 22.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }


            SVBox(
                hue = state.hue,
                saturation = state.sat,
                value = state.value
            ) { s, v ->
                state = state.copy(sat = s, value = v)
                hexInputText = state.toHex()
            }

            Spacer(Modifier.height(12.dp))

            // Hue Slider
            HueSlider(hue = state.hue) {
                state = state.copy(hue = it)
                hexInputText = state.toHex()
            }

            Spacer(Modifier.height(12.dp))

            // Alpha Slider
            AlphaSlider(color = state.toColor().copy(alpha = 1f), alpha = state.alpha) {
                state = state.copy(alpha = it)
                hexInputText = state.toHex()
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(0.85f)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Preview",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    Box(
                        Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .background(initialColor, RoundedCornerShape(8.dp))
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Image",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    Box(
                        Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_image),
                            contentDescription = "Pick Image",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(13.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(
                    text = "Hex code",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(top = 5.dp, bottom = 8.dp)
                )
                // Hex input (2-way binding)
                OutlinedTextField(
                    value = hexInputText,
                    onValueChange = {
                        hexInputText = it.uppercase()
                        runCatching {
                            val parsed = Color(hexInputText.toColorInt())
                            state.updateFromColor(parsed)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy),
                            contentDescription = "Copy",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                                .clickable {}
                        )
                    }
                )
            }
        }
    }
}



@SuppressLint("UseKtx", "UnusedBoxWithConstraintsScope")
@Composable
fun SVBox(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (Float, Float) -> Unit
) {
    val touchOffset = Offset(saturation, 1f - value)

    val hueColor = remember(hue) { Color.hsv(hue, 1f, 1f) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapAndDragGestures { offset ->
                    val x = offset.x.coerceIn(0f, size.width.toFloat())
                    val y = offset.y.coerceIn(0f, size.height.toFloat())
                    val s = (x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (y / size.height).coerceIn(0f, 1f)
                    onChange(s, v)
                }
            }
    ) {
        drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))

        drawCircle(
            color = Color.White,
            center = Offset(
                x = touchOffset.x * size.width,
                y = touchOffset.y * size.height
            ),
            radius = 10.dp.toPx(),
            style = Stroke(2.dp.toPx())
        )
    }
}

@Composable
fun HueSlider(hue: Float, onHueChanged: (Float) -> Unit) {
    val hueGradient = remember {
        Brush.horizontalGradient(
            (0..360 step 15).map { angle -> Color.hsv(angle.toFloat(), 1f, 1f) }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(30.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(hueGradient)
    ) {
        Slider(
            value = hue,
            onValueChange = onHueChanged,
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .align(Alignment.Center)
        )
    }

}

suspend fun PointerInputScope.detectTapAndDragGestures(
    onTouch: (Offset) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown()
        onTouch(down.position) // handle tap

        drag(down.id) { change ->
            change.consume()
            onTouch(change.position)
        }
    }
}


@Composable
fun AlphaSlider(
    color: Color,
    alpha: Float,
    onAlphaChanged: (Float) -> Unit
) {
    val gradient = Brush.horizontalGradient(
        listOf(color.copy(alpha = 0f), color.copy(alpha = 1f))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(30.dp)
            .clip(RoundedCornerShape(30.dp))
            .checkerboardBackground()
            .background(gradient)
    ) {
        Slider(
            value = alpha,
            onValueChange = onAlphaChanged,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Gray,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .align(Alignment.Center)
        )
    }
}


fun Modifier.checkerboardBackground(
    color1: Color = Color.LightGray,
    color2: Color = Color.White,
    squareSize: Dp = 8.dp
): Modifier = this.then(
    Modifier.drawBehind {
        val sizePx = squareSize.toPx()
        val rows = (size.height / sizePx).toInt() + 1
        val cols = (size.width / sizePx).toInt() + 1

        for (row in 0..rows) {
            for (col in 0..cols) {
                val isLight = (row + col) % 2 == 0
                drawRect(
                    color = if (isLight) color1 else color2,
                    topLeft = Offset(col * sizePx, row * sizePx),
                    size = Size(sizePx, sizePx)
                )
            }
        }
    }
)

data class ColorPickerState(
    var hue: Float,
    var sat: Float,
    var value: Float,
    var alpha: Float
) {
    fun toColor(): Color = Color.hsv(hue, sat, value, alpha)

    fun toHex(): String = "#%08X".format(toColor().toArgb())

    fun updateFromColor(color: Color) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hue = hsv[0]
        sat = hsv[1]
        value = hsv[2]
        alpha = color.alpha
    }
}
