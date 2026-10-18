package com.eduagent.ui.parts.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.KeypointCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 2 · 要点卡 `keypoint_card`（契约 §4-2）。
 *
 * 必需字段：`title` + `points[]`（`{text, level}`）。
 * `level` 越高越次要：1 核心（强调色）/ 2 要点（主文字）/ 3 补充（次级文字）。
 */
@Composable
fun KeypointCardPart(payload: KeypointCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier, title = payload.title) {
        payload.points.forEach { point ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PartBadge(text = keypointLevelLabel(point.level), color = keypointLevelColor(point.level))
                Text(
                    text = point.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTokens.Ink,
                )
            }
        }
    }
}

@Composable
private fun keypointLevelLabel(level: Int): String = stringResource(
    when (level) {
        1 -> R.string.part_keypoint_level_core
        2 -> R.string.part_keypoint_level_key
        else -> R.string.part_keypoint_level_note
    },
)

private fun keypointLevelColor(level: Int): Color = when (level) {
    1 -> ColorTokens.Accent
    2 -> ColorTokens.Ink
    else -> ColorTokens.InkMuted
}

@Preview(showBackground = true)
@Composable
private fun KeypointCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.KEYPOINT_CARD)) }
}
