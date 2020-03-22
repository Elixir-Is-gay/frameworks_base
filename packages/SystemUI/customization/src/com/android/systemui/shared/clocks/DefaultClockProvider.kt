/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.systemui.shared.clocks

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import com.android.systemui.customization.R
import com.android.systemui.plugins.clocks.ClockController
import com.android.systemui.plugins.clocks.ClockId
import com.android.systemui.plugins.clocks.ClockMessageBuffers
import com.android.systemui.plugins.clocks.ClockMetadata
import com.android.systemui.plugins.clocks.ClockPickerConfig
import com.android.systemui.plugins.clocks.ClockProvider
import com.android.systemui.plugins.clocks.ClockSettings

private val TAG = DefaultClockProvider::class.simpleName
const val DEFAULT_CLOCK_ID = "DEFAULT"
const val CUSTOM_CLOCK_NAME = "Custom Clock"
const val CUSTOM_CLOCK_ID = "CUSTOM"

/** Provides the default system clock */
class DefaultClockProvider(
    val ctx: Context,
    val layoutInflater: LayoutInflater,
    val resources: Resources,
    val hasStepClockAnimation: Boolean = false,
    val migratedClocks: Boolean = false
) : ClockProvider {
    private var messageBuffers: ClockMessageBuffers? = null

    override fun initialize(buffers: ClockMessageBuffers?) {
        messageBuffers = buffers
    }

    override fun getClocks(): List<ClockMetadata> = listOf(ClockMetadata(DEFAULT_CLOCK_ID)) + listOf(ClockMetadata(CUSTOM_CLOCK_ID))

    override fun createClock(settings: ClockSettings): ClockController {
        if (settings.clockId == DEFAULT_CLOCK_ID) {
            return DefaultClockController(ctx, layoutInflater, resources, settings, hasStepClockAnimation)
        } else {
            return CustomClockController(ctx, layoutInflater, resources, settings)
        }

        return DefaultClockController(
            ctx,
            layoutInflater,
            resources,
            settings,
            hasStepClockAnimation,
            migratedClocks,
            messageBuffers,
        )
    }

    override fun getClockPickerConfig(id: ClockId): ClockPickerConfig {
        val isCustomClock = id != DEFAULT_CLOCK_ID

        return ClockPickerConfig(
            if (isCustomClock) CUSTOM_CLOCK_ID else DEFAULT_CLOCK_ID,
            if (isCustomClock) CUSTOM_CLOCK_NAME else resources.getString(R.string.clock_default_name),
            resources.getString(R.string.clock_default_description),
            // TODO(b/352049256): Update placeholder to actual resource
            resources.getDrawable(R.drawable.clock_default_thumbnail, null),
        )
    }
}
