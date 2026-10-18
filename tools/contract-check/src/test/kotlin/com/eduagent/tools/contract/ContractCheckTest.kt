package com.eduagent.tools.contract

import java.security.MessageDigest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 正例：真实资产 + 真实版本目录，必须 0 违规。
 * 对应 04-任务包/01-契约冻结与版本锁定.md §4.1 第 1~8 条机检断言。
 */
class ContractCheckTest {

    @Test
    fun `契约资产与冻结版本目录一致性校验通过`() {
        val contractDir = ContractPaths.contractDir
        val versionsToml = ContractPaths.versionsToml

        banner("运行环境")
        println("[env] java.version = ${System.getProperty("java.version")}")
        println("[env] java.vendor  = ${System.getProperty("java.vendor")}")
        println("[env] file.encoding= ${System.getProperty("file.encoding")}")
        println("[env] os.name      = ${System.getProperty("os.name")}")

        banner("资产清点（机器与语言）")
        val assets = contractDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }?.sortedBy { it.name }
            ?: emptyList()
        assets.forEach { file ->
            println("[asset] ${file.name}  ${file.length()} bytes  sha256=${sha256(file.readBytes()).take(16)}…")
        }
        println("[asset] 合计 ${assets.size} 份 JSON 资产（期望 7）")

        val report = ContractValidator.validate(contractDir, versionsToml)
        banner("校验证据")
        report.render().trim().lines().forEach { println("[contractCheck] $it") }
        banner("结论")
        println("[contractCheck] ok=${report.ok} violations=${report.violations.size}")

        assertTrue(
            "契约校验必须 0 违规，实际 ${report.violations.size} 项：\n" +
                report.violations.joinToString("\n") { "  × $it" },
            report.ok,
        )
    }

    private fun banner(title: String) = println("==== $title ====")

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
