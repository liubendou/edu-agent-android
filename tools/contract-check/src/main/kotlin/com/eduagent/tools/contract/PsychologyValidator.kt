package com.eduagent.tools.contract

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/**
 * 心理学方法论库资产校验器（第 4 步机检实现）。
 * 任务包依据：04-任务包/04-心理学方法论库79条.md §3.2 / §3.3 / §3.4 / §3.5 / §4.1；
 * 契约依据：01-契约.md §4（十六零件）、§5（五档枚举）、§11 R-C3/R-C4/R-C5/R-T2、§13 D8/D19。
 *
 * 设计要点（沿用第 3 步评审教训）：**期望值一律取本文件的硬编码常量**，不从 assets/contract 下的
 * JSON 运行时拼装——否则期望值与被测对象同源，校验会自引用循环、必然通过。
 * 6 族名取契约 §13 D8 口径（简写「元认知」），与第 3 步 SkillAssetsValidator.PSY_FAMILIES 一致。
 *
 * 只读 assets/psychology 目录下的资产文件，不做任何写操作。
 */
class PsychologyViolation(val rule: String, val detail: String) {
    override fun toString(): String = "[$rule] $detail"
}

class PsychologyReport(val evidence: List<String>, val violations: List<PsychologyViolation>) {
    val ok: Boolean get() = violations.isEmpty()

    fun render(): String = buildString {
        appendLine("---- 心理学方法论库资产校验证据 ----")
        evidence.forEach { appendLine("  · $it") }
        if (ok) {
            appendLine("结论：PASS（0 项违规）")
        } else {
            appendLine("结论：FAIL（${violations.size} 项违规）")
            violations.forEach { appendLine("  × $it") }
        }
    }
}

/** 校验期内部持有的单条投影（仅用于 INDEX.md 与 families.json 的交叉核对） */
internal data class PsyEntry(
    val id: String,
    val name: String,
    val family: String,
    val modes: List<String>,
    val principle: String,
)

object PsychologyValidator {

    /** 6 族名（契约 §13 D8 口径，逐字；与 SkillAssetsValidator.PSY_FAMILIES 同源） */
    val PSY_FAMILIES: List<String> = listOf(
        "记忆与巩固", "理解与建构", "练习与迁移", "元认知", "动机与情绪", "习惯与节奏",
    )

    /** 五档枚举：直接用 ContractValidator 的硬编码 §5 取值（避免再从 signature-matrix.json 读成循环） */
    val EXPECTED_MODES: List<String> = ContractValidator.EXPECTED_MODES

    /** 零件白名单：16 个 type 用 ContractValidator 的硬编码区 */
    val PART_TYPES: List<String> = ContractValidator.EXPECTED_PART_TYPES

    /** 任务包 04 §0：条数严格 = 79 */
    const val ENTRY_COUNT = 79

    /** 资产自版本（D1 受版本控制资产，首版统一 1.0.0） */
    const val VERSION = "1.0.0"

    /** 任务包 04 §3.2：单条 11 个 key，逐字、不得缺、不得多 */
    val ENTRY_KEYS: List<String> = listOf(
        "id", "name", "family", "principle", "modes", "trigger",
        "injection", "evidence", "reverse", "conflict_priority",
    )

    /** 任务包 04 §3.2：injection 三子键，全齐且非空 */
    val INJECTION_KEYS: List<String> = listOf("instruction", "card_structure", "rhythm")

    /** 任务包 04 §3.5：索引行格式，逐字冻结（与 INDEX.md 同一格式） */
    const val INDEX_LINE_FORMAT = "id | 名称 | 适用档 | 一句话原理"
    const val INDEX_SEPARATOR = " | "

    /** 任务包 04 §3.3：families.json 单族 4 键 */
    val FAMILY_KEYS: List<String> = listOf("name", "covers", "modes", "count")

    /** 任务包 04 §3.4：retrieval-rules.json 顶层 7 键 */
    val RETRIEVAL_KEYS: List<String> = listOf(
        "version", "index_line_format", "injection_count", "retrieval_input",
        "selection_rules", "reverse_filter_rules", "conflict_priority_by_mode",
    )

    /** 任务包 04 §3.4：检索输入四类固定，不得增删 */
    val RETRIEVAL_INPUT_EXPECTED: List<String> = listOf("mode", "skill", "user_state", "reverse_filter")

    /** 任务包 04 §3.4 / D19 / C2.3：注入条数下限、上限 */
    const val INJECTION_MIN = 3
    const val INJECTION_MAX = 8

