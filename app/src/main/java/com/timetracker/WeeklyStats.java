package com.timetracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.timetracker.dao.ActionDao;
import com.timetracker.dao.CategoryDao;
import com.timetracker.db.DbHelper;
import com.timetracker.entities.Category;

import static com.timetracker.Constants.*;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WeeklyStats extends AppCompatActivity {

    private DbHelper dbHelper;
    private CategoryDao categoryDao;
    private ActionDao actionDao;

    public static String CATEGORY_EXTRA_KEY = "category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.content_weekly_stats);

        Intent intent = getIntent();
        Category category = (Category) intent.getSerializableExtra(CATEGORY_EXTRA_KEY);

        ListView weeklyStatsList = (ListView) findViewById(R.id.weekly_stats_list);

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix("h")
                .appendMinutes()
                .appendSuffix("m")
                .appendSeconds()
                .appendSuffix("s")
                .toFormatter();

        int firstDay = Calendar.getInstance().getFirstDayOfWeek() - 1;
        List<String> weekDaysStream = Arrays
                .stream(DateFormatSymbols.getInstance().getWeekdays())
                .skip(1)
                .collect(Collectors.toList());
        Iterator<String> weekDaysIterator =
                Stream.concat(weekDaysStream.stream(), weekDaysStream.stream()).skip(firstDay).iterator();
        List<String> durations = actionDao
                .calcCurrentWeekLogged(new LocalDateTime(), BEGIN_OF_DAY, category.id)
                .stream()
                .map((d) -> weekDaysIterator.next() + ": " + formatter.print(d.toPeriod()))
                .collect(Collectors.toList());
        ListAdapter adapter = new ArrayAdapter<>(
                getApplicationContext(),
                android.R.layout.simple_list_item_1,
                durations
                );

        weeklyStatsList.setAdapter(adapter);
    }

    private void init() {
        dbHelper = new DbHelper(getBaseContext());
        categoryDao = new CategoryDao(dbHelper);
        actionDao = new ActionDao(dbHelper);
    }

}
