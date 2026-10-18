package com.eduagent.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * 约定插件 id：eduagent.android.application
 * 契约依据：01-契约.md §2（:app 职责）、§13 D11（minSdk 29 冻结）。
 * SDK 取值：compileSdk = 37 / targetSdk = 36 / minSdk = 29。
 *   契约 §1 原冻结 compileSdk 36，但冻结库集（Compose BOM 2026.09.00 → Compose 1.12.1 等 19 个 AAR）
 *   的 AAR 元数据硬性要求 minCompileSdk ≥ 37，经用户于第 2 步书面批准（例外申请·选项 A），
 *   仅将 compileSdk 提升至 37；targetSdk 保持 36、minSdk 保持 29，运行行为不变。
 * 任务包 02 §6：release 暂不开启混淆（R8 keep 规则待第 10 步由 r8-analyzer 技能验证后决定）。
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            compileSdk = 37
            defaultConfig {
                minSdk = 29
                targetSdk = 36
                versionCode = 1
                versionName = "1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildTypes {
                getByName("release") {
                    // 第 2 步保守策略：不混淆；R8 keep 规则待第 10 步用 r8-analyzer 技能验证后决定
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                }
            }
        }

        configureKotlinAndroidJvm()
    }
}
