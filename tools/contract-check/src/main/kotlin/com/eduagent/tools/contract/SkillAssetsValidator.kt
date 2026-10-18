package com.eduagent.tools.contract

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 五档技能模板资产校验器（第 3 步机检实现）。
 * 契约依据：04-任务包/03-五档技能模板资产.md §3 / §3.3 / §3.5 / §4.1；
 *           01-契约.md §4（十六零件）、§5（签名矩阵）、§8.5（五档 tau）、§11 R-T2/R-T9/R-T10、§13 D1/D32。
 *
 * 设计要点（评审修正 2026-10-18）：**期望值一律取本文件的硬编码常量**（对齐 ContractValidator 的常量区模式），
 * 不再从 assets/contract 下的 JSON 运行时拼装——否则「任务包 §3.3 表」与「矩阵 required_any_of」之间一旦矛盾，
 * 校验器会自引用循环、必然通过。矩阵只用于**反向交叉核对**（规则「矩阵映射」）。
 *
 * 只读 assets/skills 与 assets/contract，不做任何写操作。
 */
class SkillViolation(val rule: String, val detail: String) {
    override fun toString(): String = "[$rule] $detail"
}

class SkillAssetsReport(val evidence: List<String>, val violations: List<SkillViolation>) {
    val ok: Boolean get() = violations.isEmpty()

    fun render(): String = buildString {
        appendLine("---- 五档技能模板资产校验证据 ----")
        evidence.forEach { appendLine("  · $it") }
        if (ok) {
            appendLine("结论：PASS（0 项违规）")
        } else {
            appendLine("结论：FAIL（${violations.size} 项违规）")
            violations.forEach { appendLine("  × $it") }
        }
    }
}

object SkillAssetsValidator {

    /** 任务包 03 §3.1：目录名逐字冻结 */
    val EXPECTED_MODES: List<String> = ContractValidator.EXPECTED_MODES

    /** 契约 §5 中文档名（任务包 03 §3.2 name 字段约束） */
    val NAME_ZH: Map<String, String> = linkedMapOf(
        "quick" to "快速学习", "standard" to "标准", "deep" to "深度",
        "review" to "复习", "boost" to "提分",
    )

    /**
     * 6 族名（第 4 步据此产出 families.json 的「服役档位」）。
     * 评审修正：**以契约 §13 D8 为准**（「元认知」），不采用任务包 03 §7 的「元认知与自我调节」。
     */
    val PSY_FAMILIES: List<String> = listOf(
        "记忆与巩固", "理解与建构", "练习与迁移",
        "元认知", "动机与情绪", "习惯与节奏",
    )

    /**
     * 契约 §5 签名矩阵（**硬编码冻结**，与 04-任务包 03 §3.3 表逐字一致）。
     * boost 的「或」统一编码为单元素串 `A|B`，且**或项在前**（任务包 §3.3 表顺序）。
     */
    val EXPECTED_REQUIRED: Map<String, List<String>> = linkedMapOf(
        "quick" to listOf("keypoint_card", "explain_card"),
        "standard" to listOf("explain_card", "quiz_card", "verdict_card"),
        "deep" to listOf("explain_card", "quiz_card", "map_card"),
        "review" to listOf("review_card", "verdict_card"),
        "boost" to listOf("diagnose_card|plan_panel", "quiz_card"),
    )
    val EXPECTED_OPTIONAL: Map<String, List<String>> = linkedMapOf(
        "quick" to listOf("compare_card", "table_card"),
        "standard" to listOf("keypoint_card", "review_card"),
        "deep" to listOf("error_cause_card", "compare_card"),
        "review" to listOf("progress_card"),
        "boost" to listOf("progress_card", "error_cause_card", "material_card"),
    )

    /** boost 的「或」项（用于与矩阵 `required_any_of` 反向交叉核对） */
    val EXPECTED_ANY_OF: Map<String, List<String>> =
        mapOf("boost" to listOf("diagnose_card", "plan_panel"))

    /** 「或」串的分隔符（任务包 03 §3.3 注脚） */
    const val ANY_OF_SEPARATOR = "|"

