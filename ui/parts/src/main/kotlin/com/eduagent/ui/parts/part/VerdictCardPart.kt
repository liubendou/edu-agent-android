package com.eduagent.ui.parts.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.motion.CorrectStroke
import com.eduagent.core.design.motion.wrongShake
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.VerdictCardPayload
import com.eduagent.core.model.part.VerdictResult
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 5 · 判题卡 `verdict_card`（契约 §4-5）。
 *
 * 必需字段：`result`(correct|wrong)、`user_answer`、`answer`、`explain`。
 *
 * 动效复用（§8.4，**零件内不写时长/曲线**，一律调 `:core:design` 的配方实现）：
 * - 答对 → 配方 6「答对描边」[CorrectStroke]（对勾与圆环一次描边）；
 * - 答错 → 配方 5「答错摇头」[wrongShake]（横向位移序列）。
 *
 * minimal 档静态表达（§4-5 明文）：**图标 + 文案终态，无描边动画** ——
 * 色（Success/Error）+ 图标 + 文字三重载体，信息不丢失（§8.3）。
 */
@Composable
fun VerdictCardPart(payload: VerdictCardPayload, modifier: Modifier = Modifier) {
    val correct = payload.result == VerdictResult.CORRECT
    val resultLabel = stringResource(
        if (correct) R.string.part_verdict_correct else R.string.part_verdict_wrong,
    )
    PartCard(modifier = modifier) {
        Row(
            modifier = if (correct) Modifier else Modifier.wrongShake(shakeKey = 1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (correct) {
                CorrectStroke(playKey = 1, diameter = 26.dp)
            } else {
                PartResultMark(correct = false, contentDescription = resultLabel)
            }
            PartSectionLabel(
                text = resultLabel,
                color = if (correct) ColorTokens.Success else ColorTokens.Error,
            )
        }
        PartField(
            label = stringResource(R.string.part_verdict_user_answer_label),
            value = payload.userAnswer,
        )
        PartField(
            label = stringResource(R.string.part_verdict_answer_label),
            value = payload.answer,
        )
        PartSectionLabel(stringResource(R.string.part_verdict_explain_label))
        PartBody(payload.explain)
    }
}

@Preview(showBackground = true)
@Composable
private fun VerdictCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.VERDICT_CARD)) }
}
