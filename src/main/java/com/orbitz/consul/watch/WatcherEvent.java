package com.orbitz.consul.watch;

public class WatcherEvent<T> {

    public enum WatcherEventType {
        UPDATE,
        DELETE
    }

    private T data;
    private WatcherEventType eventType;

    public WatcherEvent(T data, WatcherEventType eventType) {
        this.data = data;
        this.eventType = eventType;
    }

    public T getData() {
        return data;
    }

    public WatcherEventType getEventType() {
        return eventType;
    }
}