    /** 任务包 03 §3.5 冻结的 rhythm */
    val EXPECTED_RHYTHM: Map<String, Triple<String, Boolean, Boolean>> = linkedMapOf(
        "quick" to Triple("low", false, false),
        "standard" to Triple("mid", false, true),
        "deep" to Triple("high", true, true),
        "review" to Triple("low", false, false),
        "boost" to Triple("mid", true, true),
    )

    /** 零件白名单：直接用 ContractValidator 的硬编码 16 项（避免再从 parts.json 读成循环） */
    val PART_TYPES: List<String> = ContractValidator.EXPECTED_PART_TYPES

    /** tau 基准：直接用 ContractValidator 的硬编码 §8.5 取值 */
    val EXPECTED_TAU: Map<String, Double> = ContractValidator.EXPECTED_TAU

    /** 任务包 03 §3.2：12 个字段，逐字、不得缺、不得多 */
    val REQUIRED_FIELDS: List<String> = listOf(
        "mode", "id", "name", "version", "instruction_skeleton", "output_contract",
        "required_parts", "optional_parts", "rhythm", "psychology_tags", "tau", "source_refs",
    )
    val OUTPUT_CONTRACT_KEYS: List<String> = listOf("required_parts", "optional_parts", "emit_order")
    val RHYTHM_KEYS: List<String> = listOf("prompt_density", "require_recap", "force_quiz")
    val DENSITY_VALUES: Set<String> = setOf("low", "mid", "high")

    /** 资产自版本，统一 1.0.0（禁止写入契约 §1 的库版本号） */
    const val SKILL_VERSION = "1.0.0"

    /** boost 短期窗口（契约 §13 D32） */
    const val SHORT_WINDOW_DAYS = 30

    /** 任务包 03 §4.1 文本合规：全文命中数必须为 0（Lynx/TS 属前版技术栈痕迹） */
    val FORBIDDEN_TEXT: List<String> = listOf("lynx", "Lynx", "TS", "TypeScript")

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun validate(skillsDir: File, contractDir: File): SkillAssetsReport {
        val v = mutableListOf<SkillViolation>()
        val e = mutableListOf<String>()

        val matrix = readObject(File(contractDir, "signature-matrix.json"), v)

        e += "期望值来源：SkillAssetsValidator 硬编码常量（契约 §5 签名矩阵 / §8.5 tau / §4 十六零件）"
        e += "零件白名单：${PART_TYPES.size} 个 type（ContractValidator.EXPECTED_PART_TYPES）"
        e += "tau 基准：${EXPECTED_TAU.entries.joinToString("/") { "${it.key}=${it.value}" }}"
        e += "boost 或项编码：EXPECTED_REQUIRED[boost]=${EXPECTED_REQUIRED["boost"]}（或项在前，分隔符 «$ANY_OF_SEPARATOR»）"

        if (!skillsDir.isDirectory) {
            v += SkillViolation("assets/skills", "目录不存在：${skillsDir.path}")
            return SkillAssetsReport(e, v)
        }

        // ---- 1. 目录齐全 ----------------------------------------------------
        val dirs = skillsDir.listFiles()?.filter { it.isDirectory }?.map { it.name }?.sorted() ?: emptyList()
        e += "assets/skills 子目录=${dirs.size}（期望 5）→ $dirs"
        if (dirs.size != EXPECTED_MODES.size) {
            v += SkillViolation("目录齐全", "子目录数=${dirs.size}，期望 ${EXPECTED_MODES.size}：$dirs")
        }
        val extra = dirs - EXPECTED_MODES.toSet()
        if (extra.isNotEmpty()) v += SkillViolation("目录齐全", "出现冻结清单外的目录：$extra")

        // ---- 2. 逐档校验 ----------------------------------------------------
        EXPECTED_MODES.forEach { mode ->
            val modeDir = File(skillsDir, mode)
            if (!modeDir.isDirectory) {
                v += SkillViolation("目录齐全", "$mode/ 不存在")
                return@forEach
            }
            val skillFile = File(modeDir, "skill.json")
            val promptFile = File(modeDir, "prompt.md")
            if (!promptFile.isFile) v += SkillViolation("目录齐全", "$mode/prompt.md 不存在")
            val refs = File(modeDir, "references").listFiles()
                ?.filter { it.isFile && it.name.endsWith(".md") } ?: emptyList()
            e += "$mode/：prompt.md=${promptFile.isFile}，references 下 md=${refs.size} 份"
            if (refs.isEmpty()) v += SkillViolation("目录齐全", "$mode/references/ 至少需要 1 份 *.md")

            val root = readObject(skillFile, v) ?: return@forEach
            checkSkill(
                fileName = "$mode/skill.json", root = root, mode = mode,
                promptFile = promptFile, matrix = matrix, v = v,
            )
        }

        // ---- 3. 文本合规（全文扫描）----------------------------------------
        val hits = mutableListOf<String>()
        skillsDir.walkTopDown().filter { it.isFile }.forEach { file ->
            val text = runCatching { file.readText() }.getOrDefault("")
            FORBIDDEN_TEXT.forEach { needle ->
                if (text.contains(needle)) hits += "${file.relativeTo(skillsDir).path} 命中 «$needle»"
            }
        }
        e += "文本合规：assets/skills 全量扫描 ${FORBIDDEN_TEXT.joinToString("/")} → 命中 ${hits.size} 处"
        hits.forEach { v += SkillViolation("文本合规", it) }

        // ---- 4. 行尾一致性（防未来哈希漂移误报）----------------------------
        val crlfFiles = mutableListOf<String>()
        skillsDir.walkTopDown().filter { it.isFile && it.name.endsWith(".md") }.forEach { file ->
            val raw = runCatching { file.readText() }.getOrDefault("")
            if (raw.contains("\r\n")) crlfFiles += file.relativeTo(skillsDir).path
        }
        e += "行尾一致性：assets/skills 下全部 md 中含 CRLF 的文件 ${crlfFiles.size} 个"
        crlfFiles.forEach { v += SkillViolation("行尾一致", "$it 含 CRLF，资产行尾统一为 LF（否则哈希漂移检测会误报）") }

        return SkillAssetsReport(e, v)
    }

