package com.eduagent.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * 约定插件 id：eduagent.android.library
 * 契约依据：01-契约.md §13 D11（minSdk 29 冻结）。
 * SDK 取值：compileSdk = 37 / minSdk = 29。
 *   契约 §1 原冻结 compileSdk 36，但冻结库集的 AAR 元数据硬性要求 minCompileSdk ≥ 37，
 *   经用户于第 2 步书面批准（例外申请·选项 A）仅提升 compileSdk；minSdk 保持 29。
 * 说明：namespace 由各模块自行声明（模块专属，约定插件不代设）。
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk = 37
            defaultConfig {
                minSdk = 29
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
        }

        configureKotlinAndroidJvm()
    }
}
