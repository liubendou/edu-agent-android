// ============================================================================
// 教育教学 Agent · 施工仓库根 settings（第 1 步）
// 契约依据：01-契约.md §2 模块结构与依赖方向
//
// 依赖方向铁律（箭头单向，core 之间不得反向依赖，core 不得依赖 feature）：
//   :app            → :feature:main
//   :feature:main   → :core:domain | :ui:parts | :core:design
//   :core:domain    → :core:data | :core:network | :core:model
//   :core:network   → :core:model
//   :core:data      → :core:model
//   :core:model     → （纯 Kotlin，无 Android 依赖）
//   :core:design    → :core:model
//   :ui:parts       → :core:design | :core:model
//
// 第 1 步只声明契约校验器模块；契约 §2 的 8 个业务模块在第 2 步追加。
// ============================================================================

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "edu-agent-kt"

include(":tools:contract-check")
