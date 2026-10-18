// :ui:parts —— 契约 §2：16 个零件 Compose 实现 + 微交互配方组件
// 依赖方向：:ui:parts → :core:design | :core:model
plugins {
    alias(libs.plugins.eduagent.android.library)
    alias(libs.plugins.eduagent.android.compose)
}

android {
    namespace = "com.eduagent.ui.parts"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:model"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
