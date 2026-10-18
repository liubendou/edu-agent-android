package com.eduagent.ui.parts.part

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.CompareCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 3 · 对比卡 `compare_card`（契约 §4-3）。
 *
 * 必需字段：`title` + `columns[]`（2~3 列）+ `rows[][]`。
 * 首列为行标签，占位略宽；不做横向滚动（列数被契约限制在 2~3）。
 */
@Composable
fun CompareCardPart(payload: CompareCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier, title = payload.title) {
        val weights = List(payload.columns.size) { index -> if (index == 0) 1.2f else 1f }
        PartGridRow(cells = payload.columns, weights = weights, header = true)
        payload.rows.forEach { row -> PartGridRow(cells = row, weights = weights) }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompareCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.COMPARE_CARD)) }
}