    /** 任务包 04 §3.3：每族 count ≥ 5 */
    const val MIN_PER_FAMILY = 5

    /** 任务包 04 §4.1 文本合规：全文命中数必须为 0（Lynx/TS 属前版技术栈痕迹） */
    val FORBIDDEN_TEXT: List<String> = listOf("lynx", "Lynx", "TS", "TypeScript")

    private val ID_REGEX = Regex("^PSY-\\d{3}$")
    private val DATE_REGEX = Regex("\\d{4}-\\d{2}-\\d{2}")
    private val UUID_REGEX = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-")
    private val PART_TOKEN_REGEX = Regex("[a-z_]+")
    private val EVIDENCE_YEAR_REGEX = Regex("\\b(1[6-9]\\d{2}|20\\d{2})\\b")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun validate(psychologyDir: File): PsychologyReport {
        val v = mutableListOf<PsychologyViolation>()
        val e = mutableListOf<String>()

        e += "期望值来源：PsychologyValidator 硬编码常量（任务包 04 §3；6 族名取契约 §13 D8 口径）"
        e += "6 族名（逐字）：${PSY_FAMILIES.joinToString("/")}"
        e += "五档枚举：${EXPECTED_MODES.joinToString("/")}"
        e += "零件白名单：${PART_TYPES.size} 个 type（ContractValidator.EXPECTED_PART_TYPES）"

        if (!psychologyDir.isDirectory) {
            v += PsychologyViolation("目录齐全", "目录不存在：${psychologyDir.path}")
            return PsychologyReport(e, v)
        }

        val entriesFile = File(psychologyDir, "entries.json")
        val familiesFile = File(psychologyDir, "families.json")
        val rulesFile = File(psychologyDir, "retrieval-rules.json")
        val indexFile = File(psychologyDir, "INDEX.md")
        listOf(entriesFile, familiesFile, rulesFile, indexFile).forEach {
            if (!it.isFile) v += PsychologyViolation("目录齐全", "${it.name} 不存在（任务包 04 §3.1）")
        }

        val entriesObj = readObject(entriesFile, v)
        val familiesObj = readObject(familiesFile, v)
        val rulesObj = readObject(rulesFile, v)

        val entries = if (entriesObj != null) checkEntries(entriesObj, v, e) else emptyList()
        if (familiesObj != null) checkFamilies(familiesObj, entries, v, e)
        if (rulesObj != null) checkRetrievalRules(rulesObj, v, e)
        checkIndex(indexFile, entries, v, e)
        checkTextCompliance(psychologyDir, e, v)

        return PsychologyReport(e, v)
    }

    // ---------------------------------------------------------------- entries

