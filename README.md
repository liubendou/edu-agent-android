# 教育教学 Agent · Android

基于 Kotlin + Jetpack Compose 的教育教学 Agent 客户端，采用多模块架构（core / feature / ui / app），内置微交互动效、流式输出与掌握度可视化。

## 技术栈

- Kotlin + Jetpack Compose（Material3 / adaptive）
- Vico（掌握度环、提分曲线、进度图）
- Shimmer（思考占位）、Lottie（选装）
- minSdk 29 / targetSdk 36 / compileSdk 37

## 构建

```bash
./gradlew :app:assembleDebug
```

`local.properties` 中的本机 SDK 路径不会入库。

## 说明

本仓库为公开快照，已排除构建产物与本地配置。许可证：MIT。
