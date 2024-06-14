package com.example.nss.model;

public class Notice {
    String title, description;

    public Notice(){}
    public Notice(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}
