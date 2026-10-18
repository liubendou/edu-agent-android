// :ui:parts —— 契约 §2：16 个零件 Compose 实现 + 微交互配方组件
// 依赖方向：:ui:parts → :core:design | :core:model
plugins {
    alias(libs.plugins.eduagent.android.library)
    alias(libs.plugins.eduagent.android.compose)
}

android {
    namespace = "com.eduagent.ui.parts"

    // 渲染测试（Robolectric）需要读到 res/values/strings.xml 等资源
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
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

    // 三类库消费（00-总纲 §二：UI 库 Vico / material3-adaptive，微动作库 shimmer，动画库 constraintlayout-compose）
    implementation(libs.vico.compose.m3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.compose.shimmer)
    implementation(libs.androidx.constraintlayout.compose)

    // 渲染测试（第 6 步新增依赖 Robolectric，已登记）：机检命令是 JVM 的 `:ui:parts:test`
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