    // ------------------------------------------------------------------ 单档

    private fun checkSkill(
        fileName: String,
        root: JsonObject,
        mode: String,
        promptFile: File,
        matrix: JsonObject?,
        v: MutableList<SkillViolation>,
    ) {
        // 字段齐备 / 无未知键
        val keys = root.keys.toList()
        val missing = REQUIRED_FIELDS.filterNot { it in keys }
        val unknown = keys.filterNot { it in REQUIRED_FIELDS }
        if (missing.isNotEmpty()) v += SkillViolation("JSON 字段", "$fileName 缺字段：$missing")
        if (unknown.isNotEmpty()) v += SkillViolation("JSON 字段", "$fileName 出现未知键：$unknown")

        // 基本标量
        if (root["mode"]?.str() != mode) {
            v += SkillViolation("JSON 字段", "$fileName mode=${root["mode"]?.str()}，必须等于目录名 $mode")
        }
        if (root["id"]?.str() != "skill.$mode") {
            v += SkillViolation("JSON 字段", "$fileName id=${root["id"]?.str()}，必须为 skill.$mode")
        }
        if (root["name"]?.str() != NAME_ZH[mode]) {
            v += SkillViolation("JSON 字段", "$fileName name=${root["name"]?.str()}，必须为 ${NAME_ZH[mode]}")
        }
        if (root["version"]?.str() != SKILL_VERSION) {
            v += SkillViolation("JSON 字段", "$fileName version=${root["version"]?.str()}，必须为 $SKILL_VERSION")
        }

        // 签名一致（硬编码期望值；R-T9：不得放宽）
        val required = (root["required_parts"] as? JsonArray)?.mapNotNull { it.str() }
        val optional = (root["optional_parts"] as? JsonArray)?.mapNotNull { it.str() }
        if (required == null) v += SkillViolation("签名一致", "$fileName required_parts 必须是字符串数组")
        if (optional == null) v += SkillViolation("签名一致", "$fileName optional_parts 必须是字符串数组")
        if (required != EXPECTED_REQUIRED[mode]) {
            v += SkillViolation(
                "签名一致",
                "$fileName required_parts=$required，必须**逐字**等于冻结期望 ${EXPECTED_REQUIRED[mode]}" +
                    "（任务包 §3.3 表顺序；红线 R-T9 不得放宽、不得改序）",
            )
        }
        if (optional != EXPECTED_OPTIONAL[mode]) {
            v += SkillViolation("签名一致", "$fileName optional_parts=$optional，必须逐字等于冻结期望 ${EXPECTED_OPTIONAL[mode]}")
        }
        // 零件白名单（`|` 分隔的二选一须逐个落在 16 个 type 内）
        val tokens = (required.orEmpty() + optional.orEmpty()).flatMap { it.split(ANY_OF_SEPARATOR) }
        val badTokens = tokens.filterNot { it in PART_TYPES }
        if (badTokens.isNotEmpty()) v += SkillViolation("零件白名单", "$fileName 出现 parts.json 之外的零件：$badTokens")
        val overlap = required.orEmpty().toSet() intersect optional.orEmpty().toSet()
        if (overlap.isNotEmpty()) v += SkillViolation("签名一致", "$fileName required/optional 交集非空：$overlap")

        // 矩阵映射（反向交叉核对：矩阵改了必须报红，防止契约漂移）
        checkMatrixMapping(fileName, mode, required, matrix, v)

        // tau 一致（契约 §8.5，硬编码）
        val tau = root["tau"]?.jsonPrimitive?.doubleOrNull
        val expectedTau = EXPECTED_TAU[mode]
        if (tau == null) v += SkillViolation("tau 一致", "$fileName tau 缺失或非数字")
        else if (expectedTau != null && kotlin.math.abs(tau - expectedTau) > 1e-9) {
            v += SkillViolation("tau 一致", "$fileName tau=$tau，必须等于契约 §8.5 的 $expectedTau")
        }

        // rhythm 三键齐 + 取值（任务包 §3.5 冻结）
        val rhythm = root["rhythm"] as? JsonObject
        if (rhythm == null) {
            v += SkillViolation("JSON 字段", "$fileName rhythm 必须是对象")
        } else {
            val rKeys = rhythm.keys.toList()
            val rMissing = RHYTHM_KEYS.filterNot { it in rKeys }
            val rUnknown = rKeys.filterNot { it in RHYTHM_KEYS }
            if (rMissing.isNotEmpty()) v += SkillViolation("JSON 字段", "$fileName rhythm 缺键：$rMissing")
            if (rUnknown.isNotEmpty()) v += SkillViolation("JSON 字段", "$fileName rhythm 未知键：$rUnknown")
            val density = rhythm["prompt_density"]?.str()
            if (density !in DENSITY_VALUES) {
                v += SkillViolation("JSON 字段", "$fileName rhythm.prompt_density=$density，必须 ∈ $DENSITY_VALUES")
            }
            listOf("require_recap", "force_quiz").forEach { k ->
                if (rhythm[k]?.jsonPrimitive?.booleanOrNull == null) {
                    v += SkillViolation("JSON 字段", "$fileName rhythm.$k 必须是布尔值")
                }
            }
            EXPECTED_RHYTHM[mode]?.let { (d, recap, quiz) ->
                if (density != d || rhythm["require_recap"]?.jsonPrimitive?.booleanOrNull != recap ||
                    rhythm["force_quiz"]?.jsonPrimitive?.booleanOrNull != quiz
                ) {
                    v += SkillViolation(
                        "节奏",
                        "$fileName rhythm 与任务包 §3.5 冻结值不符：期望 ($d, $recap, $quiz)，" +
                            "实际 ($density, ${rhythm["require_recap"]?.jsonPrimitive?.booleanOrNull}, " +
                            "${rhythm["force_quiz"]?.jsonPrimitive?.booleanOrNull})",
                    )
                }
            }
            if (rhythm["force_quiz"]?.jsonPrimitive?.booleanOrNull == true && "quiz_card" !in required.orEmpty()) {
                v += SkillViolation("节奏", "$fileName force_quiz=true 但 required_parts 不含 quiz_card")
            }
        }

        // output_contract（三键、与顶层同值、emit_order 逐字等于 required_parts）
        val oc = root["output_contract"] as? JsonObject
        if (oc == null) {
            v += SkillViolation("JSON 字段", "$fileName output_contract 必须是对象")
        } else {
            val oKeys = oc.keys.toList()
            val oMissing = OUTPUT_CONTRACT_KEYS.filterNot { it in oKeys }
            val oUnknown = oKeys.filterNot { it in OUTPUT_CONTRACT_KEYS }
            if (oMissing.isNotEmpty()) v += SkillViolation("JSON 字段", "$fileName output_contract 缺键：$oMissing")
            if (oUnknown.isNotEmpty()) v += SkillViolation("JSON 字段", "$fileName output_contract 未知键：$oUnknown")
            val ocReq = (oc["required_parts"] as? JsonArray)?.mapNotNull { it.str() }
            val ocOpt = (oc["optional_parts"] as? JsonArray)?.mapNotNull { it.str() }
            if (ocReq != required) v += SkillViolation("JSON 字段", "$fileName output_contract.required_parts 与顶层不一致：$ocReq vs $required")
            if (ocOpt != optional) v += SkillViolation("JSON 字段", "$fileName output_contract.optional_parts 与顶层不一致：$ocOpt vs $optional")
            val emit = (oc["emit_order"] as? JsonArray)?.mapNotNull { it.str() }
            if (emit == null) {
                v += SkillViolation("JSON 字段", "$fileName output_contract.emit_order 必须是字符串数组")
            } else {
                // 「第 9 步按什么顺序读」必须唯一确定：emit_order 逐字等于 required_parts
                if (emit != required) {
                    v += SkillViolation("签发顺序", "$fileName emit_order=$emit，必须逐字等于 required_parts=$required（顺序不得歧义）")
                }
                val badEmit = emit.flatMap { it.split(ANY_OF_SEPARATOR) }.filterNot { it in PART_TYPES }
                if (badEmit.isNotEmpty()) v += SkillViolation("零件白名单", "$fileName emit_order 出现 parts.json 之外的零件：$badEmit")
            }
        }

        // psychology_tags ∈ 6 族名（契约 §13 D8 口径），至少 1 项
        val tags = (root["psychology_tags"] as? JsonArray)?.mapNotNull { it.str() }
        if (tags.isNullOrEmpty()) {
            v += SkillViolation("JSON 字段", "$fileName psychology_tags 至少 1 项")
        } else {
            val badTags = tags.filterNot { it in PSY_FAMILIES }
            if (badTags.isNotEmpty()) {
                v += SkillViolation("JSON 字段", "$fileName psychology_tags 越界：$badTags（须 ∈ 6 族名：$PSY_FAMILIES）")
            }
        }

        // source_refs 非空数组
        val refs = (root["source_refs"] as? JsonArray)?.mapNotNull { it.str() }
        if (refs.isNullOrEmpty()) v += SkillViolation("JSON 字段", "$fileName source_refs 不得为空")

        // instruction_skeleton 与 prompt.md 同源：归一化行尾后**逐字节相等**（R-T2：不得含时间戳/会话 ID）
        val skeleton = root["instruction_skeleton"]?.str()
        if (skeleton.isNullOrBlank()) {
            v += SkillViolation("文本合规", "$fileName instruction_skeleton 不得为空")
        } else {
            if (Regex("\\d{4}-\\d{2}-\\d{2}|\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-").containsMatchIn(skeleton)) {
                v += SkillViolation("文本合规", "$fileName instruction_skeleton 含日期/UUID（违反 R-T2）")
            }
            if (promptFile.isFile) {
                val a = normalizeText(skeleton)
                val b = normalizeText(promptFile.readText())
                if (a != b) {
                    v += SkillViolation(
                        "文本合规",
                        "$fileName instruction_skeleton 与 prompt.md 归一化行尾后**非逐字节同源**" +
                            "（长度 ${a.length} vs ${b.length}；首个差异位置=${firstDiff(a, b)}）",
                    )
                }
            }
        }

        // boost 长短期分叉（契约 §13 D1/D32，任务包 03 §3.6）
        if (mode == "boost") {
            val text = skeleton.orEmpty()
            listOf("LONG", "SHORT", "只问一次", "自动判定", "$SHORT_WINDOW_DAYS").forEach { needle ->
                if (!text.contains(needle)) {
                    v += SkillViolation("boost 分叉", "$fileName instruction_skeleton 缺关键词 «$needle»")
                }
            }
        }
    }

