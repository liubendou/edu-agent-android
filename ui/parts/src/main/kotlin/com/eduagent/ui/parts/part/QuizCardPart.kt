package com.eduagent.ui.parts.part

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.eduagent.core.model.part.QuizCardPayload
import com.eduagent.core.model.part.QuizKind
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 4 · 题卡 `quiz_card`（契约 §4-4）。
 *
 * 必需字段：`stem`、`kind`(choice|blank|open)、`options[]`(choice 时)、`explain`。
 * **硬规则**：`answer` 由端侧保管，判分前**不下发给 UI** —— 故本 payload 无 `answer`，
 * 卡片只呈现题面与讲解，并明确提示「答案由端侧保管」（R-P3：不得静默）。
 */
@Composable
fun QuizCardPart(payload: QuizCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier) {
        PartBadge(text = quizKindLabel(payload.kind), color = ColorTokens.Accent)
        PartBody(payload.stem)
        if (payload.kind == QuizKind.CHOICE && payload.options.isNotEmpty()) {
            PartSectionLabel(stringResource(R.string.part_quiz_options_label))
            payload.options.forEachIndexed { index, option -> QuizOptionRow(index = index + 1, text = option) }
        }
        PartHint(stringResource(R.string.part_quiz_answer_held))
        PartSectionLabel(stringResource(R.string.part_quiz_explain_label))
        PartBody(payload.explain)
    }
}

/** 选项行：序号 + 文本，弱描边（作答交互属第 7/10 步，本步只渲染）。 */
@Composable
private fun QuizOptionRow(index: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Card)
            .background(ColorTokens.SurfaceSubtle)
            .border(Elevation.hairlineBorder, ColorTokens.Border, ShapeTokens.Card)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.labelLarge,
            color = ColorTokens.Accent,
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = ColorTokens.Ink)
    }
}

@Composable
private fun quizKindLabel(kind: QuizKind): String = stringResource(
    when (kind) {
        QuizKind.CHOICE -> R.string.part_quiz_kind_choice
        QuizKind.BLANK -> R.string.part_quiz_kind_blank
        QuizKind.OPEN -> R.string.part_quiz_kind_open
    },
)

@Preview(showBackground = true)
@Composable
private fun QuizCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.QUIZ_CARD)) }
}
