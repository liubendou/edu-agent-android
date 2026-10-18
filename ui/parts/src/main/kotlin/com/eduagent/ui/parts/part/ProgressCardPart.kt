package com.eduagent.ui.parts.part

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.ProgressCardPayload
import com.eduagent.core.model.part.ProgressMetric
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import kotlin.math.roundToInt

/**
 * 零件 11 · 进度卡 `progress_card`（契约 §4-11）。
 *
 * 必需字段：`title`、`series[]`（`{label, value}`）、`metric`(mastery|plan|score)。
 *
 * 三类库落地：曲线走冻结的 **Vico**（`com.patrykandpatrick.vico:compose-m3` 3.3.1，
 * 契约 §1 冻结库表；本零件是 Vico 的首次消费点）。
 *
 * minimal 档静态表达（§4-11 明文）：**数值直显，无增长动画** —— 不渲染曲线，改为
 * 「标签 + 数值」文本网格（[com.eduagent.core.design.motion.InfoCarrier.VALUE] 载体）。
 */
@Composable
fun ProgressCardPart(payload: ProgressCardPayload, modifier: Modifier = Modifier) {
    val level = LocalMotionLevel.current
    PartCard(modifier = modifier, title = payload.title) {
        if (level.allowsTranslation) {
            VicoLineChart(values = payload.series.map { point -> point.value })
        }
        // 数值直显：minimal 档的唯一信息载体，也是 full 档的读数补充
        payload.series.forEach { point ->
            PartField(
                label = point.label,
                value = "${(point.value * 100).roundToInt()}${stringResource(R.string.part_progress_value_suffix)}",
            )
        }
        PartHint(stringResource(payload.metric.labelRes()))
    }
}

/** metric 的中文标签（`metric` 为冻结枚举，文案见 §7-E 与 `strings.xml`）。 */
private fun ProgressMetric.labelRes(): Int = when (this) {
    ProgressMetric.MASTERY -> R.string.part_progress_metric_mastery
    ProgressMetric.PLAN -> R.string.part_progress_metric_plan
    ProgressMetric.SCORE -> R.string.part_progress_metric_score
}

/** Vico 折线：静态渲染（Vico 本身不做「增长动画」，符合 §4-11「无增长动画」口径）。 */
@Composable
private fun VicoLineChart(values: List<Float>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) {
        modelProducer.runTransaction { lineModel { series(values) } }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(rememberLineCartesianLayer()),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun ProgressCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.PROGRESS_CARD)) }
}
