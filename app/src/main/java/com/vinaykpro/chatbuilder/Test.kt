package com.vinaykpro.chatbuilder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation

class Test : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                PdfDocBubblePreview(savePdfToDownloads = false)
            }
        }
    }
}

@Preview
@Composable
fun PdfDocBubblePreview(
    message: String = "Hii bro em doing where areeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
    timeText: String = "12:45 pm",
    isOutgoing: Boolean = true,
    maxBubbleInnerWidthPx: Int = 400,
    savePdfToDownloads: Boolean = false
) {
    val context = LocalContext.current
    val bitmap = androidx.compose.runtime.remember {
        createBitmap(595, 842)
    }

    val msgs = listOf(
        "Hii",
        "THis is a shorter message",
        "So thsi is another lonsg siht message but actually a lot longet ahne the prfvieso one",
        "LEts og fot a shorter one",
        "Shorter ig",
        "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English.",
        "The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English.",
        "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout.",
        "The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English.",
        "So thsi is another lonsg siht message but not a lot longet",
        "Shorter",
        "Shit",
        "Goddamn right u are",
        "Men are brave",
        "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English.",
        "Ending with a starter msg",
        "Hii",
    )

    fun drawBubblePage(canvas: Canvas, context: Context) {
        var startY = 10f
        canvas.drawColor(android.graphics.Color.WHITE)
        var ind = 0
        canvas.drawText(
            "Chat with Gubbarao",
            15f,
            20f,
            android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                textSize = 14f * canvas.densityCompat()
                isFakeBoldText = true
            }
        )
        canvas.drawText(
            "Exported by: ChatBuilder",
            350f,
            20f,
            android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                textSize = 14f * canvas.densityCompat()
                isFakeBoldText = true
            }
        )
        while (true) {
            if (startY >= 842) break
            startY += 2f
            val h = drawBubble(
                canvas,
                isOutgoing = ind % 2 == 0,
                isFirst = true,
                name = "Vinay",
                message = msgs[ind],
                maxBubbleInnerWidthPx = maxBubbleInnerWidthPx,
                startY = startY
            )
            if (h > 0) {
                startY += h
                ind++
            }
        }
    }

    // ---- draw to Bitmap (for preview) ----
    androidx.compose.runtime.LaunchedEffect(message, timeText, isOutgoing, maxBubbleInnerWidthPx) {
        val canvas = android.graphics.Canvas(bitmap)
        drawBubblePage(canvas, context)
    }

    // ---- optionally also write a real PDF (Downloads) using the same drawing code ----
    if (savePdfToDownloads) {
        val context = LocalContext.current
        Toast.makeText(context, "Saving", Toast.LENGTH_SHORT).show()
        androidx.compose.runtime.LaunchedEffect(
            message,
            timeText,
            isOutgoing,
            maxBubbleInnerWidthPx
        ) {
            val doc = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = doc.startPage(pageInfo)
            drawBubblePage(page.canvas, context)
            doc.finishPage(page)

            val dir =
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val outFile = java.io.File(dir, "Chat with Vinaykpro.pdf")
            try {
                java.io.FileOutputStream(outFile).use { doc.writeTo(it) }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
            }
            doc.close()
        }
    }

    androidx.compose.foundation.Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    )
}

fun drawDoubleTick(canvas: Canvas, paint: Paint, offsetX: Float = 0f, offsetY: Float = 0f) {
    val shiftY = -4f

    // First tick ✓
    canvas.drawLine(
        4f + offsetX,
        8f + offsetY + shiftY,
        8f + offsetX,
        12f + offsetY + shiftY,
        paint
    )
    canvas.drawLine(
        8f + offsetX,
        12f + offsetY + shiftY,
        16f + offsetX,
        4f + offsetY + shiftY,
        paint
    )

    // Second tick ✓
    canvas.drawLine(
        13f + offsetX,
        12f + offsetY + shiftY,
        21f + offsetX,
        4f + offsetY + shiftY,
        paint
    )
}

