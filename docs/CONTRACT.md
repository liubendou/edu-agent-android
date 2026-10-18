# CONTRACT · 人读版契约（第 1 步产出）

> 机器可读的唯一事实源是 `assets/contract/*.json`（7 份）；本文件只是给人看的一层薄视图。
> **两者不一致时以 JSON 为准**，并且 `.\gradlew contractCheck` 会报红。
> 派生字段的生成规则写在每份 JSON 的 `_source` / `_note` 里（契约 §3 只定义了顶层字段）。

---

## 一、资产台账（7 份，全部受版本控制 —— 红线 D1）

| 文件 | 内容 | 来源 | 条数 |
|---|---|---|---|
| `assets/contract/parts.json` | 16 个零件目录 + 硬规则 | 契约 §4 | 16 |
| `assets/contract/signature-matrix.json` | 五档签名矩阵（required / optional / required_any_of / tau） | 契约 §5 + §8.5 | 5 档 |
| `assets/contract/context-segments.json` | 八段顺序 + 断点位置 + 预算 | 契约 §6 | 8 段 |
| `assets/contract/redlines.json` | 49 条红线（ID / 分组 / 条文 / 拦截手段 / 归属步） | 契约 §11 | 49 |
| `assets/contract/acceptance.json` | 68 项验收索引（ID / 组 / 标题 / 归属步 / 级别 / 方法 / 判据） | 契约 §12 + 验收文档 §2~§7 + 动效文档 §9 | 68 |
| `assets/contract/decisions.json` | 33 项冻结决策（原 `[待确认]` 的全部取值） | 契约 §13 | 33 |
| `assets/contract/motion-tokens.json` | 三档动效 + 时长 + spring 令牌 + 10 条微交互配方 + tau | 契约 §8 | 10 配方 |

**本步未改任何 schema、未新增字段**：契约 §3 定义的顶层字段逐字保留（含 `_source` / `_note` 两个注释字段）。

---

## 二、八段上下文装配（顺序不可变 —— 契约 §6）

```
① 系统约束      SYSTEM     版本级     ┐
② 心理学库索引   PSY_INDEX  版本级     ├─ 断点前（前缀缓存）≤ 6k token，逐字节稳定
③ A2UI 目录     CATALOG    版本级     ┘     ↑ 断点在这
-------------------------- 缓存断点 --------------------------
④ 技能模板      SKILL      选档时变   ┐
⑤ 用户画像      PROFILE    会话间变   │
⑥ 任务指令      TASK       每回合变   ├─ 断点后 ≤ 6k token
⑦ 历史          HISTORY    每回合追加 │
⑧ 按需检索      RETRIEVAL  命中才注入 ┘
                    （输出 ≤ 2k token）
```

**前三段字节稳定的三条硬规则**（红线 R-T2，第 9 步必须 50 次哈希比对）：

1. 禁止任何随会话变化的内容：时间戳、日期、UUID、会话 ID、用户名、请求 ID、时区
2. 固定顺序 + 固定 JSON key 顺序（序列化器不得在会话内重排）
3. 使用预渲染的 raw 字符串

**冻结预算**：前缀 ≤ 6k / 断点后 ≤ 6k / 输出 ≤ 2k / 画像段 ≤ 800 token（超限按「影响当前回合决策程度」裁剪）；
心理学全文每回合注入 3~8 条。

---

## 三、16 个零件（目录 v1，`catalog_version = 1`）

| # | type | 中文 | 用途 | payload（必需） | 退化为静态 |
|---|---|---|---|---|---|
| 1 | `explain_card` | 讲解卡 | 概念讲解、例题拆解 | title, blocks | 同静态排版，无逐字 |
| 2 | `keypoint_card` | 要点卡 | 结构化要点/速览 | title, points | 同静态 |
| 3 | `compare_card` | 对比卡 | 易混概念对照 | title, columns, rows | 同静态 |
| 4 | `quiz_card` | 题卡 | 选择/填空/主观 | stem, kind, options, answer, explain | 同静态 |
| 5 | `verdict_card` | 判题卡 | 判对错 + 解析 | result, user_answer, answer, explain | 图标+文案终态，无描边动画 |
| 6 | `error_cause_card` | 错因卡 | 错因归因与纠正 | cause, evidence, fix | 同静态 |
| 7 | `map_card` | 地图卡 | 知识地图/结构图 | title, nodes, edges | 静态图 |
| 8 | `timeline_card` | 时间线卡 | 历史线/过程线 | title, events | 同静态 |
| 9 | `table_card` | 表卡 | 公式/数据/词汇表 | title, headers, rows | 同静态 |
| 10 | `plan_panel` | 计划面板 | 长短期学习计划 | horizon, goals, tasks | 同静态 |
| 11 | `progress_card` | 进度卡 | 掌握度/计划进度/提分曲线 | title, series, metric | 数值直显，无增长动画 |
| 12 | `review_card` | 复习卡 | 队列驱动的复习项 | item_id, prompt, due_at, reps | 同静态 |
| 13 | `diagnose_card` | 诊断卡 | 摸底与薄弱项诊断 | title, weak | 同静态 |
| 14 | `profile_confirm_card` | 画像确认卡 | 「我记住了这些」编辑确认 | entries | 同静态，必须保留可改可删 |
| 15 | `material_card` | 资料卡 | OCR/识图结果、资料摘要 | source_kind, summary, fields, uncertain | 同静态 |
| 16 | `tool_card` | 工具卡 | 联网搜索**结果与来源** | query, results | 同静态 |

