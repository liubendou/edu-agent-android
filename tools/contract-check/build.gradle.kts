// 契约校验器模块（纯 Kotlin/JVM，无 Android 依赖）
// 契约依据：04-任务包/01-契约冻结与版本锁定.md §3
plugins {
    alias(libs.plugins.eduagent.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    // 第 2 步：三处一致性校验（红线 R-T3/R-T8）需要 :core:model 的 PartTypes / CatalogVersion 常量
    testImplementation(project(":core:model"))
    testImplementation(libs.junit)
}

tasks.test {
    // 机检证据要能被人看到：把用例名与校验摘要打到标准输出
    testLogging {
        events("passed", "failed")
        showStandardStreams = true
    }
    systemProperty("contract.dir", rootProject.layout.projectDirectory.dir("assets/contract").asFile.absolutePath)
    systemProperty("skills.dir", rootProject.layout.projectDirectory.dir("assets/skills").asFile.absolutePath)
    systemProperty("psychology.dir", rootProject.layout.projectDirectory.dir("assets/psychology").asFile.absolutePath)
    systemProperty("versions.toml", rootProject.layout.projectDirectory.file("gradle/libs.versions.toml").asFile.absolutePath)
}

// 第 3 步：五档技能模板资产校验（任务包 03 §4.1）——独立测试任务，供根工程 `skillAssetsCheck` 聚合
tasks.register<Test>("skillAssetsTest") {
    group = "verification"
    description = "五档技能模板资产校验（assets/skills 5 档 × skill.json / prompt.md / references）"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("com.eduagent.tools.contract.SkillAssets*")
    }
    testLogging {
        events("passed", "failed")
        showStandardStreams = true
    }
    systemProperty("contract.dir", rootProject.layout.projectDirectory.dir("assets/contract").asFile.absolutePath)
    systemProperty("skills.dir", rootProject.layout.projectDirectory.dir("assets/skills").asFile.absolutePath)
}

// 第 4 步：心理学方法论库资产校验（任务包 04 §4.1）——独立测试任务，供根工程 `psychologyCheck` 聚合
tasks.register<Test>("psychologyTest") {
    group = "verification"
    description = "心理学方法论库资产校验（assets/psychology：entries/families/retrieval-rules/INDEX 共 4 份）"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("com.eduagent.tools.contract.Psychology*")
    }
    testLogging {
        events("passed", "failed")
        showStandardStreams = true
    }
    systemProperty("psychology.dir", rootProject.layout.projectDirectory.dir("assets/psychology").asFile.absolutePath)
}
