// :core:network —— 契约 §2：DeepSeek 客户端、八段装配、SSE、升级策略、埋点上报
// 依赖方向：:core:network → :core:model
plugins {
    alias(libs.plugins.eduagent.android.library)
}

android {
    namespace = "com.eduagent.core.network"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
