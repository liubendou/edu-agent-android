// ============================================================================
// 教育教学 Agent · 施工仓库根 settings
// 契约依据：01-契约.md §2 模块结构与依赖方向
//
// 依赖方向铁律（箭头单向，core 之间不得反向依赖，core 不得依赖 feature）：
//   :app            → :feature:main
//   :feature:main   → :core:domain | :ui:parts | :core:design
//   :core:domain    → :core:data | :core:network | :core:model
//   :core:network   → :core:model
//   :core:data      → :core:model
//   :core:model     → （纯 Kotlin/JVM，无 Android 依赖）
//   :core:design    → :core:model
//   :ui:parts       → :core:design | :core:model
//
// 恰好 9 个模块 = 契约 §2 的 8 个业务模块 + 契约校验工具模块 :tools:contract-check。
// 约定插件实现在 build-logic（composite build）。
// ============================================================================

pluginManagement {
    includeBuild("build-logic")
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

include(":app")
include(":feature:main")
include(":core:domain")
include(":core:network")
include(":core:data")
include(":core:model")
include(":core:design")
include(":ui:parts")
include(":tools:contract-check")
