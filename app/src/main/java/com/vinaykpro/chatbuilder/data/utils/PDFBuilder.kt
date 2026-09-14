package com.vinaykpro.chatbuilder.data.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.util.Patterns
import com.vinaykpro.chatbuilder.data.local.EmojiUtils
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.local.FileEntity
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.Locale
import java.util.zip.Deflater

/**
 * Lightweight chat-specific PDF writer.
 *
 * Coordinates:
 *   - PDF origin is bottom-left.
 *   - All dimensions are in PDF points.
 *
 * Currently supports:
 *   - Pages
 *   - Selectable text using built-in Helvetica
 *   - Left/right chat bubbles
 *   - Bubble tails
 *   - Reusable double-tick XObject
 *   - Images
 *   - Rounded/clipped images
 *   - Flate-compressed page streams
 *   - JPEG image embedding
 *
 * No external PDF library required.
 */
class PDFBuilder(
    private val width: Float = 595f,
    private val height: Float = 842f
) {

    // ------------------------------------------------------------
    // PDF object storage
    // ------------------------------------------------------------

    private data class PdfObject(
        val number: Int,
        val data: ByteArray
    )

    data class BubbleResult(
        val lastBubbleHeight: Float,
        val remainingLines: List<String>? = null
    )

    private val objects = ArrayList<PdfObject>()
    private val pages = ArrayList<Int>()

    private var currentPage: PageBuilder? = null

    private var nextObjectNumber = 1

    // Reusable resources
    private val emojiCache = HashMap<String, Int>()
    private val emojiWidthCache = HashMap<String, Float>()

    // ------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------

    var lastBubbleHeight = 0f;

    fun addPage(): PDFBuilder {
        finishPage()

        currentPage = PageBuilder(
            width = width,
            height = height
        )

        return this
    }

    fun addText(
        text: String,
        x: Float,
        y: Float,
        size: Float = 14f,
        bold: Boolean = false,
        color: Int = 0x000000
    ): PDFBuilder {

        ensurePage()

        currentPage!!.richText(
            text = text,
            x = x,
            y = y,
            size = size,
            bold = bold,
            color = color
        )

        return this
    }

    fun addBubble(
        message: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        outgoing: Boolean,
        first: Boolean = false,
        name: String? = null,
        time: String? = null,
        textSize: Float = 22f,
        nameSize: Float = 20f,
        timeSize: Float = 16f,
        availableHeight: Float = 0f,
        preWrappedLines: List<String>? = null
    ): BubbleResult {

        ensurePage()
        val page = currentPage!!

        val emojiScale = getEmojiScale(message)

        val paddingH = 14f
        val paddingV = 12f
        val innerMaxWidth = maxWidth - paddingH * 2

        val allLines = preWrappedLines ?: wrapText(message, innerMaxWidth, textSize, emojiScale)

        var nameHeight = 0f
        var nameWidth = 0f
        if (first && !outgoing && !name.isNullOrEmpty()) {
            nameHeight = maxOf(nameSize, nameSize * 1.45f) + 2f
            nameWidth = estimateTextWidth(name, nameSize, 1.45f)
        }

        val textLineHeight = maxOf(textSize, textSize * emojiScale)

        var drawnCount = 0
        var currentAccHeight = paddingV * 2 + nameHeight

        for (idx in allLines.indices) {
            val h = textLineHeight
            if (availableHeight > 0 && currentAccHeight + h > availableHeight) {
                if (drawnCount > 0) break
                else return BubbleResult(0f, allLines) // Nothing fits
            }
            currentAccHeight += h
            drawnCount++
        }

        val lines = allLines.take(drawnCount)
        val remaining = if (drawnCount < allLines.size) allLines.drop(drawnCount) else null
        val isLastPart = remaining == null

        val textWidth =
            if (lines.isEmpty()) 0f else lines.maxOf { estimateTextWidth(it, textSize, emojiScale) }
        val timeWidth = if (isLastPart) estimateTextWidth(time ?: "", timeSize) else 0f

        val lastLine = if (lines.isEmpty()) "" else lines.last()
        val lastLineWidth = estimateTextWidth(lastLine, textSize, emojiScale)
        val timeGap = 10f

        val canFitTimeOnLastLine =
            isLastPart && (lastLineWidth + timeGap + timeWidth <= innerMaxWidth)

        val bubbleInnerWidth = maxOf(
            textWidth,
            nameWidth,
            if (canFitTimeOnLastLine) lastLineWidth + timeGap + timeWidth else timeWidth
        )
        val bubbleWidth = bubbleInnerWidth + paddingH * 2

        val textHeight = lines.size * textLineHeight

        var bubbleHeight = paddingV * 2 + nameHeight + textHeight
        if (isLastPart && !canFitTimeOnLastLine) {
            bubbleHeight += timeSize + 2f
        }

        lastBubbleHeight = bubbleHeight

        val bubbleX = if (outgoing) x - bubbleWidth else x
        val bubbleY = y - bubbleHeight // Bottom of bubble in PDF coords

        val bubbleColor = if (outgoing) 0xDCF8C6 else 0xF5F5F5

        page.roundedBubble(
            x = bubbleX,
            y = bubbleY,
            width = bubbleWidth,
            height = bubbleHeight,
            outgoing = outgoing,
            first = first,
            color = bubbleColor
        )

        var currentTextTopY = y - paddingV - nameHeight

        if (first && !outgoing && !name.isNullOrEmpty()) {
            page.richText(
                text = name,
                x = bubbleX + paddingH,
                y = y - paddingV,
                size = nameSize,
                bold = true,
                color = 0x000000,
                emojiScale = 1.45f
            )
        }

        for (line in lines) {
            page.richText(
                text = line,
                x = bubbleX + paddingH,
                y = currentTextTopY, // Passing the TOP of the line
                size = textSize,
                bold = false,
                color = 0x000000,
                emojiScale = emojiScale,
                lineHeight = textLineHeight
            )
            currentTextTopY -= textLineHeight
        }

        if (isLastPart && !time.isNullOrEmpty()) {
            val timeX = bubbleX + bubbleWidth - paddingH - timeWidth
            page.text(
                text = time,
                x = timeX,
                y = bubbleY + paddingV,
                size = timeSize,
                color = 0x666666,
                bold = false
            )
        }

        return BubbleResult(bubbleHeight, remaining)
    }



    fun addMediaBubble(
        file: FileEntity,
        bitmap: Bitmap?,
        x: Float,
        y: Float,
        maxWidth: Float,
        outgoing: Boolean,
        first: Boolean = false,
        name: String? = null,
        message: String? = null,
        time: String? = null,
        textSize: Float = 22f,
        nameSize: Float = 20f,
        timeSize: Float = 16f,
        availableHeight: Float = 0f
    ): BubbleResult {
        ensurePage()
        val page = currentPage!!

        val paddingH = 14f
        val paddingV = 12f
        val innerMaxWidth = maxWidth - paddingH * 2

        val isImageOrVideo = file.type == FILETYPE.IMAGE || file.type == FILETYPE.VIDEO

        // Dimensions calculation
        var mediaWidth = innerMaxWidth
        var mediaHeight = 60f

        if (isImageOrVideo && file.thumbWidth != null && file.thumbHeight != null) {
            val aspect = file.thumbHeight.toFloat() / file.thumbWidth.toFloat()
            // Fit within innerMaxWidth, but don't exceed original size
            mediaWidth = innerMaxWidth.coerceAtMost(file.thumbWidth.toFloat())
            mediaHeight = mediaWidth * aspect
            
            // If height exceeds limits, scale down proportionally
            if (mediaHeight > 350f) {
                mediaHeight = 350f
                mediaWidth = mediaHeight / aspect
            } else if (mediaHeight < 100f) {
                mediaHeight = 100f
                mediaWidth = mediaHeight / aspect
                // Ensure width doesn't exceed bounds after minimum height scaling
                if (mediaWidth > innerMaxWidth) {
                    mediaWidth = innerMaxWidth
                    mediaHeight = mediaWidth * aspect
                }
            }
        } else if (!isImageOrVideo) {
            mediaWidth = innerMaxWidth
            mediaHeight = 60f
        }

        var nameHeight = 0f
        var nameWidth = 0f
        if (first && !outgoing && !name.isNullOrEmpty()) {
            nameHeight = maxOf(nameSize, nameSize * 1.45f) + 4f
            nameWidth = estimateTextWidth(name, nameSize, 1.45f)
        }

        val emojiScale = if (message != null) getEmojiScale(message) else 1.6f
        val lines = if (!message.isNullOrEmpty()) wrapText(
            message,
            innerMaxWidth,
            textSize,
            emojiScale
        ) else emptyList()
        val textLineHeight = maxOf(textSize, textSize * emojiScale)
        val textHeight = if (lines.isNotEmpty()) lines.size * textLineHeight + 8f else 0f

        val totalMediaContentHeight = mediaHeight + textHeight
        val bubbleHeight = paddingV * 2 + nameHeight + totalMediaContentHeight + timeSize + 4f

        if (availableHeight > 0 && bubbleHeight > availableHeight) {
            return BubbleResult(0f, null) // Doesn't fit
        }

        val bubbleWidth =
            maxOf(mediaWidth, nameWidth, estimateTextWidth(time ?: "", timeSize)) + paddingH * 2
        val bubbleX = if (outgoing) x - bubbleWidth else x
        val bubbleY = y - bubbleHeight

        val bubbleColor = if (outgoing) 0xDCF8C6 else 0xF5F5F5

        page.roundedBubble(
            x = bubbleX,
            y = bubbleY,
            width = bubbleWidth,
            height = bubbleHeight,
            outgoing = outgoing,
            first = first,
            color = bubbleColor
        )

        var currentY = y - paddingV

        // Name
        if (first && !outgoing && !name.isNullOrEmpty()) {
            page.richText(name, bubbleX + paddingH, currentY, nameSize, true, 0x000000, 1.45f)
            currentY -= nameHeight
        }

        // Media
        if (isImageOrVideo && bitmap != null) {
            val imageObject = createJpegImage(bitmap, 70)
            page.roundedImage(
                name = "Img$imageObject",
                objectNumber = imageObject,
                x = bubbleX + paddingH,
                y = currentY - mediaHeight,
                width = mediaWidth,
                height = mediaHeight,
                radius = 8f
            )

            if (file.type == FILETYPE.VIDEO) {
                page.playButton(
                    bubbleX + paddingH + mediaWidth / 2f - 25f,
                    currentY - mediaHeight / 2f - 25f,
                    50f
                )
                if (file.duration.isNotEmpty()) {
                    page.text(
                        file.duration,
                        bubbleX + paddingH + 5f,
                        currentY - mediaHeight + 5f,
                        12f,
                        false,
                        0xFFFFFF
                    )
                }
            }
            currentY -= mediaHeight + 8f
        } else if (!isImageOrVideo) {
            page.fileIcon(bubbleX + paddingH, currentY - 40f, 32f, file.type)
            page.text(
                file.displayname,
                bubbleX + paddingH + 40f,
                currentY - 20f,
                16f,
                false,
                0x000000
            )
            page.text(file.size, bubbleX + paddingH + 40f, currentY - 35f, 12f, false, 0x666666)
            currentY -= mediaHeight + 8f
        }

        // Caption
        for (line in lines) {
            page.richText(
                line,
                bubbleX + paddingH,
                currentY,
                textSize,
                false,
                0x000000,
                emojiScale,
                true,
                textLineHeight
            )
            currentY -= textLineHeight
        }

        // Time
        if (!time.isNullOrEmpty()) {
            val timeX = bubbleX + bubbleWidth - paddingH - estimateTextWidth(time, timeSize)
            page.text(time, timeX, bubbleY + paddingV, timeSize, false, 0x666666)
        }

        return BubbleResult(bubbleHeight, null)
    }

    /**
     * Adds a JPEG image.
     *
     * Bitmap is encoded once and stored as a PDF image XObject.
     */
    fun addImage(
        bitmap: Bitmap,
        x: Float,
        y: Float,
        imageWidth: Float,
        imageHeight: Float,
        quality: Int = 85
    ): PDFBuilder {

        ensurePage()

        val imageObject = createJpegImage(
            bitmap = bitmap,
            quality = quality
        )

        currentPage!!.image(
            name = "Img${imageObject}",
            objectNumber = imageObject,
            x = x,
            y = y,
            width = imageWidth,
            height = imageHeight
        )

        return this
    }

    /**
     * Adds a rounded image.
     *
     * Image itself is still stored only once.
     * Clipping is performed in the page content stream.
     */
    fun addRoundedImage(
        bitmap: Bitmap,
        x: Float,
        y: Float,
        imageWidth: Float,
        imageHeight: Float,
        radius: Float = 16f,
        quality: Int = 85
    ): PDFBuilder {

        ensurePage()

        val imageObject = createJpegImage(
            bitmap = bitmap,
            quality = quality
        )

        currentPage!!.roundedImage(
            name = "Img${imageObject}",
            objectNumber = imageObject,
            x = x,
            y = y,
            width = imageWidth,
            height = imageHeight,
            radius = radius
        )

        return this
    }

    /**
     * Writes the complete PDF.
     */
    fun writeTo(output: OutputStream) {
        val countingOutput = if (output is ByteArrayOutputStream) {
            output
        } else {
            CountingOutputStream(output)
        }

        finishPage()

        if (pages.isEmpty()) {
            addPage()
            finishPage()
        }

        val pageTree = createPageTree()
        val catalog = createObject(
            """
            <<
            /Type /Catalog
            /Pages $pageTree 0 R
            >>
            """.trimIndent().toPdfBytes()
        )

        writePdf(
            output = countingOutput,
            rootObject = catalog
        )
    }

    /**
     * Convenient byte-array version.
     */
    fun build(): ByteArray {
        val output = ByteArrayOutputStream()
        writeTo(output)
        return output.toByteArray()
    }

    // ------------------------------------------------------------
    // Page handling
    // ------------------------------------------------------------

    private fun ensurePage() {
        if (currentPage == null) {
            addPage()
        }
    }

    private fun finishPage() {
        val page = currentPage ?: return

        val contentBytes = page.content.toString().toByteArray(Charsets.US_ASCII)
        val compressed = deflate(contentBytes)

        val contentObject = createObject(
            createStreamObject(
                dictionary = "/Length ${compressed.size} /Filter /FlateDecode",
                data = compressed
            )
        )

        val resources = StringBuilder()

        resources.append("<<")

        if (page.xObjects.isNotEmpty()) {
            resources.append("/XObject <<")

            for ((name, objectNumber) in page.xObjects) {
                resources.append("/$name $objectNumber 0 R ")
            }

            resources.append(">>")
        }

        resources.append(">>")

        val annots = if (page.annots.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("[")
            for (link in page.annots) {
                val rect = link.rect
                sb.append("<< /Type /Annot /Subtype /Link /Rect [${rect.left} ${rect.top} ${rect.right} ${rect.bottom}] /A << /Type /Action /S /URI /URI (${link.url}) >> /Border [0 0 0] >> ")
            }
            sb.append("]")
            sb.toString()
        } else {
            null
        }

        val pageObject = createObject(
            """
            <<
            /Type /Page
            /Parent PAGE_TREE_PLACEHOLDER
            /MediaBox [0 0 $width $height]
            /Resources $resources
            ${if (annots != null) "/Annots $annots" else ""}
            /Contents $contentObject 0 R
            >>
            """.trimIndent().toPdfBytes()
        )

        pages.add(pageObject)

        page.pageObjectNumber = pageObject

        currentPage = null
    }

    // ------------------------------------------------------------
    // PDF resources
    // ------------------------------------------------------------

    // ------------------------------------------------------------
    // Page handling
    // ------------------------------------------------------------

    private fun createJpegImage(
        bitmap: Bitmap,
        quality: Int
    ): Int {

        val jpeg = ByteArrayOutputStream()

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            quality.coerceIn(1, 100),
            jpeg
        )

        val bytes = jpeg.toByteArray()

        val dictionary = """
            /Type /XObject
            /Subtype /Image
            /Width ${bitmap.width}
            /Height ${bitmap.height}
            /ColorSpace /DeviceRGB
            /BitsPerComponent 8
            /Filter /DCTDecode
            /Length ${bytes.size}
        """.trimIndent()

        return createObject(
            createStreamObject(
                dictionary = dictionary,
                data = bytes
            )
        )
    }

    private fun createFlateImage(bitmap: Bitmap): Int {
        val width = bitmap.width
        val height = bitmap.height

        val rgb = ByteArray(width * height * 3)
        val alpha = ByteArray(width * height)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val p = pixels[i]
            rgb[i * 3] = ((p shr 16) and 0xFF).toByte()
            rgb[i * 3 + 1] = ((p shr 8) and 0xFF).toByte()
            rgb[i * 3 + 2] = (p and 0xFF).toByte()
            alpha[i] = ((p shr 24) and 0xFF).toByte()
        }

        val compressedRgb = deflate(rgb)
        val compressedAlpha = deflate(alpha)

        val maskObject = createObject(
            createStreamObject(
                dictionary = """
                    /Type /XObject
                    /Subtype /Image
                    /Width $width
                    /Height $height
                    /ColorSpace /DeviceGray
                    /BitsPerComponent 8
                    /Filter /FlateDecode
                    /Length ${compressedAlpha.size}
                """.trimIndent(),
                data = compressedAlpha
            )
        )

        return createObject(
            createStreamObject(
                dictionary = """
                    /Type /XObject
                    /Subtype /Image
                    /Width $width
                    /Height $height
                    /ColorSpace /DeviceRGB
                    /BitsPerComponent 8
                    /SMask $maskObject 0 R
                    /Filter /FlateDecode
                    /Length ${compressedRgb.size}
                """.trimIndent(),
                data = compressedRgb
            )
        )
    }

    private fun renderEmoji(cluster: String, size: Float): Bitmap {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = size * 2 // Render larger for better quality
        }

        // Use a square bounding box for emoji to prevent squishing
        val side = (size * 2 * 1.1f).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Center the emoji in the square
        val textWidth = paint.measureText(cluster)
        val x = (side - textWidth) / 2f
        val y = side / 2f - (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f

        canvas.drawText(cluster, x, y, paint)
        
        // Store the visual width for layout (normalized to base size)
        emojiWidthCache[cluster] = textWidth / 2f

        return bitmap
    }

    // ------------------------------------------------------------
    // Page implementation
    // ------------------------------------------------------------

    data class LinkInfo(val url: String, val rect: RectF)

    private inner class PageBuilder(
        val width: Float,
        val height: Float
    ) {

        val content = StringBuilder()

        val xObjects = LinkedHashMap<String, Int>()

        val annots = ArrayList<LinkInfo>()

        var pageObjectNumber: Int = 0

        // -------------------------
        // Text
        // -------------------------

        fun richText(
            text: String,
            x: Float,
            y: Float, // This is now the TOP of the line
            size: Float,
            bold: Boolean,
            color: Int,
            emojiScale: Float = 1.6f,
            isLinkable: Boolean = true,
            lineHeight: Float = 0f
        ) {
            val actualLineHeight = if (lineHeight > 0f) lineHeight else maxOf(size, size * emojiScale)
            val lineCenterY = y - actualLineHeight / 2f
            // Baseline for text is slightly below center
            val textBaselineY = lineCenterY - (size * 0.15f)

            if (isLinkable) {
                val matcher = Patterns.WEB_URL.matcher(text)
                var lastEnd = 0
                var currentX = x
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()

                    if (start > lastEnd) {
                        val part = text.substring(lastEnd, start)
                        richText(part, currentX, y, size, bold, color, emojiScale, false, actualLineHeight)
                        currentX += estimateTextWidth(part, size, emojiScale)
                    }

                    val url = text.substring(start, end)
                    val linkWidth = estimateTextWidth(url, size, emojiScale)

                    val rect = RectF(
                        currentX,
                        y - actualLineHeight,
                        currentX + linkWidth,
                        y
                    )
                    annots.add(LinkInfo(url, rect))

                    richText(url, currentX, y, size, bold, 0x0000EE, emojiScale, false, actualLineHeight)
                    currentX += linkWidth
                    lastEnd = end
                }

                if (lastEnd < text.length) {
                    richText(text.substring(lastEnd), currentX, y, size, bold, color, emojiScale, false, actualLineHeight)
                }
                return
            }

            val clusters = EmojiUtils.graphemeClusters(text)
            var currentX = x
            val sb = StringBuilder()

            for (cluster in clusters) {
                if (EmojiUtils.isEmoji(cluster)) {
                    if (sb.isNotEmpty()) {
                        val txt = sb.toString()
                        text(txt, currentX, textBaselineY, size, bold, color)
                        currentX += estimateTextWidth(txt, size, emojiScale)
                        sb.setLength(0)
                    }

                    val actualEmojiSize = size * emojiScale
                    val emojiY = lineCenterY - actualEmojiSize / 2f

                    val objKey = "${cluster}_${actualEmojiSize}"
                    val objNumber = emojiCache.getOrPut(objKey) {
                        val bitmap = renderEmoji(cluster, actualEmojiSize)
                        val obj = createFlateImage(bitmap)
                        bitmap.recycle()
                        obj
                    }

                    image(
                        name = "E$objNumber",
                        objectNumber = objNumber,
                        x = currentX,
                        y = emojiY,
                        width = actualEmojiSize,
                        height = actualEmojiSize
                    )
                    currentX += actualEmojiSize
                } else {
                    sb.append(cluster)
                }
            }

            if (sb.isNotEmpty()) {
                text(sb.toString(), currentX, textBaselineY, size, bold, color)
            }
        }

        fun text(
            text: String,
            x: Float,
            y: Float,
            size: Float,
            bold: Boolean,
            color: Int
        ) {
            val font = if (bold) "/F2" else "/F1"

            val r = ((color shr 16) and 255) / 255f
            val g = ((color shr 8) and 255) / 255f
            val b = (color and 255) / 255f

            content.append(
                String.format(
                    Locale.US,
                    "BT %.4f %.4f %.4f rg /%s %.2f Tf %.2f %.2f Td (%s) Tj ET\n",
                    r,
                    g,
                    b,
                    font,
                    size,
                    x,
                    y,
                    escapePdfString(text)
                )
            )
        }

        // -------------------------
        // Bubble
        // -------------------------

        fun roundedBubble(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            outgoing: Boolean,
            first: Boolean,
            color: Int
        ) {

            val r = ((color shr 16) and 255) / 255f
            val g = ((color shr 8) and 255) / 255f
            val b = (color and 255) / 255f

            val radius = 14f
            val tail = if (first) 10f else 0f

            content.append(
                String.format(
                    Locale.US,
                    "q %.4f %.4f %.4f rg\n",
                    r,
                    g,
                    b
                )
            )

            val trRadius = if (outgoing && first) 0f else radius
            val tlRadius = if (!outgoing && first) 0f else radius
            val brRadius = radius
            val blRadius = radius

            val k = 0.5522848f

            // Start at bottom-left corner (after the curve)
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f m\n",
                    x + blRadius,
                    y
                )
            )

            // Bottom edge to bottom-right
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f l\n",
                    x + width - brRadius,
                    y
                )
            )

            // Bottom-right corner
            if (brRadius > 0) {
                val c = brRadius * k
                content.append(
                    String.format(
                        Locale.US,
                        "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                        x + width - brRadius + c, y,
                        x + width, y + brRadius - c,
                        x + width, y + brRadius
                    )
                )
            } else {
                content.append(String.format(Locale.US, "%.2f %.2f l\n", x + width, y))
            }

            // Right edge to top-right
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f l\n",
                    x + width,
                    y + height - trRadius
                )
            )

            // Top-right corner
            if (trRadius > 0) {
                val c = trRadius * k
                content.append(
                    String.format(
                        Locale.US,
                        "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                        x + width, y + height - trRadius + c,
                        x + width - trRadius + c, y + height,
                        x + width - trRadius, y + height
                    )
                )
            } else {
                content.append(String.format(Locale.US, "%.2f %.2f l\n", x + width, y + height))
            }

            // Top edge to top-left
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f l\n",
                    x + tlRadius,
                    y + height
                )
            )

            // Top-left corner
            if (tlRadius > 0) {
                val c = tlRadius * k
                content.append(
                    String.format(
                        Locale.US,
                        "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                        x + tlRadius - c, y + height,
                        x, y + height - tlRadius + c,
                        x, y + height - tlRadius
                    )
                )
            } else {
                content.append(String.format(Locale.US, "%.2f %.2f l\n", x, y + height))
            }

            // Left edge to bottom-left
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f l\n",
                    x,
                    y + blRadius
                )
            )

            // Bottom-left corner
            if (blRadius > 0) {
                val c = blRadius * k
                content.append(
                    String.format(
                        Locale.US,
                        "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                        x, y + blRadius - c,
                        x + blRadius - c, y,
                        x + blRadius, y
                    )
                )
            } else {
                content.append(String.format(Locale.US, "%.2f %.2f l\n", x, y))
            }

            // Tail
            if (tail > 0f) {
                if (outgoing) {
                    content.append(
                        String.format(
                            Locale.US,
                            "%.2f %.2f m %.2f %.2f l %.2f %.2f l h\n",
                            x + width, y + height, // From the sharp corner
                            x + width + tail, y + height,
                            x + width, y + height - 12f
                        )
                    )
                } else {
                    content.append(
                        String.format(
                            Locale.US,
                            "%.2f %.2f m %.2f %.2f l %.2f %.2f l h\n",
                            x, y + height, // From the sharp corner
                            x - tail, y + height,
                            x, y + height - 12f
                        )
                    )
                }
            }

            content.append("f\nQ\n")
        }

        private fun escapePdfString(text: String): String {
            return text
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
        }


        // -------------------------
        // Image
        // -------------------------

        fun image(
            name: String,
            objectNumber: Int,
            x: Float,
            y: Float,
            width: Float,
            height: Float
        ) {

            xObjects[name] = objectNumber

            content.append(
                String.format(
                    Locale.US,
                    "q %.3f 0 0 %.3f %.2f %.2f cm /%s Do Q\n",
                    width,
                    height,
                    x,
                    y,
                    name
                )
            )
        }

        // -------------------------
        // Rounded image
        // -------------------------

        fun roundedImage(
            name: String,
            objectNumber: Int,
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            radius: Float
        ) {

            xObjects[name] = objectNumber

            val k = 0.5522848f
            val c = radius * k

            content.append("q\n")

            // Rounded clipping path
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f m\n",
                    x + radius,
                    y
                )
            )

            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f l\n",
                    x + width - radius,
                    y
                )
            )

            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                    x + width - radius + c,
                    y,
                    x + width,
                    y + radius - c,
                    x + width,
                    y + radius
                )
            )

            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f l\n",
                    x + width,
                    y + height - radius
                )
            )

            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                    x + width,
                    y + height - radius + c,
                    x + width - radius + c,
                    y + height,
                    x + width - radius,
                    y + height
                )
            )

            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f l\n",
                    x + radius,
                    y + height
                )
            )

            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                    x + radius - c,
                    y + height,
                    x,
                    y + height - radius + c,
                    x,
                    y + height - radius
                )
            )

            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f l\n",
                    x,
                    y + radius
                )
            )

            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                    x,
                    y + radius - c,
                    x + radius - c,
                    y,
                    x + radius,
                    y
                )
            )

            content.append("W n\n")

            content.append(
                String.format(
                    Locale.US,
                    "q %.3f 0 0 %.3f %.2f %.2f cm /%s Do Q\n",
                    width,
                    height,
                    x,
                    y,
                    name
                )
            )

            content.append("Q\n")
        }

        fun playButton(x: Float, y: Float, size: Float) {
            val centerX = x + size / 2f
            val centerY = y + size / 2f
            val radius = size / 2.2f

            content.append("q\n")
            content.append("1 1 1 rg\n") // White fill
            content.append("1 1 1 RG\n") // White stroke
            content.append("1 w\n")

            // Circle
            val k = 0.5522848f
            val c = radius * k
            content.append(String.format(Locale.US, "%.2f %.2f m\n", centerX + radius, centerY))
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                    centerX + radius,
                    centerY + c,
                    centerX + c,
                    centerY + radius,
                    centerX,
                    centerY + radius
                )
            )
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                    centerX - c,
                    centerY + radius,
                    centerX - radius,
                    centerY + c,
                    centerX - radius,
                    centerY
                )
            )
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                    centerX - radius,
                    centerY - c,
                    centerX - c,
                    centerY - radius,
                    centerX,
                    centerY - radius
                )
            )
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f %.2f %.2f %.2f %.2f c\n",
                    centerX + c,
                    centerY - radius,
                    centerX + radius,
                    centerY - c,
                    centerX + radius,
                    centerY
                )
            )
            content.append("f\n")

            // Triangle
            val triSize = radius * 0.8f
            val x1 = centerX - triSize * 0.3f
            val y1 = centerY + triSize * 0.5f
            val x2 = centerX - triSize * 0.3f
            val y2 = centerY - triSize * 0.5f
            val x3 = centerX + triSize * 0.6f
            val y3 = centerY

            content.append("0 0 0 rg\n") // Black triangle
            content.append(
                String.format(
                    Locale.US,
                    "%.2f %.2f m %.2f %.2f l %.2f %.2f l h f\n",
                    x1,
                    y1,
                    x2,
                    y2,
                    x3,
                    y3
                )
            )

            content.append("Q\n")
        }

        fun fileIcon(x: Float, y: Float, size: Float, type: Int) {
            content.append("q\n")
            content.append("0.7 0.7 0.7 rg\n") // Gray

            val w = size * 0.8f
            val h = size
            val r = 2f

            // Simple document shape
            content.append(String.format(Locale.US, "%.2f %.2f m\n", x + r, y))
            content.append(String.format(Locale.US, "%.2f %.2f l\n", x + w - r, y))
            content.append(String.format(Locale.US, "%.2f %.2f l\n", x + w, y + r))
            content.append(String.format(Locale.US, "%.2f %.2f l\n", x + w, y + h - r))
            content.append(String.format(Locale.US, "%.2f %.2f l\n", x + r, y + h))
            content.append(String.format(Locale.US, "%.2f %.2f l\n", x, y + h - r))
            content.append(String.format(Locale.US, "%.2f %.2f l\n", x, y + r))
            content.append("h f\n")

            content.append("Q\n")
        }
    }

    // ------------------------------------------------------------
    // Page tree
    // ------------------------------------------------------------

    private fun createPageTree(): Int {

        val kids = pages.joinToString(" ") {
            "$it 0 R"
        }

        val pageTree = createObject(
            """
            <<
            /Type /Pages
            /Kids [$kids]
            /Count ${pages.size}
            >>
            """.trimIndent().toPdfBytes()
        )

        /*
         * Patch each page's parent.
         */
        for (i in objects.indices) {

            val obj = objects[i]

            val text = obj.data.toString(Charsets.ISO_8859_1)

            if (text.contains("PAGE_TREE_PLACEHOLDER")) {

                val patched = text
                    .replace(
                        "PAGE_TREE_PLACEHOLDER",
                        "$pageTree 0 R"
                    )
                    .toByteArray(Charsets.ISO_8859_1)

                objects[i] = obj.copy(
                    data = patched
                )
            }
        }

        return pageTree
    }

    // ------------------------------------------------------------
    // PDF object creation
    // ------------------------------------------------------------

    private fun createObject(data: ByteArray): Int {

        val number = nextObjectNumber++

        objects.add(
            PdfObject(
                number = number,
                data = data
            )
        )

        return number
    }

    private fun createStreamObject(
        dictionary: String,
        data: ByteArray
    ): ByteArray {

        val output = ByteArrayOutputStream()

        output.write(
            """
            <<
            $dictionary
            >>
            stream

            """.trimIndent().toByteArray(Charsets.US_ASCII)
        )

        output.write(data)

        output.write(
            """

            endstream
            """.trimIndent().toByteArray(Charsets.US_ASCII)
        )

        return output.toByteArray()
    }

    // ------------------------------------------------------------
    // Final PDF writer
    // ------------------------------------------------------------

    private fun writePdf(
        output: OutputStream,
        rootObject: Int
    ) {

        /*
         * We need fonts too.
         *
         * Helvetica and Helvetica-Bold are PDF standard fonts,
         * therefore no TTF is embedded.
         */
        val fontRegular = createObject(
            """
            <<
            /Type /Font
            /Subtype /Type1
            /BaseFont /Helvetica
            /Encoding /WinAnsiEncoding
            >>
            """.trimIndent().toPdfBytes()
        )

        val fontBold = createObject(
            """
            <<
            /Type /Font
            /Subtype /Type1
            /BaseFont /Helvetica-Bold
            /Encoding /WinAnsiEncoding
            >>
            """.trimIndent().toPdfBytes()
        )

        /*
         * Every page needs F1/F2.
         *
         * We patch page resources here.
         */
        for (i in objects.indices) {

            val obj = objects[i]

            val text = obj.data.toString(Charsets.ISO_8859_1)

            if (
                text.contains("/Resources <<") &&
                !text.contains("/Font <<")
            ) {

                val patched = text.replace(
                    "/Resources <<",
                    "/Resources << /Font << /F1 $fontRegular 0 R /F2 $fontBold 0 R >> "
                )

                objects[i] = obj.copy(
                    data = patched.toByteArray(Charsets.ISO_8859_1)
                )
            }
        }

        output.write("%PDF-1.7\n".toByteArray(Charsets.US_ASCII))
        output.write(
            byteArrayOf(
                0x25,
                0xE2.toByte(),
                0xE3.toByte(),
                0xCF.toByte(),
                0xD3.toByte(),
                0x0A
            )
        )

        val offsets = HashMap<Int, Long>()

        for (obj in objects) {

            offsets[obj.number] = currentPosition(output)

            output.write(
                "${obj.number} 0 obj\n".toByteArray(Charsets.US_ASCII)
            )

            output.write(obj.data)

            output.write(
                "\nendobj\n".toByteArray(Charsets.US_ASCII)
            )
        }

        val xrefPosition = currentPosition(output)

        output.write(
            "xref\n".toByteArray(Charsets.US_ASCII)
        )

        output.write(
            "0 ${nextObjectNumber}\n".toByteArray(Charsets.US_ASCII)
        )

        output.write(
            "0000000000 65535 f \n".toByteArray(Charsets.US_ASCII)
        )

        for (number in 1 until nextObjectNumber) {

            val offset = offsets[number] ?: 0L

            output.write(
                String.format(
                    Locale.US,
                    "%010d 00000 n \n",
                    offset
                ).toByteArray(Charsets.US_ASCII)
            )
        }

        output.write(
            """
            trailer
            <<
            /Size $nextObjectNumber
            /Root $rootObject 0 R
            >>
            startxref
            $xrefPosition
            %%EOF
            """.trimIndent().toByteArray(Charsets.US_ASCII)
        )
    }

    // ------------------------------------------------------------
    // Text wrapping
    // ------------------------------------------------------------

    private fun getEmojiInfo(text: String): Pair<Int, Boolean> {
        val clusters = EmojiUtils.graphemeClusters(text)
        var emojiCount = 0
        var onlyEmojis = true
        for (cluster in clusters) {
            if (EmojiUtils.isEmoji(cluster)) {
                emojiCount++
            } else if (cluster.trim().isNotEmpty()) {
                onlyEmojis = false
            }
        }
        return emojiCount to (onlyEmojis && emojiCount > 0)
    }

    private fun getEmojiScale(text: String): Float {
        val (count, onlyEmojis) = getEmojiInfo(text)
        return if (onlyEmojis) {
            when (count) {
                1 -> 3.8f
                2 -> 2.8f
                3 -> 2.2f
                else -> 1.45f
            }
        } else {
            1.45f
        }
    }

    private fun wrapText(
        text: String,
        maxWidth: Float,
        fontSize: Float
    ): List<String> {
        return wrapText(text, maxWidth, fontSize, 1.5f)
    }

    private fun wrapText(
        text: String,
        maxWidth: Float,
        fontSize: Float,
        emojiScale: Float
    ): List<String> {

        if (text.isEmpty()) {
            return listOf("")
        }

        val result = ArrayList<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            val words = paragraph.split(Regex("\\s+"))
            var currentLine = StringBuilder()
            var currentLineWidth = 0f

            for (word in words) {
                val wordClusters = EmojiUtils.graphemeClusters(word)
                var wordWidth = 0f
                for (cluster in wordClusters) {
                    wordWidth += estimateClusterWidth(cluster, fontSize, emojiScale)
                }

                val spaceWidth = estimateClusterWidth(" ", fontSize, emojiScale)
                val needsSpace = currentLine.isNotEmpty()
                val totalWordWidth = if (needsSpace) spaceWidth + wordWidth else wordWidth

                if (currentLineWidth + totalWordWidth <= maxWidth) {
                    if (needsSpace) {
                        currentLine.append(" ")
                        currentLineWidth += spaceWidth
                    }
                    currentLine.append(word)
                    currentLineWidth += wordWidth
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine.toString())
                        currentLine = StringBuilder()
                        currentLineWidth = 0f
                    }

                    // If a single word is wider than maxWidth, split it
                    if (wordWidth > maxWidth) {
                        for (cluster in wordClusters) {
                            val cw = estimateClusterWidth(cluster, fontSize, emojiScale)
                            if (currentLineWidth + cw > maxWidth) {
                                result.add(currentLine.toString())
                                currentLine = StringBuilder()
                                currentLineWidth = 0f
                            }
                            currentLine.append(cluster)
                            currentLineWidth += cw
                        }
                    } else {
                        currentLine.append(word)
                        currentLineWidth += wordWidth
                    }
                }
            }

            if (currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
            }
        }

        return result
    }

    private fun estimateClusterWidth(
        cluster: String,
        size: Float
    ): Float {
        return estimateClusterWidth(cluster, size, 1.5f)
    }

    private fun estimateClusterWidth(
        cluster: String,
        size: Float,
        emojiScale: Float
    ): Float {
        return if (EmojiUtils.isEmoji(cluster)) {
            // Match the square rendering in richText
            size * emojiScale
        } else {
            var width = 0f
            for (char in cluster) {
                val factor = when (char) {
                    ' ', '.', ',', '!', ';', ':', '\'', '"', '(', ')', '[', ']', '{', '}', '-', '_', '+', '=', '<', '>', '/', '\\', '|' -> 0.28f
                    'i', 'l', 'I', '1' -> 0.23f
                    'f', 't', 'j', 'r' -> 0.33f
                    'v', 'x', 'y', 'z', 'c', 's', 'k', 'J', 'L' -> 0.50f
                    'a', 'b', 'd', 'e', 'g', 'h', 'n', 'o', 'p', 'q', 'u' -> 0.56f
                    '0', '2', '3', '4', '5', '6', '7', '8', '9' -> 0.56f
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'K', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'X', 'Y', 'Z' -> 0.67f
                    'm', 'w' -> 0.75f
                    'M' -> 0.83f
                    'W' -> 0.94f
                    else -> 0.56f
                }
                width += size * factor
            }
            width
        }
    }

    private fun estimateTextWidth(
        text: String,
        size: Float
    ): Float {
        return estimateTextWidth(text, size, 1.5f)
    }

    private fun estimateTextWidth(
        text: String,
        size: Float,
        emojiScale: Float
    ): Float {
        var total = 0f
        val clusters = EmojiUtils.graphemeClusters(text)
        for (c in clusters) {
            total += estimateClusterWidth(c, size, emojiScale)
        }
        return total
    }

    // ------------------------------------------------------------
    // Compression
    // ------------------------------------------------------------

    private fun deflate(
        data: ByteArray
    ): ByteArray {

        val deflater = Deflater(
            Deflater.DEFAULT_COMPRESSION,
            false
        )

        deflater.setInput(data)
        deflater.finish()

        val output = ByteArrayOutputStream()

        val buffer = ByteArray(8192)

        while (!deflater.finished()) {

            val count = deflater.deflate(buffer)

            if (count > 0) {
                output.write(buffer, 0, count)
            }
        }

        deflater.end()

        return output.toByteArray()
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private fun currentPosition(
        output: OutputStream
    ): Long {
        return when (output) {
            is ByteArrayOutputStream -> output.size().toLong()
            is CountingOutputStream -> output.bytesWritten
            else -> throw IllegalArgumentException(
                "PDFBuilder requires ByteArrayOutputStream or CountingOutputStream"
            )
        }
    }

    private fun escapePdfString(
        value: String
    ): String {

        /*
         * Built-in Helvetica is WinAnsi.
         *
         * Characters outside WinAnsi are currently replaced.
         * Unicode/emoji font handling will be added separately.
         */
        val bytes = value.toByteArray(
            Charset.forName("windows-1252")
        )

        val safe = String(
            bytes,
            Charset.forName("windows-1252")
        )

        return safe
            .replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("\r", "")
            .replace("\n", "\\n")
    }

    private fun String.toPdfBytes(): ByteArray {
        return toByteArray(Charsets.ISO_8859_1)
    }

    private class CountingOutputStream(val out: OutputStream) : OutputStream() {
        var bytesWritten = 0L

        override fun write(b: Int) {
            out.write(b)
            bytesWritten++
        }

        override fun write(b: ByteArray) {
            out.write(b)
            bytesWritten += b.size.toLong()
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            out.write(b, off, len)
            bytesWritten += len.toLong()
        }

        override fun flush() {
            out.flush()
        }

        override fun close() {
            out.close()
        }
    }

}