// :core:data —— 契约 §2：Room、DataStore、仓库、SM-2/FSRS
// 依赖方向：:core:data → :core:model
plugins {
    alias(libs.plugins.eduagent.android.library)
}

android {
    namespace = "com.eduagent.core.data"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
