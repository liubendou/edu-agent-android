// :core:domain —— 契约 §2：决策（今天学什么）、validator、兜底链、调度、流式内核
// 依赖方向：:core:domain → :core:data | :core:network | :core:model
plugins {
    alias(libs.plugins.eduagent.android.library)
}

android {
    namespace = "com.eduagent.core.domain"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:model"))

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
