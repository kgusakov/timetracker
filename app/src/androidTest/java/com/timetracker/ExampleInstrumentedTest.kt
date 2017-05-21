package com.timetracker

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import com.timetracker.dao.ActionDao
import com.timetracker.dao.CategoryDao
import com.timetracker.db.DbHelper
import com.timetracker.entities.Action
import com.timetracker.entities.Category

import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.util.ArrayList

import android.support.test.InstrumentationRegistry.getTargetContext
import org.junit.Assert.*
import com.timetracker.entities.Action.ActionType.*

/**
 * Instrumentation test, which will execute on an Android device.

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    internal var context = InstrumentationRegistry.getContext()
    internal var dbHelper: DbHelper? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        getTargetContext().deleteDatabase(DbHelper.DATABASE_NAME)
        dbHelper = DbHelper(getTargetContext())
    }

    @Test
    @Throws(Exception::class)
    fun dailyStats() {
        val categoryDao = CategoryDao(dbHelper!!)
        val actionDao = ActionDao(dbHelper!!)

        val testCategoryName = "testCategory"
        categoryDao.save(Category.CreateCategory(testCategoryName))
        val testCategory = categoryDao.list().filter { c -> c.name.equals(testCategoryName) }.first()

        val today = LocalDate(2017, 2, 2)
        val beginOfTime = LocalTime(6, 0, 0)

        val actions = ArrayList<Action.CreateActionModel>()
        actions.add(Action.CreateActionModel(PLAY, testCategory.id, localDTToDate(today, LocalTime(3, 0, 0))))
        actions.add(Action.CreateActionModel(PAUSE, testCategory.id, localDTToDate(today, LocalTime(9, 0, 0))))
        actions.add(Action.CreateActionModel(PLAY, testCategory.id, localDTToDate(today, LocalTime(12, 0, 0))))
        actions.add(Action.CreateActionModel(PAUSE, testCategory.id, localDTToDate(today, LocalTime(15, 0, 15))))
        actions.add(Action.CreateActionModel(PLAY, testCategory.id, localDTToDate(today.plusDays(1), LocalTime(3, 0, 0))))
        actions.add(Action.CreateActionModel(PAUSE, testCategory.id, localDTToDate(today.plusDays(1), LocalTime(4, 0, 0))))
        dbHelper!!.writableDatabase.use { db ->
            for (action in actions) {
                actionDao.save(db, action)
            }
        }

        assertEquals((7 * 60 * 60 * 1000 + 15 * 1000).toLong(), actionDao.calcTodayLogged(today.toLocalDateTime(beginOfTime), beginOfTime, testCategory.id)!!.toLong())
    }

    @Test
    fun weeklyStats() {
        val categoryDao = CategoryDao(dbHelper!!)
        val actionDao = ActionDao(dbHelper!!)

        val testCategoryName = "testCategory"
        categoryDao.save(Category.CreateCategory(testCategoryName))
        val testCategory = categoryDao.list().filter { c -> c.name == testCategoryName }.first()

        val beginOfTime = LocalTime(6, 0, 0)

        val actions = ArrayList<Action.CreateActionModel>()
        actions.add(Action.CreateActionModel(PLAY, testCategory.id, localDTToDate(LocalDate(2017, 2, 13), LocalTime(3, 0, 0))))
        actions.add(Action.CreateActionModel(PAUSE, testCategory.id, localDTToDate(LocalDate(2017, 2, 13), LocalTime(9, 0, 0))))

        actions.add(Action.CreateActionModel(PLAY, testCategory.id, localDTToDate(LocalDate(2017, 2, 14), LocalTime(12, 0, 0))))
        actions.add(Action.CreateActionModel(PAUSE, testCategory.id, localDTToDate(LocalDate(2017, 2, 14), LocalTime(15, 0, 0))))

        actions.add(Action.CreateActionModel(PLAY, testCategory.id, localDTToDate(LocalDate(2017, 2, 15), LocalTime(14, 0, 0))))
        actions.add(Action.CreateActionModel(PAUSE, testCategory.id, localDTToDate(LocalDate(2017, 2, 15), LocalTime(15, 0, 0))))

        dbHelper!!.writableDatabase.use { db ->
            for (action in actions) {
                actionDao.save(db, action)
            }
        }

        val wednesday = LocalDate(2017, 2, 15)

        val results = actionDao.calcCurrentWeekLogged(wednesday.toLocalDateTime(beginOfTime), beginOfTime, testCategory.id)

        assertArrayEquals(arrayOf(Duration(3 * 60 * 60 * 1000L), Duration(3 * 60 * 60 * 1000L), Duration(60 * 60 * 1000L), Duration(0L), Duration(0L), Duration(0L), Duration(0L)), results.toTypedArray())
    }

    @Test
    fun categoryDelete() {
        val categoryDao = CategoryDao(dbHelper!!)
        categoryDao.save(Category.CreateCategory("newcategory"))
        val category = categoryDao.findById(1)!!
        categoryDao.delete(category.id)
        assertEquals(null, categoryDao.findById(category.id))
    }

    private fun localDTToDate(date: LocalDate, time: LocalTime): LocalDateTime {
        return LocalDateTime(
                date.year,
                date.monthOfYear,
                date.dayOfMonth,
                time.hourOfDay,
                time.minuteOfHour,
                time.secondOfMinute)
    }
}
