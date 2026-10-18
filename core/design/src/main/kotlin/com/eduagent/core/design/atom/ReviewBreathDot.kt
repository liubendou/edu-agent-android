package com.eduagent.core.design.atom

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.R
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.theme.ColorTokens

/**
 * 配方 9「待复习呼吸点」（契约 §8.4-9 / §7-D）：外圈呼吸扩散 + 静态实心点。
 *
 * 关键签名（逐字）：`(dueCount: Int, minimal 不渲染呼吸层)`
 *
 * 循环层**只在有意义时渲染**（`dueCount > 0` 且 full 档，§8.3 规则 2）。
 * minimal：不渲染呼吸层，**保留静态实心点**（信息不丢失）。
 * 无障碍：整体带 `contentDescription`（今日待复习 + 条数）。
 */
@Composable
fun ReviewBreathDot(
    dueCount: Int,
    modifier: Modifier = Modifier,
    diameter: Dp = 10.dp,
) {
    val level = LocalMotionLevel.current
    val hasDue = dueCount > 0
    val breath = hasDue && level.allowsLooping
    val description = stringResource(R.string.review_due_prefix) + " " + dueCount
    Box(
        modifier = modifier
            .size(diameter * 2)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (breath) {
            val transition = rememberInfiniteTransition(label = "review_breath")
            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "review_breath_progress",
            )
            Box(
                modifier = Modifier
                    .size(diameter * 2)
                    .graphicsLayer {
                        scaleX = 0.6f + progress * 0.4f
                        scaleY = 0.6f + progress * 0.4f
                        alpha = 1f - progress
                    }
                    .clip(CircleShape)
                    .background(ColorTokens.Accent),
            )
        }
        if (hasDue) {
            Box(
                modifier = Modifier
                    .size(diameter)
                    .clip(CircleShape)
                    .background(ColorTokens.Accent),
            )
        }
    }
}
