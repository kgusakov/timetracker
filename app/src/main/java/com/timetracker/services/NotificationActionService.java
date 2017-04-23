package com.timetracker.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;

import com.timetracker.MainActivity;
import com.timetracker.dao.ActionDao;
import com.timetracker.db.DbHelper;
import com.timetracker.entities.Action;

public class NotificationActionService extends IntentService {
    public static final String CATEGORY_FIELD = "CATEGORY";
    public static final String ACTION_NAME = "ACTION";

    public static final String ACTION_PAUSE = "com.timetracker.actions.PAUSE";
    public static final String ACTION_PLAY = "com.timetracker.actions.PLAY";
    public static final String ACTION_STOP = "com.timetracker.actions.STOP";
    public static final String ACTION_CLOSE = "com.timetracker.actions.CLOSE";

    public NotificationActionService() {
        super(NotificationActionService.class.getName());
    }

    public NotificationActionService(String name) {
        super(name);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        ActionDao actionDao = new ActionDao(new DbHelper(this));
        Integer categoryId = intent.getIntExtra(CATEGORY_FIELD, -1);

        Intent updateIntent = new Intent(MainActivity.UPDATE_ACTION_BROADCAST);

        switch (intent.getStringExtra(ACTION_NAME)) {
            case ACTION_PAUSE: {
                Action.ActionType resultActionType = actionDao.switchAction(categoryId, Action.ActionType.PAUSE);
                MainActivity.sendNotification(this, this.getPackageName(),
                        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE),
                        resultActionType.equals(Action.ActionType.PLAY), categoryId, actionDao);
                this.sendBroadcast(updateIntent);
                break;
            }
            case ACTION_PLAY: {
                Action.ActionType resultActionType = actionDao.switchAction(categoryId, Action.ActionType.PLAY);
                MainActivity.sendNotification(this, this.getPackageName(),
                        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE),
                        resultActionType.equals(Action.ActionType.PLAY), categoryId, actionDao);
                this.sendBroadcast(updateIntent);
                break;
            }
            case ACTION_STOP:
                actionDao.switchAction(categoryId, Action.ActionType.PAUSE);
                ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(MainActivity.NOTIFICATION_ID);
                this.sendBroadcast(updateIntent);
                break;
            case ACTION_CLOSE:
                ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(MainActivity.NOTIFICATION_ID);
                break;

        }
        stopSelf();
    }
}
