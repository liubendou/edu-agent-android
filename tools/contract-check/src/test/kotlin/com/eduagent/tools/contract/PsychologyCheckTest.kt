package com.eduagent.tools.contract

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 正例：第 4 步产出的心理学方法论库资产必须全部通过校验（任务包 04 §4.1）。
 * 生成命令：`./gradlew psychologyCheck`（须返回 0 退出码）。
 */
class PsychologyCheckTest {

    @Test
    fun `心理学方法论库资产必须 0 违规`() {
        val report = PsychologyValidator.validate(PsychologyPaths.psychologyDir)
        println(report.render())
        assertTrue("psychologyCheck 必须 0 违规，实际：${report.violations}", report.ok)
    }

    @Test
    fun `6 族条目数必须为 14-14-14-13-13-11 且合计 79`() {
        val report = PsychologyValidator.validate(PsychologyPaths.psychologyDir)
        assertTrue("校验必须先通过才能核对分布：${report.violations}", report.ok)
        val distribution = report.evidence.first { it.startsWith("6 族分布：") }
        assertEquals(
            "6 族分布：记忆与巩固=14、理解与建构=14、练习与迁移=14、元认知=13、动机与情绪=13、习惯与节奏=11",
            distribution,
        )
    }
}
