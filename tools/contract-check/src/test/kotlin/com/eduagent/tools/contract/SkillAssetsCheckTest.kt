package com.eduagent.tools.contract

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 正例：真实五档技能资产 + 真实契约资产，必须 0 违规。
 * 对应 04-任务包/03-五档技能模板资产.md §4.1 的 7 项机检。
 */
class SkillAssetsCheckTest {

    @Test
    fun `五档技能模板资产校验通过`() {
        val skillsDir = SkillPaths.skillsDir
        val contractDir = ContractPaths.contractDir

        banner("运行环境")
        println("[env] java.version = ${System.getProperty("java.version")}")
        println("[env] file.encoding = ${System.getProperty("file.encoding")}")
        println("[env] skills.dir     = ${skillsDir.absolutePath}")

        banner("资产清点")
        SkillAssetsValidator.EXPECTED_MODES.forEach { mode ->
            val dir = File(skillsDir, mode)
            val json = File(dir, "skill.json")
            val prompt = File(dir, "prompt.md")
            val refs = File(dir, "references").listFiles()?.filter { it.isFile && it.name.endsWith(".md") } ?: emptyList()
            val title = prompt.takeIf { it.isFile }?.readText()?.lineSequence()?.firstOrNull { it.isNotBlank() }
            println(
                "[asset] skills/$mode  skill.json=${json.length()}B  prompt.md=${prompt.length()}B  " +
                    "references=${refs.size}份  首行=«$title»",
            )
        }

        val report = SkillAssetsValidator.validate(skillsDir, contractDir)
        banner("校验证据")
        report.render().trim().lines().forEach { println("[skillAssetsCheck] $it") }

        val dirCount = skillsDir.listFiles()?.count { it.isDirectory } ?: 0
        val fieldMissing = report.violations.count { it.rule == "JSON 字段" && it.detail.contains("缺字段") }
        val signatureDiff = report.violations.count { it.rule == "签名一致" }
        val tauDiff = report.violations.count { it.rule == "tau 一致" }
        val lynxHit = report.violations.count { it.rule == "文本合规" }
        banner("结论")
        println(
            "[skillAssetsCheck] 目录数=$dirCount  字段缺失=$fieldMissing  " +
                "签名不一致=$signatureDiff  tau 不一致=$tauDiff  文本合规违规=$lynxHit  " +
                "ok=${report.ok}  violations=${report.violations.size}",
        )

        assertEquals("assets/skills 下应恰好 5 个档目录", 5, dirCount)
        assertTrue(
            "五档技能资产必须 0 违规，实际 ${report.violations.size} 项：\n" +
                report.violations.joinToString("\n") { "  × $it" },
            report.ok,
        )
    }

    private fun banner(title: String) = println("==== $title ====")
}
