package com.orbitz.consul.watch;

import com.google.common.base.Optional;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.option.QueryOptionsBuilder;

import java.time.Duration;
import java.util.Queue;

public class KeyValueWatcherTask extends WatcherTask<Value> {

    private KeyValueClient client;
    private Duration wait;
    private String key;
    private boolean done = false;

    public KeyValueWatcherTask(KeyValueClient client, ConsulWatcherCallback<Value> callback, String key, Duration wait) {
        super(callback);
        this.client = client;
        this.key = key;
        this.wait = wait;
    }

    public KeyValueWatcherTask(KeyValueClient client, ConsulWatcherCallback<Value> callback, String key) {
        this(client, callback, key, Duration.ofMinutes(10));
    }

    @Override
    protected void watch(Queue<WatcherEvent<Value>> responseChannel) {
        Optional<Value> v = client.getValue(key);
        if (!v.isPresent())
            return;

        int lastIndex = (int) v.get().getModifyIndex();
        while(!done) {
            v = client.getValue(key, QueryOptionsBuilder.builder().
                    blockSeconds((int) wait.getSeconds(), lastIndex).build());

            WatcherEvent<Value> watcherEvent;
            if (v.isPresent()) {
                Value value = v.get();
                if (value.getModifyIndex() == lastIndex)
                    continue;

                lastIndex = (int) value.getModifyIndex();
                watcherEvent = new WatcherEvent<>(value, WatcherEvent.WatcherEventType.UPDATE);
            } else {
                watcherEvent = new WatcherEvent<>(null, WatcherEvent.WatcherEventType.DELETE);
                done = true;
            }

            responseChannel.add(watcherEvent);
        }
    }

    public void stop() {
        this.done = true;
    }
}