    // ------------------------------------------------- 矩阵映射（反向交叉核对）

    private fun checkMatrixMapping(
        fileName: String,
        mode: String,
        required: List<String>?,
        matrix: JsonObject?,
        v: MutableList<SkillViolation>,
    ) {
        if (matrix == null || required == null) return
        val entry = (matrix["modes"] as? JsonArray)
            ?.map { it.jsonObject }
            ?.firstOrNull { it["mode"]?.str() == mode } ?: run {
            v += SkillViolation("矩阵映射", "signature-matrix.json 缺少档 $mode")
            return
        }
        val mRequired = (entry["required"] as? JsonArray)?.mapNotNull { it.str() } ?: emptyList()
        val mAnyOf = (entry["required_any_of"] as? JsonArray)?.mapNotNull { it.str() } ?: emptyList()
        val expectedAnyOf = EXPECTED_ANY_OF[mode] ?: emptyList()

        if (mAnyOf != expectedAnyOf) {
            v += SkillViolation(
                "矩阵映射",
                "signature-matrix.json[$mode].required_any_of=$mAnyOf，与冻结期望 $expectedAnyOf 不一致" +
                    "（若为有意修订，须同步本校验器常量与 skill.json）",
            )
        }
        val anyOfToken = mAnyOf.joinToString(ANY_OF_SEPARATOR)
        // 「逐字冻结」治理：矩阵 → skill.json 的映射规则必须收敛且唯一
        val expectedSet = (mRequired + if (anyOfToken.isEmpty()) emptyList() else listOf(anyOfToken)).toSet()
        if (required.toSet() != expectedSet) {
            v += SkillViolation(
                "矩阵映射",
                "signature-matrix.json[$mode] 映射到 $expectedSet，但 skill.json required_parts=${required.toSet()}",
            )
        }
        // 「或」项必须整体作为一个 token 出现，且位置逐字等于冻结期望（boost = 或项在前）
        val actualAnyOfPos = required.indexOfFirst { it.contains(ANY_OF_SEPARATOR) }
        val expectedAnyOfPos = EXPECTED_REQUIRED[mode]?.indexOfFirst { it.contains(ANY_OF_SEPARATOR) } ?: -1
        if (expectedAnyOfPos >= 0 && actualAnyOfPos != expectedAnyOfPos) {
            v += SkillViolation(
                "矩阵映射",
                "$fileName 「或」串位置=$actualAnyOfPos，冻结期望=$expectedAnyOfPos（任务包 §3.3 表：或项在前）",
            )
        }
    }

