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
import com.eduagent.core.model.part.PlanHorizon
import com.eduagent.core.model.part.PlanPanelPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 10 · 计划面板 `plan_panel`（契约 §4-10）。
 *
 * 必需字段：`horizon`(short|long)、`goals[]`、`tasks[]`（`{week, items[], done}`）。
 * `horizon = short` 对应「≤30 天」（D32）；卡片标题即档位名（payload 无 title 字段）。
 */
@Composable
fun PlanPanelPart(payload: PlanPanelPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier, title = horizonLabel(payload.horizon)) {
        PartSectionLabel(stringResource(R.string.part_plan_goals_label))
        payload.goals.forEach { goal -> PartLine(marker = null, text = goal) }

        PartSectionLabel(stringResource(R.string.part_plan_tasks_label))
        payload.tasks.forEach { task ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PartBadge(text = task.week, color = ColorTokens.Accent)
                PartHint(
                    stringResource(
                        if (task.done) R.string.part_plan_done else R.string.part_plan_undone,
                    ),
                )
            }
            task.items.forEach { item -> PartLine(marker = null, text = item) }
        }
    }
}

@Composable
private fun horizonLabel(horizon: PlanHorizon): String = stringResource(
    when (horizon) {
        PlanHorizon.SHORT -> R.string.part_plan_horizon_short
        PlanHorizon.LONG -> R.string.part_plan_horizon_long
    },
)

@Preview(showBackground = true)
@Composable
private fun PlanPanelPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.PLAN_PANEL)) }
}