**硬规则**：
- `explain_card` / `keypoint_card` / `compare_card` / `table_card` 正文 **≤ 1200 字**，长内容拆成多张同型零件
- `tool_card` 每条 results 必须有 `source`，无来源不得渲染（R-C1 / R-C2）
- `quiz_card` 与 `verdict_card` 必须**成对**出现在同一档同一回合；`answer` 由端侧保管，判分前不下发 UI
- 零件内部**不得**携带任何动效/时长/曲线字段（红线 R-T10）
- 模型只输出目录内的零件指令，**绝不输出可执行 UI 代码**（R-T1）

---

## 四、五档签名矩阵（§5 + §8.5 tau）

| 档 | required（必需） | optional（可选） | required_any_of | tau |
|---|---|---|---|---|
| `quick` 快速学习 | keypoint_card, explain_card | compare_card, table_card | — | 0.18 |
| `standard` 标准 | explain_card, quiz_card, verdict_card | keypoint_card, review_card | — | 0.25 |
| `deep` 深度 | explain_card, quiz_card, map_card | error_cause_card, compare_card | — | 0.35 |
| `review` 复习 | review_card, verdict_card | progress_card | — | 0.15 |
| `boost` 提分 | quiz_card | progress_card, error_cause_card, material_card | diagnose_card **或** plan_panel | 0.30 |

**判定规则**：命中 = 回合内任一零件 `type` 相同（重复只算一次）；出现矩阵外零件**不判废**，埋点记 `unknown_part`；
自由对话兜底回合同样走矩阵，不得放行无签名回合（除非明确进入 PLAINTEXT 兜底态）。
**红线 R-T9**：不允许放宽签名矩阵（required → optional）。

---

## 五、49 条红线（按分组，ID 连续无空缺）

| 分组 | 前缀 | 条数 | 主题 | 其中 CI 可机检 |
|---|---|---|---|---|
| 产品体验 | `R-P` | 7 | 首屏/无引导/无价值判断/禁成瘾/未成年保护/动效不阻塞 | —— |
| 技术契约 | `R-T` | 10 | 只输出零件指令/前缀字节稳定/三处同步/校验不可绕/三级兜底/升级一次/默认 Flash/版本号一致/不放宽矩阵/不输出动效参数 | R-T2、R-T3、R-T10 |
| 内容与事实 | `R-C` | 7 | 不编造来源/结果带来源/心理学条目有证据有反例/冲突裁决固定/不硬教/不提分承诺 | R-C3、R-C4 |
| 伦理与心理 | `R-E` | 6 | 心理学只用于学会/不制造焦虑/不做人格归因/画像不推断心理疾病/画像需确认/敏感条目不外显 | —— |
| 数据与隐私 | `R-D` | 4 | 敏感数据不上云/彻底删除/不训练不共享/日志无明文 | —— |
| 工程纪律 | `D` | 9 | 四类资产受版本控制/提交说明标注/资产与验收同步/指标上报/阻塞不挂账/禁新增引导/失败注入/临时方案带到期/十步线性 | —— |
| 动效 | `M` | 6 | 首屏不阻塞/交互延迟 ≤200ms/同屏动画 ≤4/低端机降档/帧率优先保流式/不依赖外部资源 | M5 |
| **合计** | | **49** | | **6 条** |

**归属步分布**（每步开工时先读本节）：1 步 6 条 · 3 步 2 条 · 4 步 5 条 · 5 步 5 条 · 6 步 2 条 · 7 步 1 条 · 8 步 7 条 · 9 步 14 条 · 10 步 7 条 = 49（无未归属项）。

---

## 六、68 项验收索引（契约 §12 分组 → 归属步）

