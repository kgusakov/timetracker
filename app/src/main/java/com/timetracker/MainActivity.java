package com.timetracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.timetracker.dao.ActionDao;
import com.timetracker.dao.CategoryDao;
import com.timetracker.db.DbHelper;
import com.timetracker.entities.Action;
import com.timetracker.entities.Category;
import com.timetracker.services.NotificationActionService;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.timetracker.Constants.*;
import static com.timetracker.services.NotificationActionService.ACTION_CLOSE;
import static com.timetracker.services.NotificationActionService.ACTION_PAUSE;
import static com.timetracker.services.NotificationActionService.ACTION_PLAY;
import static com.timetracker.services.NotificationActionService.ACTION_STOP;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private CategoryDao categoryDao;
    private ActionDao actionDao;

    public static int NOTIFICATION_ID = 1;

    public static String UPDATE_ACTION_BROADCAST = "com.timetracker.UPDATE";

    private BroadcastReceiver notificationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();

        notificationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refresh();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_ACTION_BROADCAST);
        getApplicationContext().registerReceiver(notificationBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getApplicationContext().unregisterReceiver(notificationBroadcastReceiver);
    }

    private void refresh() {
        ListView recordsList = (ListView) findViewById(R.id.records_list);

        ListAdapter adapter = new CategoryArrayAdapter(
                getApplicationContext(),
                android.R.layout.simple_list_item_1,
                categoryDao.list().collect(Collectors.toList()));

        recordsList.setAdapter(adapter);
        recordsList.setOnItemClickListener((parent, view, position, id) -> {
            Category item = (Category) adapter.getItem(position);
            Chronometer chronometer = (Chronometer) view.findViewById(R.id.category_chronometer);
            if (actionDao.switchAction(item.id).equals(Action.ActionType.PAUSE)) {

                chronometer.stop();

                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
            }
            else {
                long base = SystemClock.elapsedRealtime() - actionDao.calcTodayLogged(new LocalDateTime(), BEGIN_OF_DAY, item.id);
                chronometer.setBase(base);
                chronometer.start();

                sendNotification(getApplicationContext(),
                        getPackageName(),
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE),
                        true, item.id, actionDao);
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
            if (actionDao.lastCategoryAction(getItem(position).id).map((action) -> action.type).orElse(Action.ActionType.PAUSE).equals(Action.ActionType.PLAY))
                chronometer.start();
            textView.setText(getItem(position).name);

            return convertView;
        }
    }

    public static void sendNotification(Context context, String packageName,
                                        NotificationManager notificationManager,
                                        boolean chronometerStarted,
                                        Integer categoryId, ActionDao actionDao) {
        long base = SystemClock.elapsedRealtime() - actionDao.calcTodayLogged(new LocalDateTime(), BEGIN_OF_DAY, categoryId);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.stat_notify_voicemail)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        RemoteViews mContentView = new RemoteViews(packageName, R.layout.notification);
        mContentView.setTextViewText(R.id.notification_text_view, "Custom notification");
        mContentView.setChronometer(R.id.notification_chronometer, base, null, chronometerStarted);

        if (chronometerStarted) {
            mBuilder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingIntent(ACTION_PAUSE, 0, categoryId, context)));
            mBuilder.addAction(new NotificationCompat.Action(android.R.drawable.ic_delete, "Finish", pendingIntent(ACTION_STOP, 1, categoryId, context)));
            mBuilder.setOngoing(true);
        } else {
            mBuilder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pendingIntent(ACTION_PLAY, 2, categoryId, context)));
            mBuilder.addAction(new NotificationCompat.Action(android.R.drawable.ic_delete, "Finish", pendingIntent(ACTION_CLOSE, 3, categoryId, context)));
            mBuilder.setOngoing(false);
        }

        mBuilder.setContent(mContentView);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private static PendingIntent pendingIntent(String actionName, Integer requestCode, Integer categoryId, Context context) {
        Intent intent = new Intent(context, NotificationActionService.class);
        intent.putExtra(NotificationActionService.CATEGORY_FIELD, categoryId);
        intent.putExtra(NotificationActionService.ACTION_NAME, actionName);
        return PendingIntent.getService(context, requestCode + categoryId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
