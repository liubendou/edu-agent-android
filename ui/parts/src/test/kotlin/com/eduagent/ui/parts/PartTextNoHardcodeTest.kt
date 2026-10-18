package com.eduagent.ui.parts

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 文案集中（契约 §14.2；与 `:core:design` 的 `TextNoHardcodeTest` 同一纪律）。
 *
 * `:ui:parts` 的**面向用户文案一律登记在 `src/main/res/values/strings.xml`**，
 * 源码只允许出现 `R.string.*` 引用；注释里的中文说明不算违规。
 *
 * 说明：断言消息本身保持 ASCII，避免「纪律测试自己违反纪律」。
 */
class PartTextNoHardcodeTest {

    private val moduleDir: File = TestPaths.repoFile("ui/parts/build.gradle.kts").parentFile!!
    private val stringsXml: File = moduleDir.resolve("src/main/res/values/strings.xml")

    /** CJK 标点 + 汉字 + 全角符号。 */
    private val cjk = Regex("[\\u3000-\\u303F\\u4E00-\\u9FFF\\uFF00-\\uFFEF]")

    @Test
    fun kotlinSourcesCarryNoHardcodedChinese() {
        val violations = mutableListOf<String>()
        kotlinSources().forEach { file ->
            cjk.findAll(codeOnly(file)).forEach { hit ->
                violations += "${file.name}: hardcoded CJK '${hit.value}'"
            }
        }
        assertTrue(
            "part layer must not hardcode Chinese copy; use strings.xml:\n" + violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun everyReferencedStringResourceIsRegistered() {
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
        assertTrue("missing strings.xml entries:\n" + missing.joinToString("\n"), missing.isEmpty())
    }

    @Test
    fun chineseCopyLivesOnlyInStringsXml() {
        val resValues = stringsXml.parentFile!!
        val offenders = resValues.walkTopDown()
            .filter { it.isFile && it.extension == "xml" && it.name != "strings.xml" }
            .filter { cjk.containsMatchIn(it.readText()) }
            .map { it.name }
            .toList()
        assertTrue("Chinese copy must live only in values/strings.xml: $offenders", offenders.isEmpty())
    }

    @Test
    fun fallbackNoticeIsRegistered() {
        // §7 二级兜底文案必须在册（判废位退化不能没字可显示）
        assertTrue(stringsXml.readText().contains("name=\"part_fallback_notice\""))
    }

    private fun kotlinSources(): List<File> = listOf("src/main/kotlin", "src/debug/kotlin")
        .map { moduleDir.resolve(it) }
        .filter { it.isDirectory }
        .flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }

    /** Strip block and line comments; Chinese in comments is not hardcoded copy. */
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
