package com.eduagent.ui.parts.part

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.design.theme.Elevation
import com.eduagent.core.design.theme.ShapeTokens
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.BlockKind
import com.eduagent.core.model.part.ExplainBlock
import com.eduagent.core.model.part.ExplainCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 1 · 讲解卡 `explain_card`（契约 §4-1）。
 *
 * 必需字段：`title` + `blocks[]`（`{kind:text|formula|steps, body|items}`）。
 * minimal 档静态表达：**同静态排版，无逐字**（§4-1）。
 */
@Composable
fun ExplainCardPart(payload: ExplainCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier, title = payload.title) {
        payload.blocks.forEach { block -> ExplainBlockView(block) }
    }
}

@Composable
private fun ExplainBlockView(block: ExplainBlock) {
    when (block.kind) {
        BlockKind.TEXT -> block.body?.takeIf { it.isNotBlank() }?.let { PartBody(it) }

        BlockKind.FORMULA -> FormulaBlock(block.body.orEmpty())

        BlockKind.STEPS -> block.items.forEachIndexed { index, item ->
            PartLine(marker = "${index + 1}.", text = item)
        }
    }
}

/** 公式块：浅底 + 弱描边 + 等宽字，靠版式而非颜色区分（D25）。 */
@Composable
private fun FormulaBlock(body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Card)
            .background(ColorTokens.SurfaceSubtle)
            .border(Elevation.hairlineBorder, ColorTokens.Border, ShapeTokens.Card)
            .padding(10.dp),
    ) {
        PartHint(stringResource(R.string.part_explain_formula_label))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            color = ColorTokens.Ink,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExplainCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.EXPLAIN_CARD)) }
}
