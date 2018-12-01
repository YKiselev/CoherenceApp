package org.uze.coherence;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.processor.VersionedPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uze.coherence.model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class VersionedPutClientApp {

    public static final int KEYS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicLong counter = new AtomicLong(System.nanoTime());

    public static void main(String[] args) throws Exception {
        new VersionedPutClientApp().run();
    }

    private void run() throws Exception {
        logger.info("Starting...");
        final NamedCache items = CacheFactory.getCache("Items");

        final int nThreads = 8;
        final int nIterations = 5;
        final ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        try {
            for (int p = 1; p <= nIterations; p++) {
                logger.info("Clearing...");
                items.clear();
                logger.info("Current cache size is {} items", items.size());
                counter.set(0);
                logger.info("Iteration #{}", p);
                final List<Future> futures = new ArrayList<>();
                for (int k = 0; k < KEYS; k++) {
                    final long key = k;
                    for (int t = 0; t < nThreads; t++) {
                        futures.add(pool.submit(() -> doPut(items, key)));
                    }
                }
                logger.info("Waiting for {} futures", futures.size());
                for (Future future : futures) {
                    future.get();
                }
                logger.info("size={}, expected={}", items.size(), KEYS);
            }

        } finally {
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);
        }
        logger.info("Done!");
    }

    private void doPut(NamedCache cache, long key) {
        long version = 0;
        for (; ; ) {
            final Item item = new Item(
                    Long.toString(key),
                    Collections.emptyList(),
                    version
            );
            final VersionedPut<Object, Item> processor = new VersionedPut<>(
                    item,
                    true,
                    true
            );
            final Object result = cache.invoke(key, processor);
            if (result == null) {
                counter.incrementAndGet();
                Object v = cache.get(key);
                logger.info("Put: {}={}", key, v);
                break;
            } else if (result != item) {
                final Item existing = (Item) result;
                version = existing.getVersion();
            } else {
                counter.incrementAndGet();
            }
        }
    }
}
