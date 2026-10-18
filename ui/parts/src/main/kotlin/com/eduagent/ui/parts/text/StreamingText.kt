package com.eduagent.ui.parts.text

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.MotionLevel
import com.eduagent.core.design.motion.MotionTokens
import com.eduagent.core.design.theme.ColorTokens
import kotlinx.coroutines.delay

/** 匀速吐字节拍（每字 28ms，契约 §8.5-③「匀速吐字」）。 */
private const val TYPE_INTERVAL_MS = 28L

/**
 * 流式文本（契约 §8.5-③，逐字接口见任务包 06 §7-D）。
 *
 * 纪律：
 * 1. **匀速**吐字（固定节拍，不随内容长度变化、不做「越读越快」）；
 * 2. 吐完才回调 [onCompleted]（调用方据此 enable 交互）；
 * 3. 交互启用用**透明度过渡**提示（未完成时整块 0.92 → 完成后 1.0），不做缩放弹跳；
 * 4. minimal / reduced 档**不逐字**（契约 §4-1：讲解卡 minimal「同静态排版，无逐字」）——
 *    直接呈现全文并立即 [onCompleted]。
 *
 * @param text 完整文本（由上游按行到达后累积）。
 * @param isStreaming 上游是否仍在推送；置 false 时把剩余字符一次性补齐。
 * @param onCompleted 吐完回调（只回调一次语义：调用方需自行保证幂等）。
 */
@Composable
fun StreamingText(
    text: String,
    isStreaming: Boolean,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = LocalMotionLevel.current
    val latestOnCompleted by rememberUpdatedState(onCompleted)

    if (!level.allowsTranslation) {
        // minimal：无逐字，终态直显
        LaunchedEffect(text, isStreaming) {
            if (!isStreaming) latestOnCompleted()
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = ColorTokens.Ink,
            modifier = modifier.fillMaxWidth(),
        )
        return
    }

    var revealed by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text, isStreaming, level) {
        while (revealed < text.length) {
            delay(TYPE_INTERVAL_MS)
            revealed = (revealed + 1).coerceAtMost(text.length)
        }
        latestOnCompleted()
    }
    // 上游提前收流：一次性补齐剩余
    LaunchedEffect(isStreaming) {
        if (!isStreaming) revealed = text.length
    }

    val alpha by animateFloatAsState(
        targetValue = if (revealed >= text.length) 1f else 0.92f,
        animationSpec = tween(durationMillis = MotionTokens.baseMs),
        label = "streaming_interaction_alpha",
    )
    Text(
        text = text.take(revealed.coerceAtMost(text.length)),
        style = MaterialTheme.typography.bodyLarge,
        color = ColorTokens.Ink,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha },
    )
}

/**
 * minimal 档判定（供零件与测试复用）：当前档是否**不做**逐字。
 * 与 [MotionLevel.allowsTranslation] 同义，单独命名以便零件代码表意。
 */
val MotionLevel.isStaticText: Boolean get() = !allowsTranslation
