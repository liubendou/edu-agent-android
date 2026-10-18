package com.eduagent.ui.parts.fallback

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.ui.parts.R

/**
 * 判废零件位退化：纯文本段落（契约 §7 二级兜底，任务包 06 §7-D 逐字接口）。
 *
 * 纪律：
 * 1. **不做红色报错框**（§7 明文；R-P3：不得静默失败，但也不得用报错视觉吓用户）；
 * 2. 必有内容（空文本时给出中性占位文案）；
 * 3. 不使用任何零件容器视觉（就是普通段落）。
 */
@Composable
fun PlainTextPart(text: String, modifier: Modifier = Modifier) {
    val shown = text.ifBlank { stringResource(R.string.part_fallback_empty) }
    Text(
        text = shown,
        style = MaterialTheme.typography.bodyLarge,
        color = ColorTokens.Ink,
        modifier = modifier.fillMaxWidth(),
    )
}
