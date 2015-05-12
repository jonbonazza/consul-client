package com.orbitz.consul.watch;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

public abstract class WatcherTask<T> implements Runnable {

    private ConcurrentLinkedQueue<WatcherEvent<T>> channel = new ConcurrentLinkedQueue<>();
    private ChannelProcessor<T> channelProcessor;

    public WatcherTask(ConsulWatcherCallback<T> callback) {
        channelProcessor = new ChannelProcessor<>(channel, callback);
    }

    /**
     * Watches a resource and pushes any events to the responseChannel queue.
     *
     * @param responseChannel A queue that events are pushed to to be processed by the WatcherTask thread.
     */
    protected abstract void watch(Queue<WatcherEvent<T>> responseChannel);

    @Override
    public void run() {
        FutureTask<Void> channelProcessorTask = new FutureTask<>(channelProcessor, null);
        try {
            channelProcessorTask.run();
            watch(channel);
        } finally {
            channelProcessor.stop();
            channelProcessorTask.cancel(false);
        }
    }

    private static class ChannelProcessor<T> implements Runnable {

        private Queue<WatcherEvent<T>> queue;
        private ConsulWatcherCallback<T> callback;
        private boolean done = false;

        public ChannelProcessor(Queue<WatcherEvent<T>> queue, ConsulWatcherCallback<T> callback) {
            this.queue = queue;
            this.callback = callback;
        }

        @Override
        public void run() {
            while(!done) {
                if (!queue.isEmpty())
                    callback.onUpdate(queue.poll());
            }
        }

        public void stop() {
            done = true;
        }
    }
}
