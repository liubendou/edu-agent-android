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
import com.eduagent.core.model.part.TimelineCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 8 · 时间线卡 `timeline_card`（契约 §4-8）。
 *
 * 必需字段：`title`、`events[]`（`{at, text}`）。
 * 静态排版：左侧实心点 + 时间标注 + 事件文本。
 */
@Composable
fun TimelineCardPart(payload: TimelineCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier, title = payload.title) {
        if (payload.events.isEmpty()) {
            PartHint(stringResource(R.string.part_timeline_empty))
        }
        payload.events.forEach { event ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PartDot(color = ColorTokens.Accent)
                PartBadge(text = event.at, color = ColorTokens.InkMuted)
                PartBody(event.text)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.TIMELINE_CARD)) }
}
