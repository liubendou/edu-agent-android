package com.eduagent.core.design.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 冷白极简色板（契约 §13 D25：纯白底、无衬线、大留白、弱边框、几乎无阴影）。
 *
 * 纪律：五档模式**不得**变成五种配色，全站只用这一套 scheme（契约 §8 冻结口径）。
 * 数值未在契约中逐字冻结，此处为端侧唯一色彩事实源；语义色（Success / Error）承担
 * 「答对 / 答错」的**静态信息载体**职责（minimal 档不得只靠动画传达，见 §8.3）。
 */
object ColorTokens {
    /** 画布底色：纯白。 */
    val Canvas = Color(0xFFFFFFFF)

    /** 卡片 / 面板底色：纯白（以弱边框分层，不用阴影）。 */
    val Surface = Color(0xFFFFFFFF)

    /** 次级底板：极浅灰，用于 chip 未选中态、占位条底。 */
    val SurfaceSubtle = Color(0xFFF7F8FA)

    /** 弱边框：唯一的层级手段。 */
    val Border = Color(0xFFE6E8EB)

    /** 主文字：近黑，非纯黑（冷白偏暖的墨色）。 */
    val Ink = Color(0xFF1F2328)

    /** 次级文字。 */
    val InkMuted = Color(0xFF6B7280)

    /** 强调色：克制的墨蓝，用于选中态与主按钮。 */
    val Accent = Color(0xFF3B6FE0)

    /** 强调色上的文字。 */
    val OnAccent = Color(0xFFFFFFFF)

    /** 语义：正确（答对静态终态）。 */
    val Success = Color(0xFF1E8E5A)

    /** 语义：错误（答错静态终态，红线要求不做红色报错框，但文案与图标用此色）。 */
    val Error = Color(0xFFD14343)

    /** 语义：警示（如等待超 8s）。 */
    val Warning = Color(0xFFB26A00)

    /** 禁用态。 */
    val Disabled = Color(0xFFC7CBD1)
}

/** Material3 装配用的唯一 light scheme（暗色 v1 不做，契约 §10 `theme` 键留口）。 */
internal val EduLightColorScheme = lightColorScheme(
    primary = ColorTokens.Accent,
    onPrimary = ColorTokens.OnAccent,
    primaryContainer = ColorTokens.SurfaceSubtle,
    onPrimaryContainer = ColorTokens.Ink,
    secondary = ColorTokens.InkMuted,
    onSecondary = ColorTokens.OnAccent,
    background = ColorTokens.Canvas,
    onBackground = ColorTokens.Ink,
    surface = ColorTokens.Surface,
    onSurface = ColorTokens.Ink,
    surfaceVariant = ColorTokens.SurfaceSubtle,
    onSurfaceVariant = ColorTokens.InkMuted,
    outline = ColorTokens.Border,
    outlineVariant = ColorTokens.Border,
    error = ColorTokens.Error,
    onError = ColorTokens.OnAccent,
)
