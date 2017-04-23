package com.timetracker.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.timetracker.MainActivity;
import com.timetracker.dao.ActionDao;
import com.timetracker.db.DbHelper;
import com.timetracker.entities.Action;

public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_FIELD = "ACTION";
    public static final String CATEGORY_FIELD = "CATEGORY";

    public static final String ACTION_PAUSE = "com.timetracker.actions.PAUSE";
    public static final String ACTION_PLAY = "com.timetracker.actions.PLAY";
    public static final String ACTION_STOP = "com.timetracker.actions.STOP";
    public static final String ACTION_CLOSE = "com.timetracker.actions.CLOSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        ActionDao actionDao = new ActionDao(new DbHelper(context));
        Integer categoryId = intent.getIntExtra(CATEGORY_FIELD, -1);

        Intent updateIntent = new Intent(MainActivity.UPDATE_ACTION_BROADCAST);

        switch (intent.getAction()) {
            case ACTION_PAUSE: {
                Action.ActionType resultActionType = actionDao.switchAction(categoryId, Action.ActionType.PAUSE);
                MainActivity.sendNotification(context, context.getPackageName(),
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE),
                        false, categoryId, actionDao);
                context.sendBroadcast(updateIntent);
                break;
            }
            case ACTION_PLAY: {
                Action.ActionType resultActionType = actionDao.switchAction(categoryId, Action.ActionType.PLAY);
                MainActivity.sendNotification(context, context.getPackageName(),
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE),
                        resultActionType.equals(Action.ActionType.PLAY), categoryId, actionDao);
                context.sendBroadcast(updateIntent);
                break;
            }
            case ACTION_STOP:
                actionDao.switchAction(categoryId, Action.ActionType.PAUSE);
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(MainActivity.NOTIFICATION_ID);
                context.sendBroadcast(updateIntent);
                break;
            case ACTION_CLOSE:
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(MainActivity.NOTIFICATION_ID);
                break;
        }
    }
}
