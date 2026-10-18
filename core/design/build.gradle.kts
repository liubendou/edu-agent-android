// :core:design —— 契约 §2：tokens、主题、动效框架、触觉、通用原子组件（三类库落地处）
// 依赖方向：:core:design → :core:model（不得依赖 :feature:* / :ui:*）
plugins {
    alias(libs.plugins.eduagent.android.library)
    alias(libs.plugins.eduagent.android.compose)
}

android {
    namespace = "com.eduagent.core.design"
}

dependencies {
    implementation(project(":core:model"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // 三类库（00-总纲 §二；落地位置固定为 :core:design / :ui:parts）
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.vico.compose.m3)
    implementation(libs.compose.shimmer)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.lottie.compose)

    testImplementation(libs.junit)
}
