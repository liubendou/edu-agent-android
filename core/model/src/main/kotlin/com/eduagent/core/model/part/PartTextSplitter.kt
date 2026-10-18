package com.eduagent.core.model.part

/**
 * 正文拆分器（契约 01-契约.md §4 硬规则）。
 *
 * 硬规则：`explain_card` / `keypoint_card` / `compare_card` / `table_card` 的正文**不得超过 1200 字**，
 * 长内容必须拆成多张同型零件。本工具按句界贪心切分，保证：
 * 1. 每段 ≤ [limit]；
 * 2. `split(text).joinToString("") == text`（字符不丢失、不重排）。
 *
 * 纯 Kotlin，无 Android 依赖（`:core:model`）；第 9 步 validator 与第 6 步 `SplitTextTest` 共用。
 */
object PartTextSplitter {

    /** 契约 §4 冻结的正文上限（字）。 */
    const val TEXT_LIMIT: Int = 1200

    /** 优先在这些字符**之后**断句。 */
    private const val BREAK_CHARS = "。；！？.!?;"

    /**
     * 拆分 [text] 为若干段，每段长度 ≤ [limit]。
     *
     * @param text 原始正文；长度 ≤ [limit] 时原样返回单段。
     * @param limit 单段上限，默认契约冻结的 [TEXT_LIMIT]。
     * @throws IllegalArgumentException 当 [limit] ≤ 0。
     */
    fun split(text: String, limit: Int = TEXT_LIMIT): List<String> {
        require(limit > 0) { "limit 必须为正数，实际为 $limit" }
        if (text.isEmpty()) return listOf(text)
        if (text.length <= limit) return listOf(text)

        val chunks = mutableListOf<String>()
        val pending = StringBuilder()

        for (sentence in tokenize(text)) {
            if (pending.isNotEmpty() && pending.length + sentence.length > limit) {
                chunks += pending.toString()
                pending.setLength(0)
            }
            var rest = sentence
            while (rest.length > limit) {
                chunks += rest.substring(0, limit)
                rest = rest.substring(limit)
            }
            pending.append(rest)
        }
        if (pending.isNotEmpty()) chunks += pending.toString()
        return chunks
    }

    /** 按断句符把文本切成「带尾符」的句子序列；无断句符时整段作为一个 token。 */
    private fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()
        val buffer = StringBuilder()
        for (ch in text) {
            buffer.append(ch)
            if (ch == '\n' || BREAK_CHARS.indexOf(ch) >= 0) {
                tokens += buffer.toString()
                buffer.setLength(0)
            }
        }
        if (buffer.isNotEmpty()) tokens += buffer.toString()
        return tokens
    }
}
