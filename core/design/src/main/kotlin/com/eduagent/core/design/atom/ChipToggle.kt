package com.eduagent.core.design.atom

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.MotionTokens
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.Elevation
import com.eduagent.core.design.theme.ShapeTokens

/**
 * 配方 3「选项 chips 弹跳」（契约 §8.4-3 / §7-D）：选中 `scale [1, 1.12, 1]` 一次性 + `snappy`。
 *
 * 关键签名（逐字）：`(label: String, selected: Boolean, onToggle: () -> Unit)`
 * minimal / reduced：只留填充色变化（≤`micro`，minimal 为瞬时）。
 */
@Composable
fun ChipToggle(
    label: String,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = LocalMotionLevel.current
    var bounceKey by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(bounceKey, level) {
        if (bounceKey == 0 || !level.allowsTranslation) {
            scale.snapTo(1f)
            return@LaunchedEffect
        }
        scale.snapTo(1f)
        scale.animateTo(targetValue = 1.12f, animationSpec = MotionTokens.snappy)
        scale.animateTo(targetValue = 1f, animationSpec = MotionTokens.snappy)
    }
    val fill by animateColorAsState(
        targetValue = if (selected) ColorTokens.Accent else ColorTokens.SurfaceSubtle,
        animationSpec = tween(durationMillis = if (level.allowsFade) MotionTokens.microMs else 0),
        label = "chip_fill",
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(ShapeTokens.Small)
            .background(fill)
            .border(Elevation.hairlineBorder, ColorTokens.Border, ShapeTokens.Small)
            .clickable {
                bounceKey += 1
                onToggle()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) ColorTokens.OnAccent else ColorTokens.Ink,
        )
    }
}
