package com.eduagent.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 四档圆角（契约 §8：小控件 / 卡片 / 容器 / 胶囊）。
 *
 * 冷白极简 = 小圆角（2~12dp），不做大圆角的「气泡感」。五档共用一套。
 */
object ShapeTokens {
    /** 小控件（chip、标签）。 */
    val Small = RoundedCornerShape(2.dp)

    /** 卡片。 */
    val Card = RoundedCornerShape(6.dp)

    /** 容器 / 面板。 */
    val Container = RoundedCornerShape(10.dp)

    /** 胶囊（按钮、pill）。 */
    val Pill = RoundedCornerShape(percent = 50)
}

/** Material3 装配用的唯一形状表。 */
internal val EduShapes = Shapes(
    extraSmall = ShapeTokens.Small,
    small = ShapeTokens.Small,
    medium = ShapeTokens.Card,
    large = ShapeTokens.Container,
    extraLarge = ShapeTokens.Container,
)
