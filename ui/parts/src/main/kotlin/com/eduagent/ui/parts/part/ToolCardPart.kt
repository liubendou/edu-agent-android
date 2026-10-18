package com.eduagent.ui.parts.part

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.design.theme.Elevation
import com.eduagent.core.design.theme.ShapeTokens
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.ToolCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 16 · 工具卡 `tool_card`（契约 §4-16）。
 *
 * 必需字段：`query`、`results[]`（`{title, url, snippet, source}`）。
 *
 * **红线 R-C1 / R-C2**：不得编造来源 —— 每条 `results[]` **必须**有 `source`；
 * `source` 为空/空白的结果**一律不渲染**（过滤发生在渲染前，不是「隐藏了一下」）。
 */
@Composable
fun ToolCardPart(payload: ToolCardPayload, modifier: Modifier = Modifier) {
    // R-C1/R-C2：无来源的结果不进入渲染树
    val sourced = payload.results.filter { result -> !result.source.isNullOrBlank() }

    PartCard(modifier = modifier) {
        PartField(
            label = stringResource(R.string.part_tool_query_label),
            value = payload.query,
        )
        if (sourced.isEmpty()) {
            PartHint(stringResource(R.string.part_tool_no_source_notice))
        } else {
            PartSectionLabel(stringResource(R.string.part_tool_results_label))
            sourced.forEach { result -> ToolResultRow(result.title, result.snippet, result.source.orEmpty()) }
        }
    }
}

@Composable
private fun ToolResultRow(title: String, snippet: String, source: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Card)
            .background(ColorTokens.SurfaceSubtle)
            .border(Elevation.hairlineBorder, ColorTokens.Border, ShapeTokens.Card)
            .padding(10.dp),
    ) {
        PartBody(title)
        PartHint(snippet)
        PartField(
            label = stringResource(R.string.part_tool_source_label),
            value = source,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.TOOL_CARD)) }
}
