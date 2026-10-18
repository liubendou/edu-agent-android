package com.eduagent.core.design.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.motion.pressRebound
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.ShapeTokens

/**
 * 配方 1「按压回弹」的通用按钮（契约 §8.4-1 / §7-D）。
 *
 * 关键签名（逐字）：`(onClick: () -> Unit, enabled: Boolean = true, content: @Composable RowScope.() -> Unit)`
 * （另外接受标准 `modifier`）。按压 feedback 由 [pressRebound] 提供：full 档 scale 0.97 + snappy，
 * reduced / minimal 档取消 scale（按钮态本身仍可用）。
 */
@Composable
fun PressScaleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .pressRebound(enabled = enabled, onClick = onClick)
            .clip(ShapeTokens.Pill)
            .background(if (enabled) ColorTokens.Accent else ColorTokens.Disabled)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content,
    )
}
