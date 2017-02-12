package com.timetracker.entities;

import java.util.Date;

public class Record {

    public final int id;
    public final String category;
    public final Date startTime;
    public final Date endTime;

    public Record(Integer id, String category, Date startTime, Date endTime) {
        this.id = id;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
