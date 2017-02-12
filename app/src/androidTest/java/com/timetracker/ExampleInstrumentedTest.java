package com.timetracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.timetracker.dao.ActionDao;
import com.timetracker.dao.CategoryDao;
import com.timetracker.db.DbHelper;
import com.timetracker.entities.Action;
import com.timetracker.entities.Category;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static com.timetracker.entities.Action.ActionType.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    Context context = InstrumentationRegistry.getContext();
    DbHelper dbHelper;

    @Before
    public void setUp() throws Exception {
        getTargetContext().deleteDatabase(DbHelper.DATABASE_NAME);
        dbHelper = new DbHelper(getTargetContext());
    }

    @Test
    public void useAppContext() throws Exception {
        CategoryDao categoryDao = new CategoryDao(dbHelper);
        ActionDao actionDao = new ActionDao(dbHelper);

        categoryDao.save(new Category.CreateCategory("testCategory"));

        LocalDate today = new LocalDate(2017, 2, 2);
        LocalTime beginOfTime = new LocalTime(6, 0, 0);

        List<Action.CreateActionModel> actions = new ArrayList<>();
        actions.add(new Action.CreateActionModel(PLAY, 1, localDTToDate(today, new LocalTime(3, 0, 0))));
        actions.add(new Action.CreateActionModel(PAUSE, 1, localDTToDate(today, new LocalTime(9, 0, 0))));
        actions.add(new Action.CreateActionModel(PLAY, 1, localDTToDate(today, new LocalTime(12, 0, 0))));
        actions.add(new Action.CreateActionModel(PAUSE, 1, localDTToDate(today, new LocalTime(15, 0, 15))));
        actions.add(new Action.CreateActionModel(PLAY, 1, localDTToDate(today.plusDays(1), new LocalTime(3, 0, 0))));
        actions.add(new Action.CreateActionModel(PAUSE, 1, localDTToDate(today.plusDays(1), new LocalTime(4, 0, 0))));
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            for (Action.CreateActionModel action : actions) {
                actionDao.save(db, action);
            }
        }

        assertEquals(6*60*60*1000+15*1000+60*60*1000, actionDao.calcTodayLogged(today, beginOfTime, 1).longValue());

    }

    private LocalDateTime localDTToDate(LocalDate date, LocalTime time) {
        return new LocalDateTime(
                date.getYear(),
                date.getMonthOfYear(),
                date.getDayOfMonth(),
                time.getHourOfDay(),
                time.getMinuteOfHour(),
                time.getSecondOfMinute());
    }
}
