package com.timetracker.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.timetracker.entities.Action;
import com.timetracker.entities.Category;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.timetracker.db.ActionsContract.ActionEntry.*;

public class ActionDao {

    private final SQLiteOpenHelper dbHelper;

    public ActionDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void save(SQLiteDatabase db, Action.CreateActionModel action) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME_CATEGORY_ID, action.categoryId);
        contentValues.put(COLUMN_NAME_DATE, action.date.toDate().getTime());
        contentValues.put(COLUMN_NAME_TYPE, action.type.name());
        db.insert(TABLE_NAME, null, contentValues);
    }

    public Action.ActionType switchAction(Integer categoryId) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.beginTransaction();
            Optional<Action> lastAction = lastCategoryAction(db, categoryId);
            Action.CreateActionModel newAction;
            if (lastAction.map((action) -> action.type)
                    .orElse(Action.ActionType.PAUSE).equals(Action.ActionType.PAUSE)) {
                newAction = new Action.CreateActionModel(Action.ActionType.PLAY, categoryId, new LocalDateTime());
            } else {
                newAction = new Action.CreateActionModel(Action.ActionType.PAUSE, categoryId, new LocalDateTime());
            }
            save(db, newAction);
            db.setTransactionSuccessful();
            db.endTransaction();
            return newAction.type;
        }

    }

    public Action.ActionType switchAction(Integer categoryId, Action.ActionType actionType) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.beginTransaction();
            Action.CreateActionModel newAction;
            if (actionType.equals(Action.ActionType.PAUSE)) {
                newAction = new Action.CreateActionModel(Action.ActionType.PAUSE, categoryId, new LocalDateTime());
            } else {
                newAction = new Action.CreateActionModel(Action.ActionType.PLAY, categoryId, new LocalDateTime());
            }
            save(db, newAction);
            db.setTransactionSuccessful();
            db.endTransaction();
            return newAction.type;
        }

    }

    public Cursor findByCategoryIdCursor(Integer categoryId) {
        return dbHelper.getReadableDatabase()
                .rawQuery(String.format("select * from %s where %s=%s",
                        TABLE_NAME, COLUMN_NAME_CATEGORY_ID, categoryId), null);
    }

    public Set<Action> findByCategoryId(Integer categoryId) {
        Cursor cursor = findByCategoryIdCursor(categoryId);
        Set<Action> actions = new HashSet<>();
        while (cursor.moveToNext()) {
            actions.add(currentCursorStateToAction(cursor));
        }
        return actions;
    }

    private Optional<Action> lastCategoryAction(SQLiteDatabase db, Integer categoryId) {
        Cursor cursor = db.rawQuery(
                String.format("select * from %s where %s=%s order by %s desc limit 1",
                        TABLE_NAME, COLUMN_NAME_CATEGORY_ID, categoryId, COLUMN_NAME_DATE), null);
        Optional<Action> action;
        if (cursor.moveToNext()) action = Optional.of(currentCursorStateToAction(cursor));
        else action = Optional.empty();
        return action;
    }

    private Action currentCursorStateToAction(Cursor cursor) {
        return new Action(
                cursor.getInt(cursor.getColumnIndex(_ID)),
                Action.ActionType.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TYPE))),
                cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CATEGORY_ID)),
                new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_DATE)))
        );
    }

    public List<Action> todayActions(LocalDateTime today, LocalTime timeBeginOfDay, Integer categoryId) {
        LocalDate realToday = realToday(today, timeBeginOfDay);
        LocalDateTime beginOfDay = new LocalDateTime(
                realToday.getYear(),
                realToday.getMonthOfYear(),
                realToday.getDayOfMonth(),
                timeBeginOfDay.getHourOfDay(), timeBeginOfDay.getMinuteOfHour(), timeBeginOfDay.getSecondOfMinute());
        LocalDateTime endOfDay = beginOfDay.plusHours(24);

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                String.format("select * from %s where %s >= %s and %s < %s and %s=%s order by %s",
                        TABLE_NAME, COLUMN_NAME_DATE, beginOfDay.toDate().getTime(),
                        COLUMN_NAME_DATE, endOfDay.toDate().getTime(), COLUMN_NAME_CATEGORY_ID, categoryId, COLUMN_NAME_DATE), null);
        LinkedList<Action> actions = new LinkedList<>();
        while (cursor.moveToNext()) {
            actions.add(currentCursorStateToAction(cursor));
        }

        return actions;
    }

    public List<Duration> calcCurrentWeekLogged(LocalDateTime today, LocalTime beginOfDay, Integer categoryId) {
        LocalDate realToday = realToday(today, beginOfDay);
        return IntStream.range(1, 8)
                .mapToObj((i) -> calcTodayLogged(realToday.toLocalDateTime(beginOfDay).withDayOfWeek(i), beginOfDay, categoryId))
                .map(Duration::new)
                .collect(Collectors.toList());
    }

    public Long calcTodayLogged(LocalDateTime today, LocalTime beginOfDay, Integer categoryId) {
        LinkedList<Action> actions = new LinkedList<>(todayActions(today, beginOfDay, categoryId));
        LocalDate realToday = realToday(today, beginOfDay);
        LocalDateTime beginOfDayFullDate = new LocalDateTime(
                realToday.getYear(), realToday.getMonthOfYear(), realToday.getDayOfMonth(),
                beginOfDay.getHourOfDay(), beginOfDay.getMinuteOfHour(), beginOfDay.getSecondOfMinute()
                );
        Optional<Action> lastActionBefore = lastActionBefore(beginOfDayFullDate.withHourOfDay(beginOfDay.getHourOfDay()));
        Optional<LocalDateTime> lastPlayAction = Optional.empty();
        if (lastActionBefore.isPresent() && lastActionBefore.get().type == Action.ActionType.PLAY)
            lastPlayAction = Optional.of(new LocalDateTime(lastActionBefore.get().date));
        actions.addLast(new Action(
                Integer.MAX_VALUE,
                Action.ActionType.PAUSE,
                categoryId,
                new LocalDateTime().toDate()));
        Long sum = 0l;
        for (Action currentAction: actions) {
            if (currentAction.type == Action.ActionType.PLAY) {
                if (!lastPlayAction.isPresent()) lastPlayAction = Optional.of(new LocalDateTime(currentAction.date));
            } else {
                if (lastPlayAction.isPresent()) {
                    if (lastPlayAction.get().compareTo(beginOfDayFullDate) == 1)
                        sum += new Duration(lastPlayAction.get().toDate().getTime(), new LocalDateTime(currentAction.date).toDate().getTime()).getMillis();
                    else
                        sum += new Duration(beginOfDayFullDate.toDate().getTime(), new LocalDateTime(currentAction.date).toDate().getTime()).getMillis();
                    lastPlayAction = Optional.empty();
                }
            }
        }
        return sum;
    }

    public Optional<Action> lastActionBefore(LocalDateTime time) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                String.format("select * from %s where %s < %s order by %s desc limit 1",
                        TABLE_NAME, COLUMN_NAME_DATE, time.toDate().getTime(), COLUMN_NAME_DATE), null);
        if (cursor.moveToNext())
            return Optional.of(currentCursorStateToAction(cursor));
        else
            return Optional.empty();
    }

    private Optional<Action> firstActionAfter(LocalDateTime time) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                String.format("select * from %s where %s >= %s order by %s limit 1",
                        TABLE_NAME, COLUMN_NAME_DATE, time.toDate().getTime(), COLUMN_NAME_DATE), null);
        if (cursor.moveToNext())
            return Optional.of(currentCursorStateToAction(cursor));
        else
            return Optional.empty();
    }

    private LocalDate realToday(LocalDateTime now, LocalTime beginOfDay) {
        if (now.toLocalTime().compareTo(beginOfDay) < 0)
            return now.toLocalDate().minusDays(1);
        else
            return now.toLocalDate();
    }

    public Optional<Action> lastCategoryAction(Integer categoryId) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                String.format("select * from %s where %s = %s order by %s desc limit 1",
                        TABLE_NAME, COLUMN_NAME_CATEGORY_ID, categoryId, COLUMN_NAME_DATE), null);
        if (cursor.moveToNext())
            return Optional.of(currentCursorStateToAction(cursor));
        else
            return Optional.empty();
    }
}
