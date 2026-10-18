// ============================================================================
// build-logic（composite build）：只承载本工程的约定插件，不产出业务代码。
// 版本目录指向根工程的 gradle/libs.versions.toml（冻结版，不在本目录另立一份）。
// 契约依据：01-契约.md §2；任务包 02 §3 / §7.6。
// ============================================================================

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "edu-agent-kt-build-logic"

include(":convention")
