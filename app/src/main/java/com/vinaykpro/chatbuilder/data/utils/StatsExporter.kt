package com.vinaykpro.chatbuilder.data.utils

import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.vinaykpro.chatbuilder.ui.screens.statistics.StatisticsViewModel
import java.util.Locale

object StatsExporter {
    data class BarGraphItem(
        val name: String,
        val color: String,   // "#4CAF50"
        val count: Int
    )

    fun export(model: StatisticsViewModel): String {
        return buildString {
            with(StatsExporter) {
                beginHtml("Chat statistics of ${model.chatDetails?.name ?: "User"}")
                addCSS()
                addScript()
                beginBody()

                addHeaderProfile(
                    model.chatDetails?.name ?: "User",
                    totalMessages = String.format(Locale.US, "%,d", model.messageCount),
                    media = String.format(Locale.US, "%,d", model.mediaCount),
                    longestStreak = String.format(Locale.US, "%,d", model.streak),
                    activeDays = String.format(Locale.US, "%,d", model.activeDays),
                    emojis = String.format(Locale.US, "%,d", model.emojiCount),
                    deletedMessages = String.format(Locale.US, "%,d", model.deletedCount)
                )

                addLineChart(
                    title = "Overall chat growth",
                    chartId = 1,
                    points = model.overallChatGrowth,
                )
                addLineChart(
                    title = "Messages per day of week",
                    chartId = 2,
                    points = model.messageCountByWeekDay,
                )
                addLineChart(
                    title = "Messages per hour of day",
                    chartId = 3,
                    points = model.messageCountByHour,
                    labels = listOf(
                        "12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
                        "12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"
                    )
                )
                addBarGraphLayout(
                    heading = "Messages by user",
                    items = model.messageCountByUser.map {
                        BarGraphItem(
                            it.name,
                            String.format("#%06X", 0xFFFFFF and it.color.toArgb()),
                            it.count
                        )
                    },
                    total = model.messageCount
                )
                addBarGraphLayout(
                    heading = "Media by user",
                    items = model.mediaCountByUser.map {
                        BarGraphItem(
                            it.name,
                            String.format("#%06X", 0xFFFFFF and it.color.toArgb()),
                            it.count
                        )
                    },
                    total = model.mediaCount
                )
                addBarGraphLayout(
                    heading = "Emojis by user",
                    items = model.emojiCountByUser.map {
                        BarGraphItem(
                            it.name,
                            String.format("#%06X", 0xFFFFFF and it.color.toArgb()),
                            it.count
                        )
                    },
                    total = model.emojiCount
                )
                addBarGraphLayout(
                    heading = "Conversations started by user",
                    items = model.conversationStartsByUser.map {
                        BarGraphItem(
                            it.name,
                            String.format("#%06X", 0xFFFFFF and it.color.toArgb()),
                            it.count
                        )
                    },
                    total = model.totalConversations
                )
                addBarGraphLayout(
                    heading = "Longest conversations",
                    items = model.longestConversatins.map {
                        BarGraphItem(
                            "${it.startedByUserName} on ${it.startDate}",
                            String.format("#%06X", 0xFFFFFF and it.color.toArgb()),
                            it.messageCount
                        )
                    },
                    total = if (model.longestConversatins.isNotEmpty()) model.longestConversatins[0].messageCount else model.totalConversations
                )
                addBarGraphLayout(
                    heading = "Peak activity days",
                    items = model.topDatesWithMessages.map {
                        BarGraphItem(
                            it.date,
                            String.format("#%06X", 0xFFFFFF and it.color.toArgb()),
                            it.messageCount
                        )
                    },
                    total = if (model.topDatesWithMessages.isNotEmpty()) model.topDatesWithMessages[0].messageCount else model.activeDays
                )
                addKeywordCloud(
                    title = "Top keywords by user",
                    keywords = model.mostUsedWords,
                    isEmoji = false
                )
                addKeywordCloud(
                    title = "Top emoji's by user",
                    keywords = model.mostUsedEmojis,
                    isEmoji = true
                )
                addUserWordsCard(
                    title = "Most used emojis by user",
                    users = model.mostUsedEmojisByUser,
                    color = "#1976D2",
                    isEmoji = true,
                )
                addUserWordsCard(
                    title = "Most used words by user",
                    users = model.mostUsedWordsByUser,
                    color = "#1976D2",
                    isEmoji = false,
                )
                closeBody()
            }
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun StringBuilder.beginHtml(
        title: String = "Chat Statistics"
    ) {
        append(
            """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta
    name="viewport"
    content="width=device-width, initial-scale=1.0">
<title>${escapeHtml(title)}</title>
<head>

"""
        )

    }

    fun StringBuilder.beginBody() {

        append(
            """
</head>
<body>
"""
        )

    }

    fun StringBuilder.closeBody() {

        append(
            """
</body>
</html>
"""
        )

    }

    fun StringBuilder.addCSS() {
        append(
            """
            <style>
            body{
                margin:0;
                background:#f4f4f4;
                font-family:Arial, Helvetica, sans-serif;
            }

            .card{
                background:#fff;
                border-radius:18px;
                box-shadow:0 3px 12px rgba(0,0,0,.12);
                padding:18px;
                margin:18px;
            }

            .card-title{
                font-size:20px;
                font-weight:600;
                margin-bottom:12px;
            }

            .bar-user{
                display:flex;
                align-items:center;
                margin:14px 0;
            }

            .avatar{
                width:42px;
                height:42px;
                border-radius:50%;
                color:white;
                font-weight:bold;
                font-size:18px;

                display:flex;
                align-items:center;
                justify-content:center;

                flex-shrink:0;
            }

            .bar-content{
                flex:1;
                margin:0 14px;
            }

            .bar-top{
                display:flex;
                align-items:center;
                gap:8px;
                margin-bottom:10px;
            }

            .user-name{
                font-size:15px;
                font-weight:600;
            }

            .user-percent{
                font-size:13px;
                font-weight:500;
            }

            .progress{
                width:100%;
                height:8px;
                background:rgba(0,0,0,.08);
                border-radius:100px;
                overflow:hidden;
            }

            .progress-fill{
                height:100%;
                border-radius:100px;
            }

            .user-count{
                min-width:50px;
                text-align:right;
                font-size:15px;
                font-weight:600;
            }

            .line-chart{

                width:100%;
                height:230px;
                display:block;

                touch-action:none;

            }

            .chart-selected{

                text-align:center;

                font-size:15px;

                font-weight:500;

                margin-top:10px;

                color:#444;

                min-height:20px;

            }

            .header-card{

                background:white;
                border-radius:22px;
                box-shadow:0 5px 18px rgba(0,0,0,.12);

                margin:18px;
                padding:28px 20px;

                text-align:center;

            }

            .header-avatar{

                width:78px;
                height:78px;

                border-radius:50%;

                background:#0DA4B5;
                color:white;

                margin:auto;

                display:flex;
                align-items:center;
                justify-content:center;

                font-size:34px;
                font-weight:bold;

            }

            .header-title{

                margin-top:18px;

                font-size:28px;
                font-weight:700;

                color:#222;

            }

            .header-name{

                margin-top:8px;
                margin-bottom:24px;

                font-size:19px;
                font-weight:600;

                color:#0DA4B5;

            }

            .header-grid{

                display:grid;

                grid-template-columns:repeat(3,1fr);

                gap:14px;

            }

            .header-stat{

                background:#f8f9fa;

                border-radius:16px;

                padding:16px 10px;

            }

            .header-stat-value{

                font-size:22px;

                font-weight:700;

                color:#0DA4B5;

            }

            .header-stat-label{

                margin-top:6px;

                font-size:13px;

                color:#666;

                line-height:1.3;

            }

            .header-footer{

                margin-top:22px;

                color:#777;

                font-size:14px;

            }

            .header-footer a{

                color:#0DA4B5;

                text-decoration:none;

                font-weight:600;

            }
            .header-footer a:hover{
                text-decoration:underline;
            }
            
            .keyword-cloud{

                display:flex;

                flex-wrap:wrap;

                gap:10px;

                padding:12px;

            }

            .keyword-item{

                border-radius:18px;

                padding:10px 16px;

                text-align:center;

                min-width:70px;

            }

            .keyword-text{

                font-size:16px;

                font-weight:600;

                color:#1F6FB5;

            }

            .keyword-emoji{

                font-size:30px;

                line-height:1;

            }

            .keyword-count{

                margin-top:2px;

                font-size:12px;

                color:#777;

            }
            
            .user-words{

                display:flex;

                align-items:flex-start;

                padding:16px;

            }

            .rank-box{

                width:42px;

                height:42px;

                border-radius:14px;

                display:flex;

                align-items:center;

                justify-content:center;

                font-size:20px;

                font-weight:bold;

                flex-shrink:0;

            }

            .user-content{

                flex:1;

                margin-left:12px;

            }

            .user-name{

                font-size:17px;

                font-weight:600;

                margin-bottom:12px;

            }

            .user-tags{

                display:flex;

                flex-wrap:wrap;

                gap:8px;

            }

            .word-chip{

                border-radius:18px;

                padding:10px 16px;

                text-align:center;

            }

            .word{

                color:#1F6FB5;

                font-weight:600;

                font-size:16px;

            }

            .word-count{

                margin-top:2px;

                font-size:12px;

                color:#777;

            }

            .emoji-item{

                width:60px;

                text-align:center;

            }

            .emoji{

                font-size:28px;

                line-height:1;

            }

            .emoji-count{

                margin-top:3px;

                font-size:13px;

                color:#555;

            }

            @media (max-width:480px){

                .header-grid{

                    grid-template-columns:repeat(2,1fr);

                }
            }
            </style>
        """.trimIndent()
        )
    }

    fun StringBuilder.addHeaderProfile(
        chatName: String,
        totalMessages: String,
        media: String,
        longestStreak: String,
        activeDays: String,
        emojis: String,
        deletedMessages: String
    ) {

        append(
            """
<div class="header-card">

    <div class="header-avatar">
        ${escapeHtml(chatName.first().uppercase())}
    </div>

    <div class="header-title">
        Chat Statistics Report
    </div>

    <div class="header-name">
        ${escapeHtml(chatName)}
    </div>

    <div class="header-grid">

        <div class="header-stat">
            <div class="header-stat-value">$totalMessages</div>
            <div class="header-stat-label">Messages</div>
        </div>

        <div class="header-stat">
            <div class="header-stat-value">$media</div>
            <div class="header-stat-label">Media</div>
        </div>

        <div class="header-stat">
            <div class="header-stat-value">$longestStreak</div>
            <div class="header-stat-label">Longest Streak</div>
        </div>

        <div class="header-stat">
            <div class="header-stat-value">$activeDays</div>
            <div class="header-stat-label">Active Days</div>
        </div>

        <div class="header-stat">
            <div class="header-stat-value">$emojis</div>
            <div class="header-stat-label">Emojis Used</div>
        </div>

        <div class="header-stat">
            <div class="header-stat-value">$deletedMessages</div>
            <div class="header-stat-label">Deleted Messages</div>
        </div>

    </div>

    <div class="header-footer">
        Report generated with
        <a href="https://play.google.com/store/apps/details?id=com.vinaykpro.chatbuilder" target="_blank">ChatBuilder App ↗</a>
    </div>

</div>
"""
        )

    }

    fun StringBuilder.addBarGraphLayout(
        heading: String,
        items: List<BarGraphItem>,
        total: Int
    ) {

        append(
            """
        <div class="card">
            <div class="card-title">$heading</div>
    """.trimIndent()
        )

        items.forEach {

            val percent =
                if (total == 0) 0 else ((it.count.toFloat() / total) * 100).toInt()

            append(
                """
            
            <div class="bar-user">
            
                <div class="avatar" style="background:${it.color};">
                    ${escapeHtml(it.name.firstOrNull()?.uppercase() ?: "?")}
                </div>

                <div class="bar-content">

                    <div class="bar-top">

                        <span class="user-name">${escapeHtml(it.name)}</span>

                        <span class="user-percent"
                              style="color:${it.color};">
                            $percent%
                        </span>

                    </div>

                    <div class="progress">

                        <div class="progress-fill"
                             style="
                                width:$percent%;
                                background:${it.color};
                             ">
                        </div>

                    </div>

                </div>

                <div class="user-count">
                    ${it.count}
                </div>

            </div>

            """.trimIndent()
            )
        }

        append("</div>")
    }

    data class ChartPoint(
        val label: String,
        val value: Int
    )

    fun StringBuilder.addLineChart(
        title: String,
        chartId: Int,
        points: List<Pair<String, Int>>,
        labels: List<String>? = null
    ) {

        val id = "chart${chartId}"

        append(
            """
<div class="card">

    <div class="card-title">
        ${escapeHtml(title)}
    </div>

    <canvas
        id="chart$id"
        class="line-chart"
        height="230">
    </canvas>

    <div
        id="chart${id}_selected"
        class="chart-selected">
    </div>

</div>

<script>

drawLineChart(
    "chart$id",
    ${
                points.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) {
                    """
            {
                label:"${
                        escapeHtml(it.first)
                    }",
                value:${it.second}
            }
            """.trimIndent()
                }
            },
    ${
                if (labels == null) "null"
                else
                    labels.joinToString(
                        prefix = "[",
                        postfix = "]"
                    ) {
                        "\"${escapeHtml(it)}\""
                    }
            }
);

</script>

"""
        )

    }

    fun StringBuilder.addKeywordCloud(
        title: String,
        keywords: List<Pair<String, Int>>,
        isEmoji: Boolean = false
    ) {

        val colors = listOf(
            "#E7F8FC",
            "#EAF1FF",
            "#F2ECFF",
            "#FFF0E8",
            "#EAF6EA",
            "#FFF7E7"
        )

        val max = keywords.maxOfOrNull { it.second } ?: 1

        append(
            """
<div class="card">

    <div class="card-title">
        ${escapeHtml(title)}
    </div>

    <div class="keyword-cloud">
"""
        )

        keywords.forEachIndexed { index, item ->

            val alpha = 0.45f + (item.second / max.toFloat()) * 0.35f

            val color = colors[index % colors.size]

            append(
                """
        <div
            class="keyword-item"
            style="background:${hexToRgba(color, alpha)}">

            <div class="${if (isEmoji) "keyword-emoji" else "keyword-text"}">
                ${escapeHtml(item.first)}
            </div>

            <div class="keyword-count">
                ${item.second}
            </div>

        </div>
"""
            )
        }

        append(
            """
    </div>

</div>
"""
        )
    }

    fun StringBuilder.addUserWordsCard(
        title: String,
        users: List<Pair<String, List<Pair<String, Int>>>>,
        color: String,
        isEmoji: Boolean = false
    ) {

        val chipColors = listOf(
            "#E7F8FC",
            "#EAF1FF",
            "#F2ECFF",
            "#FFF0E8",
            "#EAF6EA",
            "#FFF7E7"
        )

        append(
            """
<div class="card">

    <div class="card-title">
        ${escapeHtml(title)}
    </div>

"""
        )

        users.forEachIndexed { rank, user ->

            val max = user.second.maxOfOrNull { it.second } ?: 1

            append(
                """
<div class="user-words">

    <div class="rank-box"
         style="
            color:$color;
            background:${hexToRgba(color, .18f)};
         ">
        ${rank + 1}
    </div>

    <div class="user-content">

        <div class="user-name">
            ${escapeHtml(user.first)}
        </div>

        <div class="user-tags">
"""
            )

            user.second.forEachIndexed { index, pair ->

                if (isEmoji) {

                    append(
                        """
<div class="emoji-item">

    <div class="emoji">
        ${escapeHtml(pair.first)}
    </div>

    <div class="emoji-count">
        ${pair.second}
    </div>

</div>
"""
                    )

                } else {

                    val alpha =
                        0.45f +
                                (pair.second / max.toFloat()) * .35f

                    append(
                        """
<div class="word-chip"
style="background:${hexToRgba(chipColors[index % chipColors.size], alpha)}">

    <div class="word">
        ${escapeHtml(pair.first)}
    </div>

    <div class="word-count">
        ${pair.second}
    </div>

</div>
"""
                    )

                }

            }

            append(
                """
        </div>

    </div>

</div>
"""
            )

        }

        append(
            """
</div>
"""
        )

    }

    fun StringBuilder.addScript() {

        append(
            """
        <script>
        function drawLineChart(id, data, customLabels) {

    const canvas = document.getElementById(id);
    const ctx = canvas.getContext("2d");

    const selectedLabel =
        document.getElementById(id + "_selected");

    let selected = -1;

    function resize() {

        canvas.width = canvas.clientWidth;
        canvas.height = 230;

        draw();

    }

    window.addEventListener("resize", resize);

    function shortNumber(v){

        if(v>=1000000)
            return (v/1000000).toFixed(1)+"M";

        if(v>=1000)
            return (v/1000).toFixed(1)+"K";

        return v.toString();

    }

    function draw(){

        ctx.clearRect(0,0,canvas.width,canvas.height);

        const left = 38;
        const top = 10;
        const bottom = 35;

        const chartHeight =
            canvas.height-top-bottom;

        const chartWidth =
            canvas.width-left-10;

        const max =
            Math.max(...data.map(x=>x.value));

        const spacing =
            chartWidth/(data.length-1);

        const pts=[];

        for(let i=0;i<data.length;i++){

            const x=
                left+i*spacing;

            const y=
                top+
                chartHeight-
                (data[i].value/max)*chartHeight;

            pts.push({x,y});

        }

        //-----------------------------
        // Grid
        //-----------------------------

        ctx.strokeStyle="#e6e6e6";
        ctx.lineWidth=1;

        for(let i=0;i<5;i++){

            const y=
                top+
                chartHeight/4*i;

            ctx.beginPath();
            ctx.moveTo(left,y);
            ctx.lineTo(canvas.width,y);
            ctx.stroke();

        }

        //-----------------------------
        // Left labels
        //-----------------------------

        ctx.fillStyle="#888";
        ctx.font="11px Arial";
        ctx.textAlign="left";

        for(let i=0;i<5;i++){

            const value=
                max-(max/4*i);

            const y=
                top+
                chartHeight/4*i+4;

            ctx.fillText(
                shortNumber(Math.round(value)),
                0,
                y
            );

        }

        //-----------------------------
        // Curve
        //-----------------------------

        ctx.beginPath();

        ctx.moveTo(
            pts[0].x,
            pts[0].y
        );

        for(let i=1;i<pts.length;i++){

            const p1=pts[i-1];
            const p2=pts[i];

            ctx.bezierCurveTo(

                p1.x+spacing/2,
                p1.y,

                p2.x-spacing/2,
                p2.y,

                p2.x,
                p2.y

            );

        }

        //-----------------------------
        // Gradient Fill
        //-----------------------------

        const fill=new Path2D();

        fill.moveTo(
            pts[0].x,
            pts[0].y
        );

        for(let i=1;i<pts.length;i++){

            const p1=pts[i-1];
            const p2=pts[i];

            fill.bezierCurveTo(

                p1.x+spacing/2,
                p1.y,

                p2.x-spacing/2,
                p2.y,

                p2.x,
                p2.y

            );

        }

        fill.lineTo(
            pts[pts.length-1].x,
            top+chartHeight
        );

        fill.lineTo(
            pts[0].x,
            top+chartHeight
        );

        fill.closePath();

        const gradient=
            ctx.createLinearGradient(
                0,
                top,
                0,
                top+chartHeight
            );

        gradient.addColorStop(
            0,
            "rgba(13,164,181,.35)"
        );

        gradient.addColorStop(
            1,
            "rgba(13,164,181,0)"
        );

        ctx.fillStyle=gradient;
        ctx.fill(fill);

        //-----------------------------
        // Blue Line
        //-----------------------------

        ctx.strokeStyle="#0DA4B5";
        ctx.lineWidth=4;
        ctx.lineCap="round";
        ctx.stroke();

        //-----------------------------
        // Bottom Labels
        //-----------------------------

        ctx.fillStyle="#777";
        ctx.textAlign="center";

        for(let i=0;i<data.length;i++){

            const text = (customLabels
    ? customLabels[i]
    : (data[i].label.length > 3
        ? data[i].label.substring(0, 3)
        : data[i].label));

            ctx.fillText(
                text,
                pts[i].x,
                canvas.height-10
            );

        }

        //-----------------------------
        // Dots
        //-----------------------------

        for(let i=0;i<pts.length;i++){

            const p=pts[i];

            ctx.beginPath();

            ctx.arc(
                p.x,
                p.y,
                data.length>12?3:4,
                0,
                Math.PI*2
            );

            ctx.fillStyle="white";
            ctx.fill();

            ctx.lineWidth=
                data.length>12?2:3;

            ctx.strokeStyle="#0DA4B5";
            ctx.stroke();

        }

        canvas.points=pts;
        canvas.chartTop=top;
        canvas.chartBottom=top+chartHeight;

    }

    resize();
    
        //----------------------------------
    // Draw selection
    //----------------------------------

    function drawSelection(){

        if(selected==-1)
            return;

        const p=canvas.points[selected];

        ctx.save();

        ctx.setLineDash([8,8]);

        ctx.beginPath();

        ctx.moveTo(
            p.x,
            canvas.chartTop
        );

        ctx.lineTo(
            p.x,
            canvas.chartBottom
        );

        ctx.strokeStyle="rgba(0,0,0,.45)";
        ctx.lineWidth=2;
        ctx.stroke();

        ctx.restore();

        // outer circle

        ctx.beginPath();

        ctx.arc(
            p.x,
            p.y,
            8,
            0,
            Math.PI*2
        );

        ctx.fillStyle="#0DA4B5";
        ctx.fill();

        // white center

        ctx.beginPath();

        ctx.arc(
            p.x,
            p.y,
            4,
            0,
            Math.PI*2
        );

        ctx.fillStyle="white";
        ctx.fill();

    }

    //----------------------------------
    // Redraw with selection
    //----------------------------------

    const originalDraw=draw;

    draw=function(){

        originalDraw();

        drawSelection();

    };

    //----------------------------------
    // Mouse
    //----------------------------------

    function selectPoint(clientX){

        const rect=
            canvas.getBoundingClientRect();

        const x=
            clientX-rect.left;

        let nearest=-1;
        let dist=999999;

        for(let i=0;i<canvas.points.length;i++){

            const d=Math.abs(
                canvas.points[i].x-x
            );

            if(d<dist){

                dist=d;
                nearest=i;

            }

        }

        if(nearest==selected){

            selected=-1;
            selectedLabel.innerHTML="";

        }else{

            selected=nearest;

            selectedLabel.innerHTML=
                "<b>"+
                data[selected].label+
                "</b> • "+
                data[selected].value+
                " messages";

        }

        draw();

    }

    canvas.addEventListener("click",e=>{

        selectPoint(e.clientX);

    });

    //----------------------------------
    // Touch support
    //----------------------------------

    canvas.addEventListener("touchstart",e=>{

        e.preventDefault();

        selectPoint(
            e.touches[0].clientX
        );

    });

}
</script>
    """.trimIndent()
        )
    }

}

fun hexToRgba(
    hex: String,
    alpha: Float
): String {

    val color = Color.parseColor(hex)

    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)

    return "rgba($r,$g,$b,$alpha)"
}