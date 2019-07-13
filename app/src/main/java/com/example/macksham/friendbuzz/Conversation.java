package com.example.macksham.friendbuzz;

public class Conversation {
    public long timestamp;

    public Conversation(){

    }

    public Conversation(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
