package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY isEnabled DESC, hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmItem>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getActiveAlarms(): List<AlarmItem>

    @Query("SELECT * FROM alarms WHERE id = :alarmId LIMIT 1")
    suspend fun getAlarmById(alarmId: Long): AlarmItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmItem): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmItem)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmItem)

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteAlarmById(alarmId: Long)

    @Query("DELETE FROM alarms WHERE id IN (:ids)")
    suspend fun deleteAlarmsByIds(ids: List<Long>)

    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :alarmId")
    suspend fun updateAlarmStatus(alarmId: Long, isEnabled: Boolean)

    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id IN (:ids)")
    suspend fun updateAlarmsStatus(ids: List<Long>, isEnabled: Boolean)

    @Query("UPDATE alarms SET nextTriggerTimeMillis = :nextTime WHERE id = :alarmId")
    suspend fun updateNextTriggerTime(alarmId: Long, nextTime: Long)
}
