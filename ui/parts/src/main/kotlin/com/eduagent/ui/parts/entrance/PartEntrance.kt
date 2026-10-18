package com.eduagent.ui.parts.entrance

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.MotionLevel
import com.eduagent.core.design.motion.MotionTokens

/** 入场上移量：契约 §8.5 规定 8~12（px），密度无关取值 10。 */
private val ENTRANCE_OFFSET = 10.dp

/**
 * 零件入场规范（契约 §8.5，逐字接口见任务包 06 §7-D）。
 *
 * 六条纪律：
 * 1. 只做「透明度 0→1 + 上移 8~12 → 0」两件事；
 * 2. 时长用 `base`（200ms），缓动 ease-out；
 * 3. **禁止**缩放弹出、**禁止**旋转、**禁止**弹跳；
 * 4. 位移走 `graphicsLayer`（绘制期），**不触发重排**（V5：入场不抖动）；
 * 5. 已入场零件不得再次入场（调用点由 `key(envelope.id)` 保证，本函数无状态重播）；
 * 6. minimal / reduced 档**直接呈现终态**（不淡入、不位移），信息不丢失。
 *
 * 说明：§8.5 原文写「上移 8~12px」；此处按密度无关的 10dp 实现，落在同一视觉量级。
 */
@Composable
fun Modifier.partEntrance(level: MotionLevel = LocalMotionLevel.current): Modifier {
    if (!level.allowsTranslation) return this

    val offsetPx = with(LocalDensity.current) { ENTRANCE_OFFSET.toPx() }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = MotionTokens.baseMs, easing = FastOutSlowInEasing),
        )
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offsetPx
    }
}
