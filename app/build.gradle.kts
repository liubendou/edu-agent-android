// :app —— 契约 §2：单 Activity 宿主、导航图、Application、DI 装配
// 依赖方向：:app → :feature:main（不得直接依赖 :core:* / :ui:*）
plugins {
    alias(libs.plugins.eduagent.android.application)
    alias(libs.plugins.eduagent.android.compose)
    alias(libs.plugins.eduagent.android.hilt)
}

android {
    namespace = "com.eduagent.app"

    defaultConfig {
        applicationId = "com.eduagent.app"
    }
}

dependencies {
    implementation(project(":feature:main"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}
