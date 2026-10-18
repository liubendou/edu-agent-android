package com.eduagent.feature.main.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.eduagent.ui.parts.gallery.PartGalleryScreen

/**
 * DEBUG-ONLY 零件陈列页临时入口（任务包 06 §4.2 人工检查点 1/2）。
 *
 * 仅在 debug 变体存在（`src/debug` 源集 + `src/debug/AndroidManifest.xml`），
 * release 构建不含本类与对应 manifest 节点。
 *
 * 授权与时限见 `99-记录\99-事件与例外.md`：**第 10 步正式挂载时删除**。
 * 本类不含任何业务逻辑，只做「打开陈列页」一件事。
 */
class PartGalleryDebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PartGalleryScreen(onBack = { finish() })
        }
    }
}
