// ============================================================================
// 教育教学 Agent · 根构建脚本（第 1 步）
//
// 本步只注册契约校验入口 contractCheck；AGP / Compose / KSP / Hilt 等插件
// 在第 2 步由各业务模块按需应用（契约 §2 的 8 个模块尚未声明）。
// ============================================================================

tasks.register("contractCheck") {
    group = "verification"
    description = "契约资产与冻结版本目录一致性校验（第 1 步机检入口）"
    dependsOn(":tools:contract-check:test")
}
