package com.eduagent.ui.parts.part

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.ErrorCauseCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 6 · 错因卡 `error_cause_card`（契约 §4-6）。
 *
 * 必需字段：`cause`、`evidence`、`fix[]`。
 * 归因 → 证候 → 纠正三步结构，纯静态排版（无动效）。
 */
@Composable
fun ErrorCauseCardPart(payload: ErrorCauseCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier) {
        PartField(label = stringResource(R.string.part_error_cause_label), value = payload.cause)
        PartField(label = stringResource(R.string.part_error_evidence_label), value = payload.evidence)
        PartSectionLabel(stringResource(R.string.part_error_fix_label))
        payload.fix.forEach { item -> PartLine(marker = null, text = item) }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorCauseCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.ERROR_CAUSE_CARD)) }
}
