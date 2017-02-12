package com.timetracker.entities;

import org.joda.time.LocalDateTime;

import java.util.Date;

public class Action {

    public final Integer id;
    public final ActionType type;
    public final Integer categoryId;
    public final Date date;

    public Action(Integer id, ActionType type, Integer categoryId, Date date) {
        this.id = id;
        this.type = type;
        this.categoryId = categoryId;
        this.date = date;
    }

    public static enum ActionType {
        PAUSE,
        PLAY;
    }

    public static class CreateActionModel {
        public final ActionType type;
        public final Integer categoryId;
        public final LocalDateTime date;

        public CreateActionModel(ActionType type, Integer categoryId, LocalDateTime date) {
            this.type = type;
            this.categoryId = categoryId;
            this.date = date;
        }
    }
}
