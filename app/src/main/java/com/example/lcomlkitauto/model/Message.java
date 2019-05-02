package com.example.lcomlkitauto.model;

public class Message {

    private final String content;
    private final long timestamp;
    private final boolean isLocalUser;

    public Message(String content, long timestamp, boolean isLocalUser) {
        this.content = content;
        this.timestamp = timestamp;
        this.isLocalUser = isLocalUser;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isLocalUser() {
        return isLocalUser;
    }
}
