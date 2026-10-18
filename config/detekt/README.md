# 教育教学 Agent · detekt 基线说明
#
# baseline.xml 由 `./gradlew detektBaseline` 生成，用于冻结本工程**当前存量**的静态检查问题，
# 使 `./gradlew detekt` 在既有基线上保持可跑（任务包 02 §4.1：「允许有既定基线，
# 但不得新增阻塞级问题」）。
#
# 规则：
#   1) 新增代码不得向 baseline.xml 追加条目掩盖新问题；
#   2) 存量条目应随对应模块（第 5~10 步）落地逐步偿还；
#   3) baseline.xml 的 diff 属于需人工评审的变更。
