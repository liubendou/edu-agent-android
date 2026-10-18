package com.eduagent.core.design.atom

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.theme.ColorTokens

/**
 * 配方 4「提交态三点加载」（契约 §8.4-4 / §7-D）：三点 `y [0,-4,0]` 循环，相位延迟 `i*0.14`，0.5s/循环。
 *
 * 关键签名（逐字）：`(label: String)`（另接受 `modifier` / `tint`）。
 * 调用方须同时把按钮文案换「动作中」并禁用（本组件只负责三点 + label）。
 *
 * minimal / reduced：不渲染循环层，三点改为**静态透明度快照**（0.35 / 0.675 / 1.0），
 * 信息由 [label] 承载（信息不丢失）。
 */
@Composable
fun ThreeDotLoading(
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = ColorTokens.Ink,
) {
    val level = LocalMotionLevel.current
    val animate = level.allowsLooping
    val liftPx = with(LocalDensity.current) { 4.dp.toPx() }
    val phase: Float = if (animate) {
        val transition = rememberInfiniteTransition(label = "three_dot_loading")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "three_dot_phase",
        )
        value
    } else {
        0f
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val staticAlpha = 0.35f + index * 0.325f
                val shifted = (phase + index * 0.28f) % 1f
                val wave = if (shifted < 0.5f) shifted * 2f else (1f - shifted) * 2f
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .graphicsLayer {
                            translationY = if (animate) -liftPx * wave else 0f
                            alpha = if (animate) 1f else staticAlpha
                        }
                        .clip(CircleShape)
                        .background(tint),
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}
