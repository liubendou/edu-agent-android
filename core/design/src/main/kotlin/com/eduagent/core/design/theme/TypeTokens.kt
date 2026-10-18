package com.eduagent.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 无衬线字阶 + 大留白行高（契约 §13 D25）。
 *
 * 只用系统无衬线族（不引入外部字体资源，红线 M6：动效/呈现不得依赖外部字体）。
 * 行高统一偏大（约 1.6~1.8 倍），承担「大留白」的呼吸感。
 */
object TypeTokens {
    /** 唯一字体族：系统无衬线。 */
    val SansSerif: FontFamily = FontFamily.SansSerif

    /** 正文基准字号。 */
    val BodySize = 16.sp

    /** 正文行高（大留白）。 */
    val BodyLineHeight = 27.sp

    /** 最小可点字号（标签 / chip）。 */
    val LabelSize = 14.sp

    /** 页标题。 */
    val TitleSize = 22.sp

    /** 大标题。 */
    val DisplaySize = 28.sp
}

/** Material3 装配用的唯一字阶（五档共用，不做第二套）。 */
internal val EduTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = TypeTokens.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = TypeTokens.DisplaySize,
        lineHeight = 36.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = TypeTokens.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = TypeTokens.TitleSize,
        lineHeight = 30.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = TypeTokens.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = TypeTokens.BodySize,
        lineHeight = TypeTokens.BodyLineHeight,
    ),
    bodyMedium = TextStyle(
        fontFamily = TypeTokens.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = TypeTokens.BodySize,
        lineHeight = TypeTokens.BodyLineHeight,
    ),
    labelLarge = TextStyle(
        fontFamily = TypeTokens.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = TypeTokens.LabelSize,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = TypeTokens.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
