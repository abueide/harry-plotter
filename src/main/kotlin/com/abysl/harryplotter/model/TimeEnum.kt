/*
 *     Copyright (c) 2021 Andrew Bueide
 *
 *     This file is part of Harry Plotter.
 *
 *     Harry Plotter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Harry Plotter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Harry Plotter.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.abysl.harryplotter.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private const val WEEKS_IN_QUARTER = 12
@OptIn(ExperimentalTime::class)
enum class TimeEnum(val readableName: String, val titleName: String, val unit: Duration, val zoom: Duration) {
    // Graphs a single day at a time
    HOURLY("Hourly", "Hour", Duration.hours(1), Duration.days(1)) {
        override fun getLabel(time: Instant): String {
            return time.toLocalDateTime(TimeZone.currentSystemDefault()).hour.toString() + ":00"
        }
    },
    // Use weekly view for half days
    DAILY("Daily", "Day", Duration.days(1), Duration.days(7)) {
        override fun getLabel(time: Instant): String {
            return time.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )
        }
    },
    // Use a quarterly view
    WEEKLY("Weekly", "Week", Duration.days(7), Duration.days(90)) {
        override fun getLabel(time: Instant): String {
            val howLongAgo = Clock.System.now() - time
            val result = (howLongAgo / Duration.days(7)).toInt().toString()
            return result
        }
    },
    MONTHLY("Monthly", "Month", Duration.days(30), Duration.days(365)) {
        override fun getLabel(time: Instant): String {
            return time.toLocalDateTime(TimeZone.currentSystemDefault()).month.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )
        }
    };

    abstract fun getLabel(time: Instant): String

    override fun toString(): String{
        return readableName
    }

    companion object {
        const val SECONDS_IN_DAY: Int = 86400
        const val MILLIS_PER_MINUTE = 60000L
    }
}