package com.eduagent.core.design.atom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.MotionTokens
import com.eduagent.core.design.motion.fadeDurationMs
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.ShapeTokens
import kotlinx.coroutines.delay

/** 顶部轻提示自动退场时长（契约 §8.4-8：3.2s）。 */
const val TOP_TOAST_AUTO_DISMISS_MS: Long = 3_200L

/**
 * 配方 8「顶部轻提示」（契约 §8.4-8 / §7-D）：顶部滑入 12~28px + 透明度，`soft`，3.2s 自动退场。
 *
 * 关键签名（逐字）：`(message: String, visible: Boolean, onDismiss: () -> Unit)`
 *
 * 纪律：**居中用外层容器居中**（[Box] + `Alignment.TopCenter`），不得靠 transform 位移居中。
 * minimal / reduced：位移取消（瞬时落位），只保留瞬时淡入淡出；文案仍完整可见。
 */
@Composable
fun TopToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = LocalMotionLevel.current
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = level.fadeDurationMs(MotionTokens.baseMs)),
        label = "top_toast_alpha",
    )
    val slide by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = if (level.allowsTranslation) {
            MotionTokens.soft
        } else {
            tween(durationMillis = 0)
        },
        label = "top_toast_slide",
    )
    val slidePx = with(LocalDensity.current) { 24.dp.toPx() }
    LaunchedEffect(visible) {
        if (visible) {
            delay(TOP_TOAST_AUTO_DISMISS_MS)
            onDismiss()
        }
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = slide * slidePx
                }
                .clip(ShapeTokens.Pill)
                .background(ColorTokens.Ink)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.labelLarge,
                color = ColorTokens.OnAccent,
            )
        }
    }
}
