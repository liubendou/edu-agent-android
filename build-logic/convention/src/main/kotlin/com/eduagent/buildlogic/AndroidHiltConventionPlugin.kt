package com.eduagent.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 约定插件 id：eduagent.android.hilt
 * 作用：应用 Hilt Gradle 插件与 KSP（本工程注解处理统一走 KSP2，契约 §1）。
 * 说明：hilt-android / hilt-compiler 依赖由各模块自行声明（避免约定插件隐式塞依赖）。
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("com.google.devtools.ksp")
    }
}
