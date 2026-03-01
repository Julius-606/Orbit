// ==========================================
// IDENTITY: The Timekeeper / Date Converter
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/DateConverter.kt
// COMPONENT: Android Offline Database
// ROLE: Converts standard Dates to Longs because SQLite databases can't hold Date objects natively.
// VIBE: Time is money, especially during the New York crossover. 🕒💰
// ==========================================

package com.example.pocket_orbit.data

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}