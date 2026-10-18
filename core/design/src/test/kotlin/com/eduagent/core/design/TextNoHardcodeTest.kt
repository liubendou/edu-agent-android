package com.eduagent.core.design

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 必测用例 6：文案集中（任务包 05 §4.1 / 契约 §14.2）。
 *
 * 主题 / 组件层**不得硬编码中文字符串**：所有面向用户的文案必须登记在
 * `src/main/res/values/strings.xml`，源码只允许出现 `R.string.*` 引用。
 */
class TextNoHardcodeTest {

    private val moduleDir: File = TestPaths.repoFile("core/design/build.gradle.kts").parentFile!!
    private val stringsXml: File = moduleDir.resolve("src/main/res/values/strings.xml")

    /** CJK 标点 + 汉字 + 全角符号。 */
    private val cjk = Regex("[\\u3000-\\u303F\\u4E00-\\u9FFF\\uFF00-\\uFFEF]")

    @Test
    fun kotlin_sources_carry_no_hardcoded_chinese() {
        val violations = mutableListOf<String>()
        kotlinSources().forEach { file ->
            cjk.findAll(codeOnly(file)).forEach { hit ->
                violations += "${file.name}: 硬编码中文字符 '${hit.value}'"
            }
        }
        assertTrue(
            "主题/组件层不得硬编码中文，一律走 strings.xml：\n" + violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun every_referenced_string_resource_is_registered() {
        val xml = stringsXml.readText()
        val missing = mutableListOf<String>()
        kotlinSources().forEach { file ->
            Regex("R\\.string\\.([a-z0-9_]+)").findAll(codeOnly(file)).forEach { hit ->
                val name = hit.groupValues[1]
                if (!xml.contains("name=\"$name\"")) {
                    missing += "${file.name}: R.string.$name"
                }
            }
        }
        assertTrue("以下文案未登记在 strings.xml：\n" + missing.joinToString("\n"), missing.isEmpty())
    }

    @Test
    fun chinese_copy_lives_only_in_strings_xml() {
        val resValues = stringsXml.parentFile!!
        val offenders = resValues.walkTopDown()
            .filter { it.isFile && it.extension == "xml" && it.name != "strings.xml" }
            .filter { cjk.containsMatchIn(it.readText()) }
            .map { it.name }
            .toList()
        assertTrue("中文文案只能集中在 values/strings.xml，违规文件：$offenders", offenders.isEmpty())
    }

    @Test
    fun component_layer_covers_all_ten_recipe_labels_in_strings_xml() {
        val xml = stringsXml.readText()
        (1..10).forEach { index ->
            val prefix = "recipe_" + index.toString().padStart(2, '0') + "_"
            assertTrue("strings.xml 缺少第 $index 条配方的文案键（前缀 $prefix）", xml.contains("name=\"$prefix"))
        }
    }

    private fun kotlinSources(): List<File> = listOf("src/main/kotlin", "src/debug/kotlin")
        .map { moduleDir.resolve(it) }
        .filter { it.isDirectory }
        .flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }

    /** 去掉块注释与行注释，只留代码（注释里的中文说明不算硬编码文案）。 */
    private fun codeOnly(file: File): String {
        val withoutBlock = Regex("/\\*[\\s\\S]*?\\*/").replace(file.readText()) { match ->
            if (match.value.contains('\n')) "\n" else " "
        }
        return withoutBlock.lines().joinToString("\n") { line ->
            val idx = line.indexOf("//")
            if (idx >= 0) line.substring(0, idx) else line
        }
    }
}