    private fun checkEntries(root: JsonObject, v: MutableList<PsychologyViolation>, e: MutableList<String>): List<PsyEntry> {
        val topUnknown = root.keys.filterNot { it in listOf("version", "count", "entries") }
        if (topUnknown.isNotEmpty()) v += PsychologyViolation("JSON 字段", "entries.json 顶层出现未知键：$topUnknown")
        if (root["version"]?.str() != VERSION) {
            v += PsychologyViolation("版本", "entries.json version=«${root["version"]?.str()}»，期望 «$VERSION»")
        }

        val arr = root["entries"] as? JsonArray ?: run {
            v += PsychologyViolation("JSON 字段", "entries.json 缺少 entries 数组")
            return emptyList()
        }
        e += "entries.json：条目数=${arr.size}（期望 $ENTRY_COUNT）"
        if (arr.size != ENTRY_COUNT) {
            v += PsychologyViolation("条数", "entries.json 条目数=${arr.size}，硬约束必须严格 = $ENTRY_COUNT（验收 C2.1）")
        }
        val count = root["count"]?.int()
        if (count != arr.size) {
            v += PsychologyViolation("条数", "entries.json count=$count 与实际条目数 ${arr.size} 不一致")
        }

        val ids = mutableListOf<String>()
        val out = mutableListOf<PsyEntry>()
        val familyTally = linkedMapOf<String, Int>()

        arr.forEachIndexed { i, element ->
            val obj = element as? JsonObject ?: run {
                v += PsychologyViolation("JSON 字段", "第 ${i + 1} 条不是对象")
                return@forEachIndexed
            }
            val label = obj["id"]?.str() ?: "#${i + 1}"
            val keys = obj.keys.toList()
            val missing = ENTRY_KEYS.filterNot { it in keys }
            val unknown = keys.filterNot { it in ENTRY_KEYS }
            if (missing.isNotEmpty()) v += PsychologyViolation("JSON 字段", "$label 缺 key：$missing")
            if (unknown.isNotEmpty()) v += PsychologyViolation("JSON 字段", "$label 出现未知 key：$unknown")

            val id = obj["id"]?.str().orEmpty()
            if (!ID_REGEX.matches(id)) v += PsychologyViolation("ID", "$label id 必须形如 PSY-001（零填充 3 位）")
            ids += id

            val name = obj["name"]?.str().orEmpty()
            if (name.isBlank()) v += PsychologyViolation("字段完备", "$label name 为空")

            val family = obj["family"]?.str().orEmpty()
            if (family !in PSY_FAMILIES) {
                v += PsychologyViolation("6 族覆盖", "$label family=«$family» 不在 6 族名 $PSY_FAMILIES")
            }
            familyTally[family] = (familyTally[family] ?: 0) + 1

            val principle = obj["principle"]?.str().orEmpty()
            if (principle.isBlank()) v += PsychologyViolation("字段完备", "$label principle 为空")

            val modes = (obj["modes"] as? JsonArray)?.mapNotNull { it.str() }
            if (modes.isNullOrEmpty()) {
                v += PsychologyViolation("枚举合法", "$label modes 必须是非空数组")
            } else {
                val bad = modes.filterNot { it in EXPECTED_MODES }
                if (bad.isNotEmpty()) v += PsychologyViolation("枚举合法", "$label modes 含非枚举值：$bad（须 ∈ $EXPECTED_MODES）")
                if (modes.toSet().size != modes.size) v += PsychologyViolation("枚举合法", "$label modes 有重复项：$modes")
            }

            if (obj["trigger"]?.str().isNullOrBlank()) v += PsychologyViolation("字段完备", "$label trigger 为空")

            checkInjection(label, obj["injection"] as? JsonObject, v)

            if (obj["evidence"]?.str().isNullOrBlank()) {
                v += PsychologyViolation("证据/反例", "$label evidence 为空（红线 R-C3：无出处不入库）")
            }
            if (obj["reverse"]?.str().isNullOrBlank()) {
                v += PsychologyViolation("证据/反例", "$label reverse 为空（红线 R-C4）")
            }
            val cp = obj["conflict_priority"]?.int()
            if (cp == null || cp !in 1..5) {
                v += PsychologyViolation("字段完备", "$label conflict_priority=$cp 不在整数 1..5")
            }

            out += PsyEntry(id, name, family, modes.orEmpty(), principle)
        }

        ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach {
            v += PsychologyViolation("ID", "id 重复：$it")
        }
        val expectedIds = (1..ENTRY_COUNT).map { "PSY-%03d".format(it) }
        if (ids.sorted() != expectedIds) {
            val missing = expectedIds.filterNot { it in ids }
            val extra = ids.filterNot { it in expectedIds }
            v += PsychologyViolation(
                "ID",
                "id 未全覆盖 PSY-001~PSY-079：缺失=${missing.take(6)}… 多出=${extra.take(6)}…",
            )
        }
        if (ids.isNotEmpty() && ids != ids.sorted()) {
            v += PsychologyViolation("ID", "entries 顺序不是 id 升序（索引要求 id 升序）")
        }
        e += "6 族分布：${PSY_FAMILIES.joinToString("、") { "$it=${familyTally[it] ?: 0}" }}"
        return out
    }

    private fun checkInjection(label: String, inj: JsonObject?, v: MutableList<PsychologyViolation>) {
        if (inj == null) {
            v += PsychologyViolation("字段完备", "$label injection 必须是对象")
            return
        }
        val keys = inj.keys.toList()
        val missing = INJECTION_KEYS.filterNot { it in keys }
        val unknown = keys.filterNot { it in INJECTION_KEYS }
        if (missing.isNotEmpty()) v += PsychologyViolation("字段完备", "$label injection 缺子键：$missing")
        if (unknown.isNotEmpty()) v += PsychologyViolation("字段完备", "$label injection 未知子键：$unknown")
        INJECTION_KEYS.forEach { k ->
            if (inj[k]?.str().isNullOrBlank()) v += PsychologyViolation("字段完备", "$label injection.$k 为空")
        }
        val cs = inj["card_structure"]?.str().orEmpty()
        val tokens = PART_TOKEN_REGEX.findAll(cs).map { it.value }.toList()
        if (tokens.isEmpty()) {
            v += PsychologyViolation("零件白名单", "$label card_structure 未引用任何零件 type：«$cs»")
        } else {
            val bad = tokens.filterNot { it in PART_TYPES }
            if (bad.isNotEmpty()) {
                v += PsychologyViolation("零件白名单", "$label card_structure 出现 parts.json 之外的零件：$bad")
            }
        }
    }

