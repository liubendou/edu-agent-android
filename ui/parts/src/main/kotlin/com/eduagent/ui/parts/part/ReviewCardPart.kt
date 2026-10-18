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
import com.eduagent.core.design.atom.ReviewBreathDot
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.ReviewCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 12 · 复习卡 `review_card`（契约 §4-12）。
 *
 * 必需字段：`item_id`、`prompt`、`due_at`、`reps`。
 * 待复习呼吸点复用 `:core:design` 的配方 9 实现 [ReviewBreathDot]（minimal 档退化为静态实心点）。
 */
@Composable
fun ReviewCardPart(payload: ReviewCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReviewBreathDot(dueCount = 1)
            PartBody(payload.prompt)
        }
        PartField(
            label = stringResource(R.string.part_review_due_label),
            value = payload.dueAt,
        )
        PartField(
            label = stringResource(R.string.part_review_reps_label),
            value = "${payload.reps}${stringResource(R.string.part_review_reps_suffix)}",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.REVIEW_CARD)) }
}
