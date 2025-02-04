package io.questdb.std;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicLong;

public class Closeables {
    private static final AtomicLong objIdCounter = new AtomicLong();

    public static long nextObjId() {
        return objIdCounter.incrementAndGet();
    }

    public static class CloseableTacking {
        public final long id;
        public final StackTraceElement[] bt;
        public final Closeable obj;
        public final long ticks;

        public CloseableTacking(long id, StackTraceElement[] bt, Closeable obj) {
            this.id = id;
            this.bt = bt;
            this.obj = obj;
            this.ticks = Unsafe.ticks.get();
        }
    }

    public static final LongObjHashMap<CloseableTacking> opened = new LongObjHashMap<>();

    public static LongHashSet snapshot() {
        final LongHashSet list = new LongHashSet();
        synchronized (opened) {
            opened.forEach((id, tracking) -> {
                list.add(id);
            });
        }
        return list;
    }

    public static void trackOpened(long id, @NotNull Closeable closeable) {
        final var bt = Thread.currentThread().getStackTrace();
        final var tracking = new CloseableTacking(id, bt, closeable);
        synchronized (opened) {
            assert opened.get(id) == null;
            opened.put(id, tracking);
        }
    }

    public static void trackClosed(long id) {
        synchronized (opened) {
            opened.remove(id);
//            if (opened.remove(id) == -1) {
//                throw new IllegalStateException("Already closed");
//            }
        }
    }
}
