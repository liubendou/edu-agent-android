package com.eduagent.ui.parts.part

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.Elevation
import com.eduagent.core.design.theme.ShapeTokens

/**
 * 16 个零件共用的**内部**容器与文本基元（避免 16 份重复版式）。
 *
 * 纪律：本文件只是版式基元，**不含任何动效参数**（R-T10：零件内不得出现时长/曲线）；
 * 视觉一律取自 `:core:design` 的 tokens（§14.4：页面严禁裸写样式数值外的自定义参数）。
 */

/** 零件外壳：冷白卡面 + 发丝描边 + 大留白（D25）。 */
@Composable
internal fun PartCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Card)
            .background(ColorTokens.Surface)
            .border(Elevation.hairlineBorder, ColorTokens.Border, ShapeTokens.Card)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = ColorTokens.Ink,
            )
        }
        content()
    }
}

/** 小节标签（卡内二级标题）。 */
@Composable
internal fun PartSectionLabel(text: String, color: Color = ColorTokens.InkMuted) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
}

/** 正文。 */
@Composable
internal fun PartBody(text: String, color: Color = ColorTokens.Ink) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = color)
}

/** 弱化说明文字。 */
@Composable
internal fun PartHint(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = ColorTokens.InkMuted)
}

/** 「标签 + 值」一行。 */
@Composable
internal fun PartField(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PartSectionLabel(label)
        PartBody(value)
    }
}

/** 无序/有序条目行（[marker] 为序号或符号）。 */
@Composable
internal fun PartLine(marker: String?, text: String, markerColor: Color = ColorTokens.Accent) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (marker != null) {
            Text(
                text = marker,
                style = MaterialTheme.typography.labelLarge,
                color = markerColor,
                modifier = Modifier.width(20.dp),
            )
        }
        PartBody(text)
    }
}

/** 小圆点（Canvas 手绘，M6：不依赖外部图片资源）。 */
@Composable
internal fun PartDot(color: Color, diameter: Dp = 8.dp) {
    Canvas(modifier = Modifier.size(diameter)) {
        drawCircle(color = color, radius = size.minDimension / 2f)
    }
}

/**
 * 对 / 错标记图标（判题卡使用）。
 *
 * 可访问性：带 [contentDescription]（文案取自 `strings.xml`，调用方传入），供测试与读屏断言。
 */
@Composable
internal fun PartResultMark(
    correct: Boolean,
    contentDescription: String,
    diameter: Dp = 20.dp,
    color: Color = if (correct) ColorTokens.Success else ColorTokens.Error,
) {
    Canvas(
        modifier = Modifier
            .size(diameter)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val stroke = 2.dp.toPx()
        if (correct) {
            drawLine(
                color = color,
                start = Offset(size.width * 0.24f, size.height * 0.54f),
                end = Offset(size.width * 0.44f, size.height * 0.72f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(size.width * 0.44f, size.height * 0.72f),
                end = Offset(size.width * 0.76f, size.height * 0.32f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        } else {
            drawLine(
                color = color,
                start = Offset(size.width * 0.26f, size.height * 0.26f),
                end = Offset(size.width * 0.74f, size.height * 0.74f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(size.width * 0.74f, size.height * 0.26f),
                end = Offset(size.width * 0.26f, size.height * 0.74f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** 小徽标（等级 / 机型 / 分类等短标签）。 */
@Composable
internal fun PartBadge(text: String, color: Color = ColorTokens.InkMuted) {
    Box(
        modifier = Modifier
            .clip(ShapeTokens.Small)
            .border(Elevation.hairlineBorder, ColorTokens.Border, ShapeTokens.Small)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/** 键值两栏网格行（对比卡 / 表卡使用）。 */
@Composable
internal fun PartGridRow(cells: List<String>, weights: List<Float>, header: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.forEachIndexed { index, cell ->
            Box(modifier = Modifier.weight(weights.getOrElse(index) { 1f })) {
                Text(
                    text = cell,
                    style = if (header) {
                        MaterialTheme.typography.labelLarge
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = if (header) ColorTokens.Ink else ColorTokens.InkMuted,
                )
            }
        }
    }
}
