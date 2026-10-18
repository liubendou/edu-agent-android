package com.eduagent.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Android 模块的 Kotlin 编译目标统一为 JVM 17（与官方脚手架一致，任务包 02 §7.7）。
 * AGP 9 内建 Kotlin 支持，本工程不单独应用 org.jetbrains.kotlin.android。
 */
internal fun Project.configureKotlinAndroidJvm() {
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
