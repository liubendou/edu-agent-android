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

    testImplementation(libs.junit)
}
