package com.eduagent.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * 冷白极简主题装配入口（契约 §2 / §3）：色、字、形三处统一注入。
 *
 * 用法：`:app` 组合根最外层包 [EduTheme] { ... }；页面不得自行 `MaterialTheme(...)`。
 * 本步不引入暗色 scheme（契约 §10 `theme` 键留口，v1 只做 light）。
 */
@Composable
fun EduTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EduLightColorScheme,
        typography = EduTypography,
        shapes = EduShapes,
        content = content,
    )
}
