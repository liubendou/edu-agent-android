// ============================================================================
// 教育教学 Agent · 根构建脚本
// 契约依据：01-契约.md §2（模块与依赖方向）；04-任务包/02-工程骨架与三处一致性校验.md §3 / §4.1 / §8
//
// 本脚本做三件事：
//   1) 以 apply false 把 AGP / KGP / Compose / KSP / Hilt 登记进本构建的插件解析，
//      供 build-logic 的约定插件按 id 应用；约定插件内部不声明版本，版本唯一来源是版本目录。
//   2) 注册两个聚合入口：contractCheck（契约校验）、ci（契约校验 + 全模块测试 + debug 构建）。
//   3) 在根工程接入 detekt，一次命令扫描全仓 Kotlin 源。
// ============================================================================

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.gradle) apply false
    alias(libs.plugins.detekt)
}

// ---- detekt：一次命令扫描全仓 Kotlin 源（任务包 02 §4.1 机检项）----------------
detekt {
    buildUponDefaultConfig = true
    baseline = file("config/detekt/baseline.xml")
    source.setFrom(
        fileTree(rootDir) {
            include("**/src/**/*.kt")
            exclude("**/build/**")
        },
    )
}

// ---- 聚合入口 -----------------------------------------------------------------

// 契约校验入口（第 1 步产出，第 2 步沿用）：契约资产 + 冻结版本目录 + 端侧常量三处一致性
tasks.register("contractCheck") {
    group = "verification"
    description = "契约资产、冻结版本目录与端侧常量一致性校验"
    dependsOn(":tools:contract-check:test")
}

// 五档技能模板资产校验入口（第 3 步产出）：assets/skills 5 档 schema + 签名矩阵/tau 一致性 + 文本合规
tasks.register("skillAssetsCheck") {
    group = "verification"
    description = "五档技能模板资产校验（assets/skills 5 档）"
    dependsOn(":tools:contract-check:skillAssetsTest")
}

// 心理学方法论库资产校验入口（第 4 步产出）：assets/psychology 4 份资产 schema + 6 族计数 + 索引格式 + 裁决优先级
tasks.register("psychologyCheck") {
    group = "verification"
    description = "心理学方法论库资产校验（assets/psychology 79 条 / 6 族 / 4 份资产）"
    dependsOn(":tools:contract-check:psychologyTest")
}

// 本地 CI 一键入口：契约校验 + 技能模板资产校验 + 全部模块单元测试 + :app debug 构建（任一失败即非零退出）
//
// 注意：Gradle 会为 :core / :ui / :feature / :tools 这类**容器工程**也建立 Project 实例，
// 但它们没有 test 任务。故用「目录下存在 build.gradle.kts」筛出真正的叶子模块，避免
// "Task with name 'test' not found in project ':core'"。
tasks.register("ci") {
    group = "verification"
    description = "本地 CI：contractCheck + 全部模块 test + :app assembleDebug"
    val leafModules = subprojects.filter { it.projectDir.resolve("build.gradle.kts").exists() }
    dependsOn("contractCheck")
    dependsOn("skillAssetsCheck")
    dependsOn("psychologyCheck")
    dependsOn(leafModules.map { "${it.path}:test" })
    dependsOn(":app:assembleDebug")
}
