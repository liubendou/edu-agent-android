package com.eduagent.ui.parts.part

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.TableCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 9 · 表卡 `table_card`（契约 §4-9）。
 *
 * 必需字段：`title`、`headers[]`、`rows[][]`。
 * 硬规则：正文（表格内容）不得超过 1200 字，长内容拆成多张同型零件（拆分工具见
 * `core:model` 的 `PartTextSplitter`，`SplitTextTest` 断言）。
 */
@Composable
fun TableCardPart(payload: TableCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier, title = payload.title) {
        val weights = List(payload.headers.size) { 1f }
        PartGridRow(cells = payload.headers, weights = weights, header = true)
        payload.rows.forEach { row -> PartGridRow(cells = row, weights = weights) }
    }
}

@Preview(showBackground = true)
@Composable
private fun TableCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.TABLE_CARD)) }
}