    // ----------------------------------------------------------------- helpers

    /** 行尾归一化：CRLF/CR → LF，并去掉结尾多余换行（用于「同源」逐字节比对） */
    internal fun normalizeText(s: String): String =
        s.replace("\r\n", "\n").replace('\r', '\n').trimEnd('\n')

    private fun firstDiff(a: String, b: String): Int {
        val n = minOf(a.length, b.length)
        for (i in 0 until n) if (a[i] != b[i]) return i
        return n
    }

    private fun readObject(file: File, v: MutableList<SkillViolation>): JsonObject? {
        if (!file.isFile) {
            v += SkillViolation(file.name, "文件不存在：${file.path}")
            return null
        }
        return runCatching { json.parseToJsonElement(file.readText()).jsonObject }
            .getOrElse {
                v += SkillViolation(file.name, "不是合法 JSON 对象：${it.message}")
                null
            }
    }

    private fun JsonElement?.str(): String? =
        (this as? JsonPrimitive)?.takeIf { it !is JsonNull }?.contentOrNull
}

/** 技能资产路径解析：优先用 Gradle 传入的 system property，其次按仓库相对路径兜底。 */
object SkillPaths {
    val skillsDir: File
        get() = System.getProperty("skills.dir")?.let(::File) ?: File("../../assets/skills")

    val contractDir: File
        get() = ContractPaths.contractDir
}
