package com.timetracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView

import com.timetracker.dao.ActionDao
import com.timetracker.dao.CategoryDao
import com.timetracker.db.DbHelper
import com.timetracker.entities.Category

import com.timetracker.Constants.*

import org.joda.time.LocalDateTime
import org.joda.time.format.PeriodFormatterBuilder

import java.text.DateFormatSymbols
import java.util.Calendar

class WeeklyStats : AppCompatActivity() {

    private var dbHelper: DbHelper? = null
    private var categoryDao: CategoryDao? = null
    private var actionDao: ActionDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContentView(R.layout.content_weekly_stats)

        val intent = intent
        val category = intent.getSerializableExtra(CATEGORY_EXTRA_KEY) as Category

        val weeklyStatsList = findViewById(R.id.weekly_stats_list) as ListView

        val formatter = PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix("h")
                .appendMinutes()
                .appendSuffix("m")
                .appendSeconds()
                .appendSuffix("s")
                .toFormatter()

        val firstDay = Calendar.getInstance().firstDayOfWeek - 1
        val weekDaysStream = emptySequence<String>().plus(DateFormatSymbols.getInstance().weekdays)
                .drop(1)
        val weekDaysIterator = weekDaysStream.plus(weekDaysStream).drop(firstDay).iterator()
        val durations = actionDao!!
                .calcCurrentWeekLogged(LocalDateTime(), BEGIN_OF_DAY, category.id)
                .map { d -> weekDaysIterator.next() + ": " + formatter.print(d.toPeriod()) }
        val adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                durations
        )

        weeklyStatsList.adapter = adapter
    }

    private fun init() {
        dbHelper = DbHelper(baseContext)
        categoryDao = CategoryDao(dbHelper!!)
        actionDao = ActionDao(dbHelper!!)
    }

    companion object {

        var CATEGORY_EXTRA_KEY = "category"
    }

}
