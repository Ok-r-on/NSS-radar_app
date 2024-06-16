package com.example.nss.model;

public class Schedule {
    private String eventTime,eventName,eventType,eventLoc,eventDate;
    private long tmpstmp;

    public String getEventDate() {
        return eventDate;
    }
    public Schedule(){}

    public long getTmpstmp() {
        return tmpstmp;
    }

    public Schedule(String date, String time, String eventName, String eventType, String eventLoc, long tmpstmp) {
        this.eventTime = time;
        this.eventDate = date;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventLoc = eventLoc;
        this.tmpstmp=tmpstmp;
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
