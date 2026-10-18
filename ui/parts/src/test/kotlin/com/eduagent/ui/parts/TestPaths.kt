package com.eduagent.ui.parts

import java.io.File

/**
 * 测试期文件定位工具（测试基建，非产出资产）。
 *
 * Gradle 单测的工作目录是**模块目录**（`ui/parts`），本工具向上回溯找仓库根下的相对路径，
 * 以免把目录层级写死。
 */
internal object TestPaths {

    fun repoFile(relativePath: String): File {
        var dir: File? = File(".").absoluteFile
        while (dir != null) {
            val candidate = File(dir, relativePath)
            if (candidate.isFile) return candidate
            dir = dir.parentFile
        }
        error("repo file not found: $relativePath (module cwd should be ui/parts)")
    }
}
