package com.eduagent.core.design.atom

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.R
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.ShapeTokens
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/** 超过这个时长（8s）必须明示「模型正在思考」（契约 §8.4-7）。 */
const val THINKING_LONG_THRESHOLD_MS: Long = 8_000L

/**
 * 配方 7「等待思考」（契约 §8.4-7）：`compose-shimmer` 占位条 + 光标呼吸；
 * 超 [THINKING_LONG_THRESHOLD_MS] 必须明示。
 *
 * 关键签名（逐字）：`(elapsedMs: Long)`（另接受 `modifier`）。
 * minimal / reduced：不渲染 shimmer 循环层，退化为**静态文案**（信息不丢失）。
 */
@Composable
fun ThinkingPlaceholder(
    elapsedMs: Long,
    modifier: Modifier = Modifier,
) {
    val level = LocalMotionLevel.current
    val isLong = elapsedMs >= THINKING_LONG_THRESHOLD_MS
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (level.allowsLooping) {
            val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(12.dp)
                    .clip(ShapeTokens.Small)
                    .background(ColorTokens.SurfaceSubtle)
                    .shimmer(shimmer),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        val cursorAlpha: Float = if (level.allowsLooping) {
            val transition = rememberInfiniteTransition(label = "thinking_cursor")
            val value by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 700, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "thinking_cursor_alpha",
            )
            value
        } else {
            1f
        }
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 14.dp)
                .background(
                    if (isLong) {
                        ColorTokens.Warning
                    } else {
                        ColorTokens.InkMuted.copy(alpha = cursorAlpha)
                    },
                ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(
                if (isLong) R.string.thinking_long else R.string.thinking_short,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLong) ColorTokens.Warning else ColorTokens.InkMuted,
        )
    }
}
