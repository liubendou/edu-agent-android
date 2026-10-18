package com.eduagent.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Hilt 宿主 Application（契约 §2：:app 负责 Application 与 DI 装配）。 */
@HiltAndroidApp
class EduAgentApplication : Application()
