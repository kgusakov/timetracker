package com.timetracker.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

import com.timetracker.entities.Action

import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

import java.util.Date
import java.util.HashSet
import java.util.LinkedList

import com.timetracker.db.ActionsContract.ActionEntry.*

class ActionDao(private val dbHelper: SQLiteOpenHelper) {

    fun save(db: SQLiteDatabase, action: Action.CreateActionModel) {
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME_CATEGORY_ID, action.categoryId)
        contentValues.put(COLUMN_NAME_DATE, action.date.toDate().time)
        contentValues.put(COLUMN_NAME_TYPE, action.type.name)
        db.insert(TABLE_NAME, null, contentValues)
    }

    fun switchAction(categoryId: Int?): Action.ActionType {
        dbHelper.writableDatabase.use { db ->
            db.beginTransaction()
            val lastAction = lastCategoryAction(db, categoryId)
            val newAction: Action.CreateActionModel
            if ((lastAction?.type ?: Action.ActionType.PAUSE).equals(Action.ActionType.PAUSE)) {
                newAction = Action.CreateActionModel(Action.ActionType.PLAY, categoryId, LocalDateTime())
            } else {
                newAction = Action.CreateActionModel(Action.ActionType.PAUSE, categoryId, LocalDateTime())
            }
            save(db, newAction)
            db.setTransactionSuccessful()
            db.endTransaction()
            return newAction.type
        }

    }

    fun switchAction(categoryId: Int?, actionType: Action.ActionType): Action.ActionType {
        dbHelper.writableDatabase.use { db ->
            db.beginTransaction()
            val newAction: Action.CreateActionModel
            if (actionType == Action.ActionType.PAUSE) {
                newAction = Action.CreateActionModel(Action.ActionType.PAUSE, categoryId, LocalDateTime())
            } else {
                newAction = Action.CreateActionModel(Action.ActionType.PLAY, categoryId, LocalDateTime())
            }
            save(db, newAction)
            db.setTransactionSuccessful()
            db.endTransaction()
            return newAction.type
        }

    }

    fun findByCategoryIdCursor(categoryId: Int?): Cursor {
        return dbHelper.readableDatabase
                .rawQuery(String.format("select * from %s where %s=%s",
                        TABLE_NAME, COLUMN_NAME_CATEGORY_ID, categoryId), null)
    }

    fun findByCategoryId(categoryId: Int?): Set<Action> {
        val cursor = findByCategoryIdCursor(categoryId)
        val actions = HashSet<Action>()
        while (cursor.moveToNext()) {
            actions.add(currentCursorStateToAction(cursor))
        }
        return actions
    }

    private fun lastCategoryAction(db: SQLiteDatabase, categoryId: Int?): Action? {
        val cursor = db.rawQuery(
                String.format("select * from %s where %s=%s order by %s desc limit 1",
                        TABLE_NAME, COLUMN_NAME_CATEGORY_ID, categoryId, COLUMN_NAME_DATE), null)
        val action: Action?
        if (cursor.moveToNext())
            action = currentCursorStateToAction(cursor)
        else
            action = null
        return action
    }

    private fun currentCursorStateToAction(cursor: Cursor): Action {
        return Action(
                cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                Action.ActionType.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TYPE))),
                cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CATEGORY_ID)),
                Date(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_DATE)))
        )
    }

    fun todayActions(today: LocalDateTime, timeBeginOfDay: LocalTime, categoryId: Int?): List<Action> {
        val realToday = realToday(today, timeBeginOfDay)
        val beginOfDay = LocalDateTime(
                realToday.year,
                realToday.monthOfYear,
                realToday.dayOfMonth,
                timeBeginOfDay.hourOfDay, timeBeginOfDay.minuteOfHour, timeBeginOfDay.secondOfMinute)
        val endOfDay = beginOfDay.plusHours(24)

        val cursor = dbHelper.readableDatabase.rawQuery(
                String.format("select * from %s where %s >= %s and %s < %s and %s=%s order by %s",
                        TABLE_NAME, COLUMN_NAME_DATE, beginOfDay.toDate().time,
                        COLUMN_NAME_DATE, endOfDay.toDate().time, COLUMN_NAME_CATEGORY_ID, categoryId, COLUMN_NAME_DATE), null)
        val actions = LinkedList<Action>()
        while (cursor.moveToNext()) {
            actions.add(currentCursorStateToAction(cursor))
        }

        return actions
    }

    fun calcCurrentWeekLogged(today: LocalDateTime, beginOfDay: LocalTime, categoryId: Int?): List<Duration> {
        val realToday = realToday(today, beginOfDay)
        return (1..7).map { i -> Duration(calcTodayLogged(realToday.toLocalDateTime(beginOfDay).withDayOfWeek(i), beginOfDay, categoryId)) }
    }

    fun calcTodayLogged(today: LocalDateTime, beginOfDay: LocalTime, categoryId: Int?): Long? {
        val actions = LinkedList(todayActions(today, beginOfDay, categoryId))
        val realToday = realToday(today, beginOfDay)
        val beginOfDayFullDate = LocalDateTime(
                realToday.year, realToday.monthOfYear, realToday.dayOfMonth,
                beginOfDay.hourOfDay, beginOfDay.minuteOfHour, beginOfDay.secondOfMinute
        )
        val lastActionBefore = lastActionBefore(beginOfDayFullDate.withHourOfDay(beginOfDay.hourOfDay))
        var lastPlayAction: LocalDateTime? = null
        if (lastActionBefore != null && lastActionBefore.type == Action.ActionType.PLAY)
            lastPlayAction = LocalDateTime(lastActionBefore.date)
        actions.addLast(Action(
                Integer.MAX_VALUE,
                Action.ActionType.PAUSE,
                categoryId,
                LocalDateTime().toDate()))
        var sum: Long = 0L
        for (currentAction in actions) {
            if (currentAction.type == Action.ActionType.PLAY) {
                if (lastPlayAction == null) lastPlayAction = LocalDateTime(currentAction.date)
            } else {
                if (lastPlayAction != null) {
                    if (lastPlayAction.compareTo(beginOfDayFullDate) == 1)
                        sum += Duration(lastPlayAction.toDate().getTime(), LocalDateTime(currentAction.date).toDate().time).millis
                    else
                        sum += Duration(beginOfDayFullDate.toDate().time, LocalDateTime(currentAction.date).toDate().time).millis
                    lastPlayAction = null
                }
            }
        }
        return sum
    }

    fun lastActionBefore(time: LocalDateTime): Action? {
        val cursor = dbHelper.readableDatabase.rawQuery(
                String.format("select * from %s where %s < %s order by %s desc limit 1",
                        TABLE_NAME, COLUMN_NAME_DATE, time.toDate().time, COLUMN_NAME_DATE), null)
        if (cursor.moveToNext())
            return currentCursorStateToAction(cursor)
        else
            return null
    }

    private fun firstActionAfter(time: LocalDateTime): Action? {
        val cursor = dbHelper.readableDatabase.rawQuery(
                String.format("select * from %s where %s >= %s order by %s limit 1",
                        TABLE_NAME, COLUMN_NAME_DATE, time.toDate().time, COLUMN_NAME_DATE), null)
        if (cursor.moveToNext())
            return currentCursorStateToAction(cursor)
        else
            return null
    }

    private fun realToday(now: LocalDateTime, beginOfDay: LocalTime): LocalDate {
        if (now.toLocalTime().compareTo(beginOfDay) < 0)
            return now.toLocalDate().minusDays(1)
        else
            return now.toLocalDate()
    }

    fun lastCategoryAction(categoryId: Int?): Action? {
        val cursor = dbHelper.readableDatabase.rawQuery(
                String.format("select * from %s where %s = %s order by %s desc limit 1",
                        TABLE_NAME, COLUMN_NAME_CATEGORY_ID, categoryId, COLUMN_NAME_DATE), null)
        if (cursor.moveToNext())
            return currentCursorStateToAction(cursor)
        else
            return null
    }
}