| 组 | 前缀 | 项数 | 主题 | 归属步 |
|---|---|---|---|---|
| C1 | C1.1~8 | 8 | 五档与独立入口 | 3 / 8 / 10 |
| C2 | C2.1~5 | 5 | 心理学 79 条 | 4 / 9 |
| C3 | C3.1~6 | 6 | 画像识别与确认 | 8 / 9 |
| S1 | S1.1~10 | 10 | 云侧八段与兜底 | 9 |
| K1 | K1.1~8 | 8 | 端侧零件与离线 | 6 / 8 / 9 |
| X1 | X1.1~7 | 7 | 首屏零学习成本 | 10 |
| T1 | T1.1~5 | 5 | 内置工具 | 9 |
| P1 | P1.1~5 | 5 | 性能 | 8 / 9 / 10 |
| P2 | P2.1~2 | 2 | 稳定性 | 8 / 10 |
| P3 | P3.1~4 | 4 | 隐私与合规 | 8 / 9 / 10 |
| V | V1~V8 | 8 | 动效验收（动效文档 §9） | 5 / 6 / 7 / 10 |
| **合计** | | **68** | | |

**级别**：P0 = 必须全过 · P1 = 未通过 ≤ 3 项且不影响主链路 · P2 = 可挂账但须记录。
**阻塞级**（不接受挂账）：`S1.8`（三级兜底不出现空白回合）、`V3`（流式收尾可靠）、`V4`（停止生成即时反馈）。

---

## 七、动效契约（§8，三类库落地）

**时长令牌**：`micro 120ms` · `base 200ms` · `slow 600ms` · `draw 800ms`

**spring 令牌**（1:1 直译，禁止 cubic-bezier 近似）：

| 令牌 | stiffness | dampingRatio | 用途 |
|---|---|---|---|
| `snappy` | 380 | 30 | 按钮、chips、pill |
| `default` | 260 | 30 | 页面进出、卡片 |
| `soft` | 260 | 26 | toast、面板 |

**三档开关**：

| 档 | 行为 | 触发 |
|---|---|---|
| `full` | 全部动效 | 默认 |
| `reduced` | 去位移与循环，仅保留 ≤120ms 的透明度/颜色变化 | 用户设置，或低端机自动降档（内存/帧率判定） |
| `minimal` | 全部 duration = 0，退化为静态/瞬时（**不是隐藏**） | 系统「移除动画」→ 强制 minimal（读 `LocalMotionDurationScale.current == 0f`），不给用户反向覆盖入口 |

**10 条微交互配方**（每条都有 `minimal_fallback`，退档后信息不丢失 —— 验收 V2）：
按压回弹 · 模式指示滑动（MotionLayout 空间连续）· 选项 chips 弹跳 · 提交态三点加载 · 答错摇头 ·
答对描边 · 等待思考（shimmer + 光标呼吸，>8s 明示）· 顶部轻提示 · 待复习呼吸点 · 掌握度增长。

**流式纪律**：粗平滑（16ms 帧窗口聚合，首帧立即下发，结束前必须 flush）+ tau 细平滑（`rate = clamp(backlog/tau, MIN, MAX)`）；
RAF 五条收敛规则见 `motion-tokens.json` 的 `streaming.raf_rules`（红线 M5：帧率 < 45fps 优先砍动效，不砍流式）。

**性味**：B 冷白极简偏暖（纯白底、无衬线、大留白、弱边框、几乎无阴影）；五档**不得**变成五种配色。

---

## 八、三类库 6 个坐标（`gradle/libs.versions.toml`，已联网核实）

| 类别 | 坐标 | 版本 | 落地模块 |
|---|---|---|---|
| UI | `androidx.compose.material3.adaptive:adaptive` | 1.3.0 | `:core:design` |
| UI | `com.patrykandpatrick.vico:compose-m3` | 3.3.1 | `:core:design` / `:ui:parts` |
| 微动作 | `com.valentinilk.shimmer:compose-shimmer` | 1.5.0 | `:core:design` |
| 微动作 | Compose 触觉 API（`LocalHapticFeedback` + `LocalView.performHapticFeedback`） | 框架内置 | `:core:design` |
| 动画 | `androidx.compose.animation:animation` / `-core` | 由 BOM 管（不写版本） | `:core:design` |
| 动画 | `androidx.constraintlayout:constraintlayout-compose`（MotionLayout） | 1.1.2 | `:core:design` |
| 动画 | `com.airbnb.android:lottie-compose`（选装） | 6.6.6 | `:ui:parts` |

**规则**：任何新增依赖必须登记进 `gradle/libs.versions.toml` 并在交接包 `00-总纲.md` §二 表格补一行，否则视为违规。

---

## 九、改动纪律

| 动作 | 必须同时改 |
|---|---|
| 新增零件 type | `parts.json` + `signature-matrix.json`（若进矩阵）+ 云侧第 3 段（第 9 步）+ `ContractValidator.EXPECTED_PART_TYPES` |
| 新增红线 | `redlines.json`（分组编号必须连续）+ `REDLINE_GROUPS` 常量 |
| 新增验收项 | `acceptance.json` + `REQUIRED_ACCEPTANCE_IDS`（若要设为必须项） |
| 断点前内容任何改动 | 提交说明必须写明「是否影响断点前内容」（红线 D2） |
| 资产变更 | 同步更新本文件与 `00_施工总控.md` §五 对应条目（红线 D3） |
