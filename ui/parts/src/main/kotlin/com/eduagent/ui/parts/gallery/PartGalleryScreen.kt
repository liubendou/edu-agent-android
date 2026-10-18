package com.eduagent.ui.parts.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduagent.core.design.atom.ModeChipRow
import com.eduagent.core.design.atom.PressScaleButton
import com.eduagent.core.design.haptics.GatedHaptics
import com.eduagent.core.design.haptics.LocalHaptics
import com.eduagent.core.design.haptics.rememberAndroidHaptics
import com.eduagent.core.design.motion.DeviceTier
import com.eduagent.core.design.motion.LocalMotionLevel
import com.eduagent.core.design.motion.LocalMotionState
import com.eduagent.core.design.motion.MotionLevel
import com.eduagent.core.design.motion.rememberSystemAnimationScaleIsZero
import com.eduagent.core.design.theme.ColorTokens
import com.eduagent.core.design.theme.EduTheme
import com.eduagent.core.design.theme.ShapeTokens
import com.eduagent.core.model.PartTypes
import com.eduagent.core.model.part.PartEnvelope
import com.eduagent.core.model.part.ProfileConfirmCardPayload
import com.eduagent.core.model.part.ProfileEntry
import com.eduagent.ui.parts.LocalPartAnalytics
import com.eduagent.ui.parts.PartAnalytics
import com.eduagent.ui.parts.PartHost
import com.eduagent.ui.parts.R
import com.eduagent.ui.parts.entrance.partEntrance
import com.eduagent.ui.parts.part.ProfileConfirmCardPart
import com.eduagent.ui.parts.text.StreamingText

/**
 * 16 零件陈列页（任务包 06 §3；契约 §12 K1.1）。
 *
 * 逐字接口：`@Composable fun PartGalleryScreen(onBack: () -> Unit)`，**仅 debug 变体由 `:app` 挂载**
 * （第 10 步接入正式路由；本步临时入口的授权见 `99-记录\99-事件与例外.md`）。
 *
 * 页面职责（对应 §4.2 人工检查点 1~6）：
 * 1. 16 个零件依次陈列、可滚动（第 1 / 2 条）；
 * 2. 每个零件入场走 [partEntrance]（第 3 条：不重排、只向上补位）；
 * 3. 顶部可切 full / reduced / minimal 三档（第 4 条）；
 * 4. 画像确认卡为**有状态**演示：条目可改可删（第 6 条）；
 * 5. 附流式文本演示（[StreamingText] 匀速吐字，吐完才 enable）。
 */