    // --------------------------------------------------------------- families

    private fun checkFamilies(
        root: JsonObject,
        entries: List<PsyEntry>,
        v: MutableList<PsychologyViolation>,
        e: MutableList<String>,
    ) {
        if (root["version"]?.str() != VERSION) {
            v += PsychologyViolation("版本", "families.json version=«${root["version"]?.str()}»，期望 «$VERSION»")
        }
        val arr = root["families"] as? JsonArray ?: run {
            v += PsychologyViolation("JSON 字段", "families.json 缺少 families 数组")
            return
        }
        e += "families.json：族数=${arr.size}（期望 ${PSY_FAMILIES.size}）"
        if (arr.size != PSY_FAMILIES.size) {
            v += PsychologyViolation("6 族覆盖", "families.json 族数=${arr.size}，必须逐字为 ${PSY_FAMILIES.size} 族")
        }
        val names = arr.mapNotNull { (it as? JsonObject)?.get("name")?.str() }
        if (names.toSet() != PSY_FAMILIES.toSet()) {
            v += PsychologyViolation("6 族覆盖", "families.json 族名=$names，必须逐字等于 $PSY_FAMILIES")
        }

        val actualTally = entries.groupingBy { it.family }.eachCount()
        var sum = 0
        arr.forEach { element ->
            val obj = element as? JsonObject ?: return@forEach
            val nm = obj["name"]?.str() ?: "?"
            val keys = obj.keys.toList()
            val missing = FAMILY_KEYS.filterNot { it in keys }
            val unknown = keys.filterNot { it in FAMILY_KEYS }
            if (missing.isNotEmpty()) v += PsychologyViolation("JSON 字段", "families.json «$nm» 缺 key：$missing")
            if (unknown.isNotEmpty()) v += PsychologyViolation("JSON 字段", "families.json «$nm» 未知 key：$unknown")
            if (obj["covers"]?.str().isNullOrBlank()) v += PsychologyViolation("JSON 字段", "families.json «$nm» covers 为空")

            val c = obj["count"]?.int()
            if (c == null) {
                v += PsychologyViolation("6 族覆盖", "families.json «$nm» count 非整数")
            } else {
                sum += c
                if (c < MIN_PER_FAMILY) {
                    v += PsychologyViolation("6 族覆盖", "families.json «$nm» count=$c，每族必须 ≥ $MIN_PER_FAMILY")
                }
                val actual = actualTally[nm] ?: 0
                if (c != actual) {
                    v += PsychologyViolation(
                        "6 族覆盖",
                        "families.json «$nm» count=$c 与 entries.json 实际条数 $actual 不一致（交叉核对）",
                    )
                }
            }
            val modes = (obj["modes"] as? JsonArray)?.mapNotNull { it.str() }
            if (modes.isNullOrEmpty()) {
                v += PsychologyViolation("6 族覆盖", "families.json «$nm» modes（服役档位）不得为空")
            } else {
                val bad = modes.filterNot { it in EXPECTED_MODES }
                if (bad.isNotEmpty()) v += PsychologyViolation("6 族覆盖", "families.json «$nm» modes 含非枚举值：$bad")
            }
        }
        e += "families.json：count 之和=$sum（期望 $ENTRY_COUNT）"
        if (sum != ENTRY_COUNT) {
            v += PsychologyViolation("6 族覆盖", "families.json count 之和=$sum，必须严格 = $ENTRY_COUNT")
        }
    }

    // -------------------------------------------------------- retrieval rules

