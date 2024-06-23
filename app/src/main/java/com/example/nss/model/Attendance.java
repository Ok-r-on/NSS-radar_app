package com.example.nss.model;

//This is for attendance format in the attendance fragment on the senior side
public class Attendance {
    String eventName,eventDate,count;
    private long tmpstmp;
    public long getTmpstmp() {
        return tmpstmp;
    }

    public Attendance() {
    }

    public Attendance(String eventName, String eventDate, String count, long tmpstmp) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.count = count;
        this.tmpstmp=tmpstmp;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
