package com.eduagent.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * 约定插件 id：eduagent.kotlin.jvm
 * 作用：纯 Kotlin/JVM 模块的统一编译目标（Java 与 Kotlin 均为 JVM 17，与官方脚手架一致）。
 * Java 与 Kotlin 目标必须同值，否则 Gradle 会报 "Inconsistent JVM Target Compatibility"。
 * 用于 :core:model（契约 §2「纯 Kotlin，无 Android 依赖」）与 :tools:contract-check。
 */
class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
