package com.timetracker;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.timetracker.dao.ActionDao;
import com.timetracker.dao.CategoryDao;
import com.timetracker.db.CategoriesContract;
import com.timetracker.db.DbHelper;
import com.timetracker.entities.Action;
import com.timetracker.entities.Category;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private CategoryDao categoryDao;
    private ActionDao actionDao;

    private Integer dayChangeHours = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        setContentView(R.layout.activity_main);

        ListView recordsList = (ListView) findViewById(R.id.records_list);

        ListAdapter adapter = new CategoryArrayAdapter(
                getApplicationContext(),
                android.R.layout.simple_list_item_1,
                categoryDao.list().collect(Collectors.toList()));
        recordsList.setAdapter(adapter);
        recordsList.setOnItemClickListener((parent, view, position, id) -> {
            Category item = (Category) adapter.getItem(position);
            Chronometer chronometer = (Chronometer) view.findViewById(R.id.category_chronometer);
            if (actionDao.switchAction(item.id).equals(Action.ActionType.PAUSE))
                chronometer.stop();
            else {
                LocalTime beginOfDay = new LocalTime(6, 0);
                LocalDate today = (beginOfDay.compareTo(new LocalTime()) == 1) ? new LocalDate().minusDays(1) : new LocalDate();
                chronometer.setBase(SystemClock.elapsedRealtime() - actionDao.calcTodayLogged(today, beginOfDay, item.id));
                chronometer.start();
            }
        });
    }

    private void init() {
        dbHelper = new DbHelper(getBaseContext());
        categoryDao = new CategoryDao(dbHelper);
        actionDao = new ActionDao(dbHelper);
    }

    private class CategoryArrayAdapter extends ArrayAdapter<Category> {

        public CategoryArrayAdapter(Context context, int resource, List<Category> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.category_text_view);

            Chronometer chronometer = (Chronometer) convertView.findViewById(R.id.category_chronometer);
            LocalTime beginOfDay = new LocalTime(6, 0);
            LocalDate today = (beginOfDay.compareTo(new LocalTime()) == 1) ? new LocalDate().minusDays(1) : new LocalDate();
            chronometer.setBase(SystemClock.elapsedRealtime() - actionDao.calcTodayLogged(today, beginOfDay, getItem(position).id));

            textView.setText(getItem(position).name);

            return convertView;
        }
    }

    public CategoryListItem of(Category category) {
        return new CategoryListItem(category, new Chronometer(getApplicationContext()));
    }

    public static class CategoryListItem {
        public final Category category;
        public final Chronometer chronometer;

        public CategoryListItem(Category category, Chronometer chronometer) {
            this.category = category;
            this.chronometer = chronometer;
        }
    }

}