@Composable
fun PartGalleryScreen(onBack: () -> Unit) {
    val systemScaleIsZero = rememberSystemAnimationScaleIsZero()
    var override by remember { mutableStateOf<MotionLevel?>(null) }
    val state = MotionLevel.resolve(
        systemScaleIsZero = systemScaleIsZero,
        userOverride = override,
        deviceTier = DeviceTier.STANDARD,
    )
    val deviceHaptics = rememberAndroidHaptics(enabled = true)
    val haptics = remember(deviceHaptics) { GatedHaptics(deviceHaptics, true) }

    EduTheme {
        CompositionLocalProvider(
            LocalMotionLevel provides state.level,
            LocalMotionState provides state,
            LocalHaptics provides haptics,
            // 陈列页不上报埋点：用默认空实现（真实链路由第 9/10 步装配）
            LocalPartAnalytics provides PartAnalytics.NoOp,
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = ColorTokens.Canvas) {
                PartGalleryContent(
                    canUserOverride = state.canUserOverride,
                    selectedLevel = override ?: state.level,
                    onSelectLevel = { index ->
                        override = MotionLevel.entries.getOrNull(index)
                        haptics.perform(com.eduagent.core.design.haptics.HapticKind.CONTEXT_CLICK)
                    },
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun PartGalleryContent(
    canUserOverride: Boolean,
    selectedLevel: MotionLevel,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit,
) {
    var restartKey by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PressScaleButton(onClick = onBack) {
                Text(
                    text = stringResource(R.string.part_gallery_action_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = ColorTokens.OnAccent,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.part_gallery_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = ColorTokens.Ink,
                )
                Text(
                    text = stringResource(R.string.part_gallery_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorTokens.InkMuted,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            PressScaleButton(onClick = { restartKey += 1 }) {
                Text(
                    text = stringResource(R.string.part_gallery_action_restart),
                    style = MaterialTheme.typography.labelLarge,
                    color = ColorTokens.OnAccent,
                )
            }
        }

        Text(
            text = stringResource(R.string.part_gallery_section_level),
            style = MaterialTheme.typography.labelLarge,
            color = ColorTokens.InkMuted,
        )
        if (canUserOverride) {
            ModeChipRow(
                chips = listOf(
                    stringResource(R.string.part_gallery_level_full),
                    stringResource(R.string.part_gallery_level_reduced),
                    stringResource(R.string.part_gallery_level_minimal),
                ),
                selectedIndex = selectedLevel.ordinal,
                onSelect = onSelectLevel,
            )
        } else {
            Text(
                text = stringResource(R.string.part_gallery_locked),
                style = MaterialTheme.typography.labelSmall,
                color = ColorTokens.Warning,
            )
        }

        Text(
            text = stringResource(R.string.part_gallery_section_parts),
            style = MaterialTheme.typography.labelLarge,
            color = ColorTokens.InkMuted,
        )

        // 重播入场：整段重挂载，使 partEntrance 可被反复观察（§4.2 第 3 条）
        key(restartKey) {
            PartSamples.all.forEach { envelope ->
                key(envelope.id) {
                    GalleryItem(envelope)
                }
            }
        }

        Text(
            text = stringResource(R.string.part_gallery_section_streaming),
            style = MaterialTheme.typography.labelLarge,
            color = ColorTokens.InkMuted,
        )
        StreamingDemo()
    }
}

/**
 * 陈列单个零件：入场统一走 [partEntrance]（§6 禁止事项 2：零件不自带入场参数）。
 *
 * 画像确认卡单独走有状态演示（§4.2 第 6 条：条目必须可改可删）。
 */
@Composable
private fun GalleryItem(envelope: PartEnvelope) {
    if (envelope.type == PartTypes.PROFILE_CONFIRM_CARD) {
        ProfileConfirmDemo(envelope, modifier = Modifier.partEntrance())
    } else {
        PartHost(envelope = envelope, modifier = Modifier.partEntrance())
    }
}

/** 画像确认卡：有状态演示 —— 删除即从列表移除，修改即写回条目（可改可删）。 */
@Composable
private fun ProfileConfirmDemo(envelope: PartEnvelope, modifier: Modifier = Modifier) {
    val initial = (envelope.payload as? ProfileConfirmCardPayload)?.entries.orEmpty()
    val entries = remember { mutableStateListOf<ProfileEntry>().apply { addAll(initial) } }
    ProfileConfirmCardPart(
        payload = ProfileConfirmCardPayload(entries = entries.toList()),
        modifier = modifier,
        onEditValue = { index, newValue ->
            entries.getOrNull(index)?.let { entry -> entries[index] = entry.copy(value = newValue) }
        },
        onDelete = { index -> entries.removeAt(index) },
    )
}

/** 流式文本演示：匀速吐字，吐完才 enable（§8.5-③）。 */
@Composable
private fun StreamingDemo() {
    var runKey by remember { mutableIntStateOf(0) }
    var completed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Card)
            .background(ColorTokens.Surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        key(runKey) {
            StreamingText(
                text = STREAMING_DEMO_TEXT,
                isStreaming = true,
                onCompleted = { completed = true },
                modifier = Modifier.partEntrance(),
            )
        }
        Text(
            text = stringResource(
                if (completed) {
                    R.string.part_gallery_streaming_done
                } else {
                    R.string.part_gallery_streaming_running
                },
            ),
            style = MaterialTheme.typography.labelSmall,
            color = if (completed) ColorTokens.Success else ColorTokens.InkMuted,
        )
        PressScaleButton(onClick = {
            completed = false
            runKey += 1
        }) {
            Text(
                text = stringResource(R.string.part_gallery_action_replay),
                style = MaterialTheme.typography.labelLarge,
                color = ColorTokens.OnAccent,
            )
        }
    }
}

/** 演示用正文（ASCII：仓库纪律要求 Kotlin 源码零硬编码中文）。 */
private const val STREAMING_DEMO_TEXT: String =
    "Streaming text arrives character by character at a steady pace, " +
        "so the learner starts reading immediately and interaction unlocks only after it finishes."

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PartGalleryScreenPreview() {
    PartGalleryScreen(onBack = { })
}
