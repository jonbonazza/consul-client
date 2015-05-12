package com.orbitz.consul.watch;

public interface ConsulWatcherCallback<T> {

    void onUpdate(WatcherEvent<T> event);
}
