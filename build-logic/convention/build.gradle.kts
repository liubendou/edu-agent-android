// ============================================================================
// 约定插件实现模块。
// 依赖说明：AGP / KGP / Compose / KSP / Hilt 的 DSL 类型只在编译期引用（compileOnly），
//           版本一律取自冻结版本目录（gradle/libs.versions.toml），不引入任何新版本。
// ============================================================================

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "eduagent.android.application"
            implementationClass = "com.eduagent.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "eduagent.android.library"
            implementationClass = "com.eduagent.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "eduagent.android.compose"
            implementationClass = "com.eduagent.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "eduagent.android.hilt"
            implementationClass = "com.eduagent.buildlogic.AndroidHiltConventionPlugin"
        }
        register("kotlinJvm") {
            id = "eduagent.kotlin.jvm"
            implementationClass = "com.eduagent.buildlogic.KotlinJvmConventionPlugin"
        }
    }
}