fun drawBubble(
    canvas: Canvas,
    isOutgoing: Boolean,
    isFirst: Boolean = true,
    name: String? = null,
    date: String? = null,
    maxBubbleInnerWidthPx: Int,
    startY: Float = 5f,
    message: String = "Hii bro em doing where are",
    timeText: String = "12:45 pm"
): Float {
    val paddingH = 12f
    val paddingV = 10f
    val marginX = 18f

    // paints
    val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isOutgoing) "#DCF8C6".toColorInt() else "#F5F5F5".toColorInt()
        style = Paint.Style.FILL_AND_STROKE
    }
    val namePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        textSize = 20f * canvas.densityCompat()
        isFakeBoldText = true
    }
    val textPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        textSize = 22f * canvas.densityCompat()
    }
    val timePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF666666.toInt()
        textSize = 15f * canvas.densityCompat()
    }
    val tickPaint = Paint().apply {
        this.color = android.graphics.Color.BLUE
        strokeWidth = 1.7f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    fun buildLayout(
        text: String,
        paint: android.text.TextPaint,
        width: Int
    ): android.text.StaticLayout {
        return if (android.os.Build.VERSION.SDK_INT >= 23) {
            android.text.StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            android.text.StaticLayout(
                text, paint, width,
                android.text.Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false
            )
        }
    }

    val innerMax = maxBubbleInnerWidthPx
    val textLayout = buildLayout(message, textPaint, 400)
    var nameHeight = 0
    if (isFirst && name != null && !isOutgoing) {
        nameHeight = 18
    }
    val textHeight = textLayout.height + nameHeight
    val lines = textLayout.lineCount
    val lastLineWidth = textLayout.getLineWidth(lines - 1)
    val textMaxWidth = (0 until lines).maxOf { textLayout.getLineWidth(it) }.toInt()

    val timeWidth = timePaint.measureText(timeText)
    val ticks = if (isOutgoing) "||||||" else ""
    val ticksWidth = tickPaint.measureText(ticks)
    val gapTextTime = 6f
    val gapTicksTime = 1f
    val timeBlockWidth = ticksWidth + gapTicksTime + timeWidth

    val timeFitsSameLine = lastLineWidth + gapTextTime + timeBlockWidth <= innerMax

    // bubble size
    val bubbleInnerWidth = if (timeFitsSameLine) {
        kotlin.math.max(textMaxWidth.toFloat(), lastLineWidth + gapTextTime + timeBlockWidth)
    } else textMaxWidth.toFloat()
    val bubbleWidth = bubbleInnerWidth + paddingH * 2
    val timeLineHeight = (timePaint.textSize * 1.33f)
    val bubbleHeight = if (timeFitsSameLine) {
        textHeight + paddingV * 2
    } else {
        (textHeight + timeLineHeight + paddingV * 2).toInt()
    }

    if (bubbleHeight.toFloat() + startY >= 840 && startY >= 50f) {
        return -1f
    }

    // alignment
    val leftX = if (isOutgoing) (595f - marginX - bubbleWidth) else marginX
    val topY = startY
    val rightX = leftX + bubbleWidth
    val bottomY = topY + bubbleHeight.toFloat()

    // bubble path with tail
    val rect = android.graphics.RectF(leftX, topY, rightX, bottomY)
    val path = android.graphics.Path().apply {
        val radii = if (isOutgoing && isFirst) floatArrayOf(
            20f, 20f,
            0f, 0f,
            20f, 20f,
            20f, 20f
        ) else if (isFirst) floatArrayOf(
            0f, 0f,
            20f, 20f,
            20f, 20f,
            20f, 20f
        ) else floatArrayOf(
            20f, 20f,
            20f, 20f,
            20f, 20f,
            20f, 20f
        )
        addRoundRect(rect, radii, android.graphics.Path.Direction.CW)
        if (isFirst) {
            if (isOutgoing) {
                moveTo(rect.right, rect.top)
                lineTo(rect.right + 14f, rect.top)
                lineTo(rect.right, rect.top + 12f)
                lineTo(rect.right, rect.top)
                close()
            } else {
                moveTo(rect.left, rect.top)
                lineTo(rect.left - 14f, rect.top)
                lineTo(rect.left, rect.top + 12f)
                lineTo(rect.left, rect.top)
                close()
            }
        }
    }
    canvas.drawPath(path, bubblePaint)

    // draw name
    if (isFirst && !isOutgoing && name != null)
        canvas.drawText(name, leftX + paddingH, topY + paddingV + 12, namePaint)

    // draw message text
    canvas.withTranslation(leftX + paddingH, topY + paddingV + nameHeight) {
        textLayout.draw(this)
    }

    // draw ticks + time (bottom-right inside bubble)
    val rightInnerX = rightX - paddingH
    val timeBaseY = if (timeFitsSameLine) {
        topY + paddingV + textLayout.getLineBaseline(lines - 1) + 3f
    } else {
        bottomY - paddingV - 1f
    } + nameHeight

    // ticks
    val ticksX = rightInnerX - ticksWidth + gapTicksTime
    if (isOutgoing) drawDoubleTick(canvas, tickPaint, ticksX, timeBaseY - 9f)

    // time
    val timeX = rightInnerX - timeWidth - ticksWidth
    canvas.drawText(
        timeText,
        timeX,
        if (timeBaseY > startY + bubbleHeight.toFloat()) timeBaseY - nameHeight else timeBaseY,
        timePaint
    )

    return bubbleHeight.toFloat()
}

internal fun Canvas.densityCompat(): Float = 1f



















