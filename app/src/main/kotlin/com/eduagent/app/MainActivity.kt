package com.eduagent.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

/**
 * 单 Activity 宿主（契约 §2）。
 *
 * 第 2 步只挂一个**空白 Compose 页面**：可安装、可启动、不崩不闪退即达标。
 * 首屏 / 会话 / 复习 / 提分四个页面在第 10 步由 :feature:main 接入。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // 第 2 步：空白页面，不渲染任何内容
                }
            }
        }
    }
}
