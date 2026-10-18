package com.eduagent.ui.parts.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.DiagnoseCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples
import kotlin.math.roundToInt

/**
 * 零件 13 · 诊断卡 `diagnose_card`（契约 §4-13）。
 *
 * 必需字段：`title`、`weak[]`（`{topic, score, evidence}`）。
 * 静态排版：主题徽标 + 得分（数值直显）+ 依据。
 */
@Composable
fun DiagnoseCardPart(payload: DiagnoseCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier, title = payload.title) {
        payload.weak.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PartBadge(text = item.topic, color = ColorTokens.Accent)
                PartHint(
                    stringResource(R.string.part_diagnose_score_label) +
                        " " + (item.score * 100).roundToInt() +
                        stringResource(R.string.part_progress_value_suffix),
                )
            }
            PartLine(marker = null, text = item.evidence)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiagnoseCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.DIAGNOSE_CARD)) }
}
