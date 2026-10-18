package com.eduagent.core.design.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 阴影纪律（契约 §13 D25：几乎无阴影，以弱边框分层代替投影）。
 *
 * 全部层级阴影恒为 0.dp；层级只靠 [hairlineBorder] 与底色差表达（[ColorTokens.SurfaceSubtle]）。
 * 这样在 minimal 档与低端机上不会出现「阴影拖影」的首帧抖动。
 */
object Elevation {
    /** 平面：贴底（画布、通栏）。 */
    val Flat: Dp = 0.dp

    /** 卡片：与画布同白，靠 1dp 弱边框区分。 */
    val Card: Dp = 0.dp

    /** 浮层（toast、面板）：同样无投影。 */
    val Overlay: Dp = 0.dp

    /** 弱边框宽度：唯一的层级手段。 */
    val hairlineBorder: Dp = 1.dp
}
