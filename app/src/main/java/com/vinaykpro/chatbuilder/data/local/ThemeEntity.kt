package com.vinaykpro.chatbuilder.data.local

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val icon: String? = null,
    val name: String = "Default theme",
    val author: String = "Vinaykpro",
    val appcolor: String = "#FF1283A6",
    val appcolordark: String = "#FF323232",

    val headerstyle: String = "",

    val bodystyle: String = "",

    val messagebarstyle: String = "",
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class HeaderStyle(
    val color_navbar: String = "#FF1283A6",
    val color_navicons: String = "#FFffffff",
    val color_text_primary: String = "#FFffffff",
    val color_text_secondary: String = "#FFBDBDBD",
    val color_navbar_dark: String = "#FF222222",
    val color_navicons_dark: String = "#FFffffff",
    val color_text_primary_dark: String = "#FFffffff",
    val color_text_secondary_dark: String = "#FF999999",
    val showbackbtn: Boolean = true,
    val backbtn_size: Int = 24,
    val backbtn_gap: Int = 2,
    val backbtn_icon: String? = null,
    val showprofilepic: Boolean = true,
    val profilepic_size: Int = 40,
    val profilepic_gap_sides: Int = 2,
    val profilepic_icon: String? = null,
    val showstatus: Boolean = true,
    val threedots_gap: Int = 2,
    val threedots_icon: String? = null,
    val actionicons_order: List<Int> = listOf(1,2,3),
    val is_icon1_visible: Boolean = true,
    val is_icon2_visible: Boolean = true,
    val is_icon3_visible: Boolean = false,
    val icon1: String? = null,
    val icon2: String? = null,
    val icon3: String? = null,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class BodyStyle(
    val bubble_style: Int = 1,
    val bubble_radius: Float = 8f,
    val bubble_tip_radius: Float = 8f,
    val color_chatbackground: String = "#FFC5B6A1",
    val color_senderbubble: String = "#FFADFFF5",
    val color_receiverbubble: String = "#FFffffff",
    val color_datetext: String = "#FF818181",
    val color_text_primary: String = "#FF000000",
    val color_text_secondary: String = "#FF5E5E5E",
    val color_chatbackground_dark: String = "#FF000000",
    val color_senderbubble_dark: String = "#FF1A7585",
    val color_receiverbubble_dark: String = "#FF323232",
    val color_datetext_dark: String = "#FFdddddd",
    val color_text_primary_dark: String = "#FFffffff",
    val color_text_secondary_dark: String = "#FF999999",
    val show_time: Boolean = true,
    val use12hr: Boolean = true,
    val showticks: Boolean = true,
    val ticks_icon: String? = null,
    val showreceiverpic: Boolean = false
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MessageBarStyle(
    val color_widgetbackground: String = "#00000000",
    val color_barbackground: String = "#FFffffff",
    val color_outerbutton: String = "#FF1283A6",
    val color_rightinnerbutton: String = "#FF1283A6",
    val color_leftinnerbutton: String = "#00000000",
    val color_outerbutton_icon: String = "#FFffffff",
    val color_rightinnerbutton_icon: String = "#FFffffff",
    val color_leftinnerbutton_icon: String = "#FF000000",
    val color_icons: String = "#FF000000",
    val color_inputtext: String = "#FF000000",
    val color_hinttext: String = "#FF888888",
    val color_widgetbackground_dark: String = "#00000000",
    val color_barbackground_dark: String = "#FF323232",
    val color_outerbutton_dark: String = "#FF1283A6",
    val color_rightinnerbutton_dark: String = "#FF1283A6",
    val color_leftinnerbutton_dark: String = "#00000000",
    val color_outerbutton_icon_dark: String = "#FFffffff",
    val color_rightinnerbutton_icon_dark: String = "#FFffffff",
    val color_leftinnerbutton_icon_dark: String = "#FF000000",
    val color_icons_dark: String = "#FF000000",
    val color_inputtext_dark: String = "#FFffffff",
    val color_hinttext_dark: String = "#FF888888",
    val showleftinnerbutton: Boolean = true,
    val showrightinnerbutton: Boolean = false,
    val showouterbutton: Boolean = true,
    val leftinnerbutton_icon: String? = null,
    val rightinnerbutton_icon: String? = null,
    val outerbutton_icon: String? = null,
    val actionicons_order: List<Int> = listOf(1,2,3),
    val is_icon1_visible: Boolean = true,
    val is_icon2_visible: Boolean = true,
    val is_icon3_visible: Boolean = false,
    val icon1: String? = null,
    val icon2: String? = null,
    val icon3: String? = null,
)

