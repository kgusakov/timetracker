package com.timetracker;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.timetracker.dao.ActionDao;
import com.timetracker.dao.CategoryDao;
import com.timetracker.db.DbHelper;
import com.timetracker.entities.Action;
import com.timetracker.entities.Category;

import static com.timetracker.Constants.*;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private CategoryDao categoryDao;
    private ActionDao actionDao;

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
                chronometer.setBase(SystemClock.elapsedRealtime() - actionDao.calcTodayLogged(new LocalDateTime(), BEGIN_OF_DAY, item.id));
                chronometer.start();
            }
        });
        recordsList.setOnItemLongClickListener((parent, view, position, id) -> {
            Category item = (Category) adapter.getItem(position);
            Intent intent = new Intent(this, WeeklyStats.class);
            intent.putExtra(WeeklyStats.CATEGORY_EXTRA_KEY, item);
            startActivity(intent);
            return true;
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
            chronometer.setBase(SystemClock.elapsedRealtime() - actionDao.calcTodayLogged(new LocalDateTime(), beginOfDay, getItem(position).id));

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
