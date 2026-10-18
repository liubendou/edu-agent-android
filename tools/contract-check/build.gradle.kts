// 契约校验器模块（纯 Kotlin/JVM，无 Android 依赖）
// 契约依据：04-任务包/01-契约冻结与版本锁定.md §3
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

tasks.test {
    // 机检证据要能被人看到：把用例名与校验摘要打到标准输出
    testLogging {
        events("passed", "failed")
        showStandardStreams = true
    }
    systemProperty("contract.dir", rootProject.layout.projectDirectory.dir("assets/contract").asFile.absolutePath)
    systemProperty("versions.toml", rootProject.layout.projectDirectory.file("gradle/libs.versions.toml").asFile.absolutePath)
}
