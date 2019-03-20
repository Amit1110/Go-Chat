package com.example.amit.hey;

import android.os.Message;

public class Messages {
    private String message, type;
    long time;
    boolean seen;


    public Messages(String message, boolean seen, long time, String  type) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String  getType() {
        return type;
    }

    public void setType(String  type) {
        this.type = type;
    }

    public Messages(){

    }

}
