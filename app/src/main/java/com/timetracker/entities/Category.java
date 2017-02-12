package com.timetracker.entities;

public class Category {

    public final Integer id;
    public final String name;

    public Category(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static class CreateCategory {

        public final String name;

        public CreateCategory(String name) {
            this.name = name;
        }
    }
}
