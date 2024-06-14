package com.example.nss.model;

public class Schedule {
    String eventTime,eventName,eventType,eventLoc,eventDate;

    public String getEventDate() {
        return eventDate;
    }
    public Schedule(){}

    public Schedule(String date, String time, String eventName, String eventType, String eventLoc) {
        this.eventTime = time;
        this.eventDate = date;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventLoc = eventLoc;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getEventName() {
        return eventName;
    }
    public String getEventType() {
        return eventType;
    }
    public String getEventLoc() {
        return eventLoc;
    }
}
