package com.eduagent.ui.parts.part

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.MaterialCardPayload
import com.eduagent.core.model.part.MaterialSourceKind
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 15 · 资料卡 `material_card`（契约 §4-15）。
 *
 * 必需字段：`source_kind`(photo|scan|upload)、`summary`、`fields[]`、`uncertain[]`。
 * `uncertain[]` 必须**显式呈现**（R-P3：不得静默——识别不确定项要如实告知）。
 */
@Composable
fun MaterialCardPart(payload: MaterialCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier) {
        PartBadge(text = sourceKindLabel(payload.sourceKind), color = ColorTokens.Accent)
        PartBody(payload.summary)

        if (payload.fields.isNotEmpty()) {
            PartSectionLabel(stringResource(R.string.part_material_fields_label))
            payload.fields.forEach { field -> PartLine(marker = null, text = field) }
        }
        if (payload.uncertain.isNotEmpty()) {
            PartSectionLabel(stringResource(R.string.part_material_uncertain_label), ColorTokens.Warning)
            payload.uncertain.forEach { item ->
                PartLine(marker = null, text = item, markerColor = ColorTokens.Warning)
            }
        }
    }
}

@Composable
private fun sourceKindLabel(kind: MaterialSourceKind): String = stringResource(
    when (kind) {
        MaterialSourceKind.PHOTO -> R.string.part_material_source_photo
        MaterialSourceKind.SCAN -> R.string.part_material_source_scan
        MaterialSourceKind.UPLOAD -> R.string.part_material_source_upload
    },
)

@Preview(showBackground = true)
@Composable
private fun MaterialCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.MATERIAL_CARD)) }
}