    private fun checkRetrievalRules(root: JsonObject, v: MutableList<PsychologyViolation>, e: MutableList<String>) {
        val keys = root.keys.toList()
        val missing = RETRIEVAL_KEYS.filterNot { it in keys }
        val unknown = keys.filterNot { it in RETRIEVAL_KEYS }
        if (missing.isNotEmpty()) v += PsychologyViolation("JSON 字段", "retrieval-rules.json 缺 key：$missing")
        if (unknown.isNotEmpty()) v += PsychologyViolation("JSON 字段", "retrieval-rules.json 未知 key：$unknown")
        if (root["version"]?.str() != VERSION) {
            v += PsychologyViolation("版本", "retrieval-rules.json version=«${root["version"]?.str()}»，期望 «$VERSION»")
        }

        val fmt = root["index_line_format"]?.str()
        e += "retrieval-rules.json：index_line_format=«$fmt»"
        if (fmt != INDEX_LINE_FORMAT) {
            v += PsychologyViolation("索引格式", "index_line_format=«$fmt»，必须逐字等于 «$INDEX_LINE_FORMAT»")
        }

        val ic = root["injection_count"] as? JsonObject
        val mn = ic?.get("min")?.int()
        val mx = ic?.get("max")?.int()
        e += "retrieval-rules.json：injection_count=($mn,$mx)（期望 ($INJECTION_MIN,$INJECTION_MAX)）"
        if (mn != INJECTION_MIN || mx != INJECTION_MAX) {
            v += PsychologyViolation("注入条数", "injection_count=($mn,$mx)，必须为 ($INJECTION_MIN,$INJECTION_MAX)（D19 / C2.3）")
        }

        val ri = (root["retrieval_input"] as? JsonArray)?.mapNotNull { it.str() }
        if (ri != RETRIEVAL_INPUT_EXPECTED) {
            v += PsychologyViolation("JSON 字段", "retrieval_input=$ri，必须逐字等于 $RETRIEVAL_INPUT_EXPECTED")
        }

        val sel = (root["selection_rules"] as? JsonArray)?.mapNotNull { it.str() } ?: emptyList()
        e += "retrieval-rules.json：selection_rules=${sel.size} 条（须 3~8）"
        if (sel.size !in 3..8) v += PsychologyViolation("JSON 字段", "selection_rules=${sel.size} 条，必须 3~8 条")
        if (sel.any { it.isBlank() }) v += PsychologyViolation("JSON 字段", "selection_rules 含空规则")

        val rf = (root["reverse_filter_rules"] as? JsonArray)?.mapNotNull { it.str() } ?: emptyList()
        e += "retrieval-rules.json：reverse_filter_rules=${rf.size} 条"
        if (rf.isEmpty()) {
            v += PsychologyViolation("证据/反例", "reverse_filter_rules 不得为空（红线 R-C4 / 验收 C2.5）")
        }

        val prio = root["conflict_priority_by_mode"] as? JsonObject
        if (prio == null) {
            v += PsychologyViolation("裁决优先级", "缺少 conflict_priority_by_mode（红线 R-C5）")
        } else {
            e += "retrieval-rules.json：conflict_priority_by_mode 档数=${prio.keys.size}（期望 5）"
            if (prio.keys.toSet() != EXPECTED_MODES.toSet()) {
                v += PsychologyViolation("裁决优先级", "档位=${prio.keys}，必须逐字等于 $EXPECTED_MODES")
            }
            EXPECTED_MODES.forEach { mode ->
                val list = (prio[mode] as? JsonArray)?.mapNotNull { it.str() } ?: emptyList()
                e += "  优先级[$mode]=${list.joinToString(" > ")}"
                if (list.size != PSY_FAMILIES.size || list.toSet() != PSY_FAMILIES.toSet()) {
                    v += PsychologyViolation(
                        "裁决优先级",
                        "$mode 优先级=$list，每档必须含且仅含 6 族各一次（红线 R-C5，不允许留空或交模型判断）",
                    )
                }
            }
        }
    }

    // --------------------------------------------------------------- INDEX.md

