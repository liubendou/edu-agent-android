package com.eduagent.ui.parts.part

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.MapCardPayload
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.gallery.PartSamples

/**
 * 零件 7 · 地图卡 `map_card`（契约 §4-7）。
 *
 * 必需字段：`title`、`nodes[]`、`edges[]`。
 * 静态表达（§4-7 minimal：静态图）：节点按目录顺序列出，边以「起点 -> 终点」关系行呈现。
 * 本步不引入图布局算法（避免在零件层造轮子）；关系行与节点列表等价传达同一结构信息。
 */
@Composable
fun MapCardPart(payload: MapCardPayload, modifier: Modifier = Modifier) {
    PartCard(modifier = modifier, title = payload.title) {
        val labelOf = payload.nodes.associate { node -> node.id to node.label }
        payload.nodes.forEach { node -> PartLine(marker = null, text = node.label) }
        payload.edges.forEach { edge ->
            val from = labelOf[edge.from] ?: edge.from
            val to = labelOf[edge.to] ?: edge.to
            PartLine(marker = null, text = "$from -> $to")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MapCardPartPreview() {
    EduTheme { PartHost(PartSamples.envelopeFor(PartTypes.MAP_CARD)) }
}
