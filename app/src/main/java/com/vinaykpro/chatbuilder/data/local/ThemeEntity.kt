package com.vinaykpro.chatbuilder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val icon: String? = null,
    val name: String = "Default theme",
    val author: String = "Vinaykpro",
    val appcolor: String = "#FF3DBFDC",
    val appcolordark: String = "#FF323232",

    val headerstyle: String = "",
    val headerstyledark: String = "",

    val bodystyle: String = "",
    val bodystyedark: String = "",

    val messagestyle: String = "",
    val messagestyledark: String = ""
)

@Serializable
data class HeaderStyle(
    val color_navbar: String = "#FF3DBFDC",
    val color_text_primary: String = "#FFffffff",
    val color_text_secondary: String = "#FFBDBDBD",
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
    val show_time: Boolean = true,
    val use12hr: Boolean = true,
    val showticks: Boolean = true,
    val ticks_icon: String? = null,
    val showreceiverpic: Boolean = false
)

@Serializable
data class MessageBarStyle(
    val color_widgetbackground: String,
    val color_barbackground: String,
    val color_outerbutton: String,
    val color_rightinnerbutton: String,
    val color_leftinnerbutton: String,
    val color_inputtext: String,
    val color_hinttext: String,
    val showleftinnerbutton: Boolean,
    val showrightinnerbutton: Boolean,
    val showouterbutton: Boolean,
    val leftinnerbutton_icon: String? = null,
    val rightinnerbutton_icon: String? = null,
    val outerbutton_icon: String? = null
)