    private fun checkIndex(
        file: File,
        entries: List<PsyEntry>,
        v: MutableList<PsychologyViolation>,
        e: MutableList<String>,
    ) {
        if (!file.isFile) {
            v += PsychologyViolation("索引行数", "INDEX.md 不存在")
            return
        }
        val raw = file.readText()
        if (raw.contains("\r\n") || raw.contains('\r')) {
            v += PsychologyViolation("行尾一致", "INDEX.md 含 CRLF，资产行尾统一为 LF")
        }
        val lines = raw.split("\n").map { it.trimEnd() }.filter { it.isNotEmpty() }
        e += "INDEX.md：有效数据行=${lines.size}（期望 $ENTRY_COUNT）"
        if (lines.size != ENTRY_COUNT) {
            v += PsychologyViolation("索引行数", "INDEX.md 有效数据行=${lines.size}，必须严格 = $ENTRY_COUNT（任务包 §3.5）")
        }

        if (entries.isNotEmpty()) {
            lines.forEachIndexed { i, line ->
                val segs = line.split(INDEX_SEPARATOR)
                if (segs.size != 4) {
                    v += PsychologyViolation(
                        "索引格式",
                        "INDEX.md 第 ${i + 1} 行不是 4 段（分隔符 «$INDEX_SEPARATOR»）：«$line»",
                    )
                    return@forEachIndexed
                }
                val expected = entries.getOrNull(i) ?: return@forEachIndexed
                val id = segs[0]
                val name = segs[1]
                val modes = segs[2]
                val principle = segs[3]
                if (id != expected.id) {
                    v += PsychologyViolation("索引格式", "INDEX.md 第 ${i + 1} 行 id=«$id»，期望 «${expected.id}»（须 id 升序）")
                }
                if (name != expected.name) {
                    v += PsychologyViolation("索引格式", "INDEX.md 第 ${i + 1} 行 name=«$name»，期望 «${expected.name}»")
                }
                if (modes != expected.modes.joinToString(",")) {
                    v += PsychologyViolation(
                        "索引格式",
                        "INDEX.md 第 ${i + 1} 行 适用档=«$modes»，期望 «${expected.modes.joinToString(",")}»",
                    )
                }
                if (principle != expected.principle) {
                    v += PsychologyViolation("索引格式", "INDEX.md 第 ${i + 1} 行 一句话原理与 entries.json 不一致")
                }
            }
        }

        // R-T2：INDEX.md 位于八段第 2 段（断点前），不得含时间戳/日期/UUID/会话 ID
        if (DATE_REGEX.containsMatchIn(raw)) {
            v += PsychologyViolation("文本合规", "INDEX.md 含日期字面量（违反 R-T2 断点前逐字节稳定）")
        }
        if (UUID_REGEX.containsMatchIn(raw)) {
            v += PsychologyViolation("文本合规", "INDEX.md 含 UUID（违反 R-T2）")
        }
    }

    // ----------------------------------------------------------- 文本合规

    private fun checkTextCompliance(dir: File, e: MutableList<String>, v: MutableList<PsychologyViolation>) {
        val hits = mutableListOf<String>()
        dir.walkTopDown().filter { it.isFile }.forEach { file ->
            val text = runCatching { file.readText() }.getOrDefault("")
            FORBIDDEN_TEXT.forEach { needle ->
                if (text.contains(needle)) hits += "${file.relativeTo(dir).path} 命中 «$needle»"
            }
        }
        e += "文本合规：assets/psychology 全量扫描 ${FORBIDDEN_TEXT.joinToString("/")} → 命中 ${hits.size} 处"
        hits.forEach { v += PsychologyViolation("文本合规", it) }

        // 证据性统计（非违规）：R-C1 要求出处可复核，故 evidence 内的年份字面量必然存在；
        // R-T2 的「断点前逐字节稳定」只约束 PSY_INDEX 所在的断点前内容（即 INDEX.md），已在上面单独校验。
        val entriesText = runCatching { File(dir, "entries.json").readText() }.getOrDefault("")
        val yearHits = EVIDENCE_YEAR_REGEX.findAll(entriesText).count()
        e += "证据性统计（非违规）：entries.json 证据年份字面量 $yearHits 处（R-C1 要求，不适用 R-T2 断点前约束）"
    }

    // ----------------------------------------------------------------- helpers

    private fun readObject(file: File, v: MutableList<PsychologyViolation>): JsonObject? {
        if (!file.isFile) {
            v += PsychologyViolation(file.name, "文件不存在：${file.path}")
            return null
        }
        return runCatching { json.parseToJsonElement(file.readText()).jsonObject }
            .getOrElse {
                v += PsychologyViolation(file.name, "不是合法 JSON 对象：${it.message}")
                null
            }
    }

    private fun JsonElement?.str(): String? =
        (this as? JsonPrimitive)?.takeIf { it !is JsonNull }?.contentOrNull

    private fun JsonElement?.int(): Int? = str()?.trim()?.toIntOrNull()
}

/** 心理学资产路径解析：优先用 Gradle 传入的 system property，其次按仓库相对路径兜底。 */
object PsychologyPaths {
    val psychologyDir: File
        get() = System.getProperty("psychology.dir")?.let(::File) ?: File("../../assets/psychology")
}
