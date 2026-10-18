package com.eduagent.tools.contract

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 反例：破坏资产或契约矩阵后，`skillAssetsCheck` 必须报红（证明校验不是「假通过」）。
 * 对应 04-任务包/03-五档技能模板资产.md §4.1 / §5（R-T9 签名不得放宽、文本合规、boost 分叉），
 * 以及评审修正（2026-10-18）新增的三条：签名顺序、逐字节同源、矩阵映射。
 */
class SkillAssetsNegativeTest {

    private fun withCorruptedSkills(block: (File) -> Unit) {
        val src = SkillPaths.skillsDir
        val tmp = Files.createTempDirectory("skills-negative").toFile()
        try {
            src.copyRecursively(tmp, overwrite = true)
            block(tmp)
        } finally {
            tmp.deleteRecursively()
        }
    }

    private fun withCorruptedContract(block: (File) -> Unit) {
        val src = SkillPaths.contractDir
        val tmp = Files.createTempDirectory("contract-negative").toFile()
        try {
            src.copyRecursively(tmp, overwrite = true)
            block(tmp)
        } finally {
            tmp.deleteRecursively()
        }
    }

    private fun validate(skills: File, contract: File = SkillPaths.contractDir) =
        SkillAssetsValidator.validate(skills, contract)

    private fun report(violations: List<SkillViolation>, label: String) {
        violations.forEach { println("[negative] $label violation: $it") }
    }

    @Test
    fun `放宽签名（删掉 required 里的一个零件）必须报红`() {
        withCorruptedSkills { tmp ->
            val file = File(tmp, "quick/skill.json")
            file.writeText(file.readText().replace("\"keypoint_card\", \"explain_card\"", "\"keypoint_card\""))
            println("[negative] 破坏注入：quick required_parts 删掉 explain_card（放宽签名，红线 R-T9）")

            val r = validate(tmp)
            report(r.violations, "签名")
            assertFalse("放宽签名后校验必须报红", r.ok)
            assertTrue("应产生「签名一致」违规", r.violations.any { it.rule == "签名一致" && it.detail.contains("required_parts") })
        }
    }

    @Test
    fun `残留 Lynx 字样必须报红`() {
        withCorruptedSkills { tmp ->
            val file = File(tmp, "review/prompt.md")
            file.appendText("\n<!-- lynx 前端残留 -->\n")
            println("[negative] 破坏注入：review/prompt.md 追加 lynx 字样")

            val r = validate(tmp)
            report(r.violations, "文本")
            assertFalse("出现 Lynx 字样后校验必须报红", r.ok)
            assertTrue("应产生「文本合规」违规", r.violations.any { it.rule == "文本合规" && it.detail.contains("lynx") })
        }
    }

    @Test
    fun `boost 缺长期分支关键词必须报红`() {
        withCorruptedSkills { tmp ->
            val file = File(tmp, "boost/skill.json")
            file.writeText(file.readText().replace("LONG", "长期分支"))
            println("[negative] 破坏注入：boost skill.json 把 LONG 换成「长期分支」（缺分叉关键词）")

            val r = validate(tmp)
            report(r.violations, "分叉")
            assertFalse("boost 缺 LONG 关键词必须报红", r.ok)
            assertTrue("应产生「boost 分叉」违规", r.violations.any { it.rule == "boost 分叉" && it.detail.contains("LONG") })
        }
    }

    /** 评审点 2：boost 的「或」项顺序必须逐字等于任务包 §3.3 表（或项在前），不得反过来。 */
    @Test
    fun `boost「或」项顺序改成题卡在前必须报红`() {
        withCorruptedSkills { tmp ->
            val file = File(tmp, "boost/skill.json")
            file.writeText(
                file.readText().replace(
                    "\"required_parts\": [\"diagnose_card|plan_panel\", \"quiz_card\"],",
                    "\"required_parts\": [\"quiz_card\", \"diagnose_card|plan_panel\"],",
                ),
            )
            println("[negative] 破坏注入：boost required_parts 顺序改成 [quiz_card, diagnose_card|plan_panel]（与任务包 §3.3 表相反）")

            val r = validate(tmp)
            report(r.violations, "顺序")
            assertFalse("顺序与冻结期望不符必须报红", r.ok)
            assertTrue(
                "应产生「签名一致」或「矩阵映射」顺序类违规",
                r.violations.any { it.rule == "签名一致" && it.detail.contains("逐字") } ||
                    r.violations.any { it.rule == "矩阵映射" && it.detail.contains("位置") },
            )
        }
    }

    /** 评审点 4：prompt.md 与 instruction_skeleton 必须归一化行尾后逐字节同源，防止未来哈希漂移误报。 */
    @Test
    fun `prompt_md 与 instruction_skeleton 非逐字节同源必须报红`() {
        withCorruptedSkills { tmp ->
            val file = File(tmp, "quick/prompt.md")
            file.appendText("\n<!-- 尾部多出一行：用于检验同源断言 -->\n")
            println("[negative] 破坏注入：quick/prompt.md 尾部多出一行（与 instruction_skeleton 脱钩）")

            val r = validate(tmp)
            report(r.violations, "同源")
            assertFalse("prompt.md 与 skeleton 脱钩必须报红", r.ok)
            assertTrue(
                "应产生「文本合规」非逐字节同源违规",
                r.violations.any { it.rule == "文本合规" && it.detail.contains("非逐字节同源") },
            )
        }
    }

    /**
     * 评审点 2 的根因：期望值改为硬编码后，矩阵一旦漂移必须被抓到。
     * 这里把 signature-matrix.json 的 boost `required_any_of` 顺序反过来，模拟「契约被就地改动」。
     */
    @Test
    fun `契约矩阵 required_any_of 漂移必须报红`() {
        withCorruptedContract { contract ->
            val file = File(contract, "signature-matrix.json")
            val original = file.readText()
            val modified = original.replace(
                "\"required_any_of\": [\"diagnose_card\", \"plan_panel\"]",
                "\"required_any_of\": [\"plan_panel\", \"diagnose_card\"]",
            )
            file.writeText(modified)
            println("[negative] 破坏注入：signature-matrix.json boost required_any_of 顺序反转")

            assertNotEquals("注入必须真的改动契约文件，否则本用例是假通过", original, modified)

            val r = validate(SkillPaths.skillsDir, contract)
            report(r.violations, "矩阵")
            assertFalse("矩阵漂移必须报红", r.ok)
            assertTrue(
                "应产生「矩阵映射」违规",
                r.violations.any { it.rule == "矩阵映射" },
            )
        }
    }
}
