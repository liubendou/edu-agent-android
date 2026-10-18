package com.eduagent.ui.parts.part

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.atom.PressScaleButton
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.design.theme.Elevation
import com.eduagent.core.design.theme.ShapeTokens
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.ProfileConfirmCardPayload
import com.eduagent.core.model.part.ProfileEntry
import com.eduagent.core.model.part.Sensitivity
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 14 · 画像确认卡 `profile_confirm_card`（契约 §4-14）。
 *
 * 必需字段：`entries[]`（`{category, key, value, sensitivity}`）。
 *
 * **硬规则**：渲染层**必须保留可改可删**（不是只读展示）——每条目给出「修改 / 删除」，
 * 修改态走内联输入。可改可删的动作经回调上抛（[onEditValue] / [onDelete]），
 * **不进 payload**（R-T10：payload 不含任何交互 / 动效字段）。
 */
@Composable
fun ProfileConfirmCardPart(
    payload: ProfileConfirmCardPayload,
    modifier: Modifier = Modifier,
    onEditValue: (index: Int, newValue: String) -> Unit = { _, _ -> },
    onDelete: (index: Int) -> Unit = { },
) {
    PartCard(modifier = modifier, title = stringResource(R.string.part_profile_title)) {
        PartHint(stringResource(R.string.part_profile_hint))
        if (payload.entries.isEmpty()) {
            PartHint(stringResource(R.string.part_profile_empty))
        }
        payload.entries.forEachIndexed { index, entry ->
            ProfileEntryRow(
                index = index,
                entry = entry,
                onEditValue = onEditValue,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun ProfileEntryRow(
    index: Int,
    entry: ProfileEntry,
    onEditValue: (index: Int, newValue: String) -> Unit,
    onDelete: (index: Int) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(entry.value) { mutableStateOf(entry.value) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(Elevation.hairlineBorder, ColorTokens.Border, ShapeTokens.Card)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PartBadge(text = entry.category, color = ColorTokens.InkMuted)
            PartBadge(text = entry.key, color = ColorTokens.Ink)
            if (entry.sensitivity == Sensitivity.HIGH) {
                PartBadge(text = stringResource(R.string.part_profile_sensitive), color = ColorTokens.Warning)
            }
        }

        if (editing) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            ProfileActionButton(label = stringResource(R.string.part_profile_action_confirm)) {
                onEditValue(index, draft)
                editing = false
            }
        } else {
            PartBody(entry.value)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileActionButton(label = stringResource(R.string.part_profile_action_edit)) {
                    draft = entry.value
                    editing = true
                }
                ProfileActionButton(label = stringResource(R.string.part_profile_action_delete)) {
                    onDelete(index)
                }
            }
        }
    }
}

@Composable
private fun ProfileActionButton(label: String, onClick: () -> Unit) {
    PressScaleButton(onClick = onClick) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = ColorTokens.OnAccent,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileConfirmCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.PROFILE_CONFIRM_CARD)) }
}
