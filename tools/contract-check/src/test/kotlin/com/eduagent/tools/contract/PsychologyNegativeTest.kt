package com.eduagent.tools.contract

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 反例：破坏心理学资产后 `psychologyCheck` 必须报红（证明校验不是「假通过」）。
 * 对应任务包 04 §4.1 各项机检规则与 §5 红线 R-C3（无出处不入库）、R-C4（反例非空）、R-C5（每档裁决优先级）。
 */
class PsychologyNegativeTest {

    private fun withCorrupted(block: (File) -> Unit) {
        val src = PsychologyPaths.psychologyDir
        val tmp = Files.createTempDirectory("psy-negative").toFile()
        try {
            src.copyRecursively(tmp, overwrite = true)
            block(tmp)
        } finally {
            tmp.deleteRecursively()
        }
    }

    private fun validate(dir: File) = PsychologyValidator.validate(dir)

    private fun report(violations: List<PsychologyViolation>, label: String) {
        violations.forEach { println("[negative] $label violation: $it") }
    }

    /** 注入必须真的改动文件，否则用例是假通过 */
    private fun edit(file: File, from: String, to: String) {
        val original = file.readText()
        val modified = original.replace(from, to)
        assertNotEquals("注入未生效（未匹配到 «$from»），本用例会假通过", original, modified)
        file.writeText(modified)
    }

    @Test
    fun `count 与实际条目数不一致必须报红`() {
        withCorrupted { tmp ->
            edit(File(tmp, "entries.json"), "\"count\":  79", "\"count\":  78")
            println("[negative] 破坏注入：entries.json 顶层 count 79 -> 78")
            val r = validate(tmp)
            report(r.violations, "条数")
            assertFalse("count 与条目数不一致必须报红", r.ok)
            assertTrue("应产生「条数」违规", r.violations.any { it.rule == "条数" })
        }
    }

    @Test
    fun `reverse 置空必须报红`() {
        withCorrupted { tmp ->
            edit(
                File(tmp, "entries.json"),
                "\"reverse\":  \"内容尚未形成任何可提取的初始表征时（首次学习）不适用，应先讲解后入队\"",
                "\"reverse\":  \"\"",
            )
            println("[negative] 破坏注入：PSY-001 的 reverse 置空（红线 R-C4）")
            val r = validate(tmp)
            report(r.violations, "证据/反例")
            assertFalse("reverse 置空必须报红", r.ok)
            assertTrue(
                "应产生「证据/反例」reverse 违规",
                r.violations.any { it.rule == "证据/反例" && it.detail.contains("reverse") },
            )
        }
    }

    @Test
    fun `族名越界写成元认知与自我调节必须报红`() {
        withCorrupted { tmp ->
            edit(File(tmp, "entries.json"), "\"family\":  \"元认知\"", "\"family\":  \"元认知与自我调节\"")
            println("[negative] 破坏注入：元认知 族的 family 写成「元认知与自我调节」（自创第 7 族）")
            val r = validate(tmp)
            report(r.violations, "6 族")
            assertFalse("族名越界必须报红", r.ok)
            assertTrue("应产生「6 族覆盖」违规", r.violations.any { it.rule == "6 族覆盖" })
        }
    }

    @Test
    fun `card_structure 引用零件白名单之外的零件必须报红`() {
        withCorrupted { tmp ->
            edit(
                File(tmp, "entries.json"),
                "\"review_card -> verdict_card(progress_card)\"",
                "\"review_card -> foo_card\"",
            )
            println("[negative] 破坏注入：card_structure 引入 parts.json 之外的 foo_card")
            val r = validate(tmp)
            report(r.violations, "零件白名单")
            assertFalse("引用兜底外零件必须报红", r.ok)
            assertTrue(
                "应产生「零件白名单」违规",
                r.violations.any { it.rule == "零件白名单" && it.detail.contains("foo_card") },
            )
        }
    }

    @Test
    fun `families count 改错必须报红（交叉核对 + 求和）`() {
        withCorrupted { tmp ->
            edit(File(tmp, "families.json"), "\"count\": 14", "\"count\": 13")
            println("[negative] 破坏注入：families.json 三族 count 14 -> 13")
            val r = validate(tmp)
            report(r.violations, "6 族")
            assertFalse("count 与 entries 实际条数脱钩必须报红", r.ok)
            assertTrue(
                "应产生 count 交叉核对违规",
                r.violations.any { it.rule == "6 族覆盖" && it.detail.contains("不一致") },
            )
            assertTrue(
                "应产生 count 求和违规",
                r.violations.any { it.rule == "6 族覆盖" && it.detail.contains("之和") },
            )
        }
    }

    @Test
    fun `INDEX 少一行必须报红`() {
        withCorrupted { tmp ->
            val file = File(tmp, "INDEX.md")
            val original = file.readText().trimEnd('\n')
            val modified = original.substringBeforeLast('\n') + "\n"
            assertNotEquals("注入未生效，本用例会假通过", original, modified.trimEnd('\n'))
            file.writeText(modified)
            println("[negative] 破坏注入：INDEX.md 删掉最后一行（79 -> 78 行）")
            val r = validate(tmp)
            report(r.violations, "索引行数")
            assertFalse("索引行数不足必须报红", r.ok)
            assertTrue("应产生「索引行数」违规", r.violations.any { it.rule == "索引行数" })
        }
    }

    @Test
    fun `某档裁决优先级缺一族必须报红`() {
        withCorrupted { tmp ->
            edit(
                File(tmp, "retrieval-rules.json"),
                "\"boost\": [\"练习与迁移\", \"元认知\", \"动机与情绪\", \"记忆与巩固\", \"理解与建构\", \"习惯与节奏\"]",
                "\"boost\": [\"练习与迁移\", \"元认知\", \"动机与情绪\", \"记忆与巩固\", \"理解与建构\"]",
            )
            println("[negative] 破坏注入：boost 档裁决优先级删掉「习惯与节奏」（红线 R-C5）")
            val r = validate(tmp)
            report(r.violations, "裁决优先级")
            assertFalse("每档优先级缺族必须报红", r.ok)
            assertTrue(
                "应产生「裁决优先级」违规",
                r.violations.any { it.rule == "裁决优先级" && it.detail.contains("boost") },
            )
        }
    }
}
