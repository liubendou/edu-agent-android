package com.eduagent.core.design.atom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.MotionTokens
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.ShapeTokens
import kotlin.math.roundToInt

/**
 * 配方 2「模式指示滑动」（契约 §8.4-2 / §7-D）：选中底片从旧位**滑到**新位（空间连续），200ms。
 *
 * 关键签名（逐字）：`(chips: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit)`
 *
 * 空间连续的落地方式：底片在一个已测宽的容器内做位移插值（[animateFloatAsState] + `offset`）。
 * 冻结库表里的 `constraintlayout-compose`（MotionLayout）已在 `:core:design` 装配可用，留给第 6 步
 * 需要更复杂「空间连续」的页面使用；此处用等价的位移插值实现（差异见 docs/06 报告）。
 *
 * minimal / reduced：底片**瞬时落到**目标位（无位移），选中态靠填充色区分（信息不丢失）。
 */
@Composable
fun ModeChipRow(
    chips: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = LocalMotionLevel.current
    val density = LocalDensity.current
    var contentWidthPx by remember { mutableFloatStateOf(0f) }
    val chipWidthPx = if (chips.isEmpty()) 0f else contentWidthPx / chips.size
    val indicatorX by animateFloatAsState(
        targetValue = chipWidthPx * selectedIndex,
        animationSpec = if (level.allowsTranslation) {
            MotionTokens.default
        } else {
            tween(durationMillis = 0)
        },
        label = "mode_indicator_offset",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Pill)
            .background(ColorTokens.SurfaceSubtle)
            .padding(3.dp)
            .onSizeChanged { contentWidthPx = it.width.toFloat() },
    ) {
        if (chipWidthPx > 0f) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorX.roundToInt(), 0) }
                    .width(with(density) { chipWidthPx.toDp() })
                    .fillMaxHeight()
                    .clip(ShapeTokens.Pill)
                    .background(ColorTokens.Surface),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            chips.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(ShapeTokens.Pill)
                        .clickable { onSelect(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) ColorTokens.Ink else ColorTokens.InkMuted,
                    )
                }
            }
        }
    }
}
