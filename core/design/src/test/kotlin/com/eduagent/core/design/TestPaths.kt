package com.eduagent.core.design

import java.io.File

/**
 * 测试期文件定位工具（测试基建，非产出资产）。
 *
 * Gradle 单测的工作目录是**模块目录**（`core/design`），本工具向上回溯找仓库根下的相对路径，
 * 以免把目录层级写死。契约资产是「代码常量须一致」的基线，测试必须真读它。
 */
internal object TestPaths {

    fun repoFile(relativePath: String): File {
        var dir: File? = File(".").absoluteFile
        while (dir != null) {
            val candidate = File(dir, relativePath)
            if (candidate.isFile) return candidate
            dir = dir.parentFile
        }
        error("向上回溯未找到 $relativePath（模块工作目录应为 core/design）")
    }

    fun moduleFile(relativePath: String): File {
        val candidate = File(relativePath)
        check(candidate.isFile) { "模块内未找到 $relativePath" }
        return candidate
    }
}

/**
 * `assets/contract/motion-tokens.json` 的极简读取（只取测试要断言的键）。
 * 不引入序列化依赖：用正则从冻结 JSON 中取标量，足以证明「代码常量与机器可读基线一致」。
 */
internal object MotionTokensJson {

    private val text: String by lazy { TestPaths.repoFile("assets/contract/motion-tokens.json").readText() }

    fun duration(name: String): Int =
        match("\"$name\"\\s*:\\s*(\\d+)").groupValues[1].toInt()

    /** 弹簧的 (stiffness, dampingRatio)，须命中 `"<name>": { "stiffness": ..., "dampingRatio": ... }`。 */
    fun spring(name: String): Pair<Float, Float> {
        val m = match(
            "\"$name\"\\s*:\\s*\\{\\s*\"stiffness\"\\s*:\\s*([0-9.]+)\\s*,\\s*\"dampingRatio\"\\s*:\\s*([0-9.]+)",
        )
        return m.groupValues[1].toFloat() to m.groupValues[2].toFloat()
    }

    fun tau(name: String): Float = match("\"$name\"\\s*:\\s*([0-9.]+)").groupValues[1].toFloat()

    fun levelIds(): List<String> =
        Regex("\"id\"\\s*:\\s*\"(full|reduced|minimal)\"").findAll(text).map { it.groupValues[1] }.toList()

    private fun match(pattern: String): MatchResult =
        Regex(pattern).find(text) ?: error("motion-tokens.json 中未命中模式：$pattern")
}
