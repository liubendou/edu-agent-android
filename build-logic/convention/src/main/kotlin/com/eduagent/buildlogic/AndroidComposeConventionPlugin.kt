package com.eduagent.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 约定插件 id：eduagent.android.compose
 * 作用：应用 Compose 编译器插件并打开 android.buildFeatures.compose。
 * 用法：必须排在 eduagent.android.application / eduagent.android.library 之后应用（扩展需先注册）。
 * 契约依据：00-总纲 §二（纯 Compose 路线）；任务包 02 §3。
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.findByType(ApplicationExtension::class.java)?.let { application ->
            application.buildFeatures.compose = true
            return@with
        }
        extensions.findByType(LibraryExtension::class.java)?.let { library ->
            library.buildFeatures.compose = true
            return@with
        }

        error(
            "eduagent.android.compose 未找到 android 扩展：请先应用 eduagent.android.application " +
                "或 eduagent.android.library 再应用本插件（模块 ${path}）。",
        )
    }
}
