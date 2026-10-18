package com.eduagent.core.design.atom

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.R
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.MotionTokens
import com.eduagent.core.design.theme.ColorTokens
import kotlin.math.roundToInt

/** 进度类零件（`progress_card`）的三个 metric（契约 §4：`metric`(mastery|plan|score)）。 */
enum class Metric { MASTERY, PLAN, SCORE }

/** metric -> 中文标签（文案集中在 `strings.xml`，§14.2）。 */
val Metric.labelRes: Int
    get() = when (this) {
        Metric.MASTERY -> R.string.metric_mastery
        Metric.PLAN -> R.string.metric_plan
        Metric.SCORE -> R.string.metric_score
    }

/**
 * 配方 10「掌握度增长」（契约 §8.4-10 / §7-D）：数值变化时插值增长（400~600ms ease-out），**不来回摆动**。
 *
 * 关键签名（逐字）：`(value: Float, metric: Metric)`
 *
 * 实现说明：0..1 的环形进度用 Canvas 手绘（`drawArc`）。冻结库表里的 Vico（`compose-m3`）已在
 * `:core:design` 装配可用，其首次消费点在第 6 步的 `progress_card` 折线/柱状曲线（此处环形并非 Vico 形态）。
 *
 * minimal / reduced：不插值，**直接跳到目标值**，数值与单位直显（信息不丢失）。
 */
@Composable
fun MasteryRing(
    value: Float,
    metric: Metric,
    modifier: Modifier = Modifier,
    diameter: Dp = 72.dp,
) {
    val level = LocalMotionLevel.current
    val target = value.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = if (level.allowsTranslation) MotionTokens.slowMs else 0,
            easing = FastOutSlowInEasing,
        ),
        label = "mastery_growth",
    )
    val shown = if (level.allowsTranslation) animated else target
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(diameter)) {
                val strokeWidth = 3.dp.toPx()
                drawArc(
                    color = ColorTokens.Border,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                )
                drawArc(
                    color = ColorTokens.Accent,
                    startAngle = -90f,
                    sweepAngle = 360f * shown,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            Text(
                text = "${(target * 100).roundToInt()}",
                style = MaterialTheme.typography.titleLarge,
                color = ColorTokens.Ink,
            )
        }
        Text(
            text = stringResource(metric.labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = ColorTokens.InkMuted,
        )
    }
}
