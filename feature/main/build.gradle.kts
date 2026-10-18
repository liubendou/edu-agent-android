// :feature:main —— 契约 §2：首屏 / 会话 / 复习入口 / 提分入口 四个页面
// 依赖方向：:feature:main → :core:domain | :ui:parts | :core:design
plugins {
    alias(libs.plugins.eduagent.android.library)
    alias(libs.plugins.eduagent.android.compose)
}

android {
    namespace = "com.eduagent.feature.main"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":ui:parts"))
    implementation(project(":core:design"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // 第 6 步调试期临时入口（src/debug）：仅 debug 变体需要 ComponentActivity + setContent。
    // 授权与删除时限见 99-记录\99-事件与例外.md；第 10 步正式挂载时随入口一并移除。
    // core-ktx 与契约 §1 冻结版（1.19.1）对齐，避免 activity-compose 传递带入旧版 1.18.0。
    debugImplementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
