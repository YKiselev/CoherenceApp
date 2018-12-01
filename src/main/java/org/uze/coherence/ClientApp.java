package org.uze.coherence;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.processor.VersionedPutAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uze.coherence.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ClientApp {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicLong sequence = new AtomicLong(System.nanoTime());

    private final ValueExtractor extractor = new KeyExtractor();

    public static void main(String[] args) throws Exception {
        new ClientApp().run();
    }

    private void run() throws Exception {
        logger.info("Starting...");
        final NamedCache items = CacheFactory.getCache("Items");

        items.addIndex(extractor, false, null);

        logger.info("Clearing...");
        items.clear();

        logger.info("Generating...");
        final Map<Object, Item> map = generate(1_000);
        final List<Object> keys = new ArrayList<>(map.keySet());
        logger.info("Filling...");
        items.putAll(map);
        logger.info("Cache size is {} items", items.size());
        final int nThreads = 4;
        final int nIterations = 10;
        final ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        try {
            for (int p = 1; p <= nIterations; p++) {
                logger.info("Iteration #{}", p);
                final List<Future> futures = new ArrayList<>();
                for (int k = 0; k < nThreads; k++) {
                    futures.add(
                            pool.submit(() -> increment(items, keys))
                    );
                }
                logger.info("Waiting for {} futures", futures.size());
                for (Future future : futures) {
                    future.get();
                }
            }
            final Map<Object, Item> result = items.getAll(keys);
            final int totalSubItems = result.values()
                    .stream()
                    .filter(v -> v.getPayload() != null)
                    .mapToInt(v -> v.getPayload().size())
                    .sum();
            final Map m = items.getAll(Collections.singletonList(new Long(123456L)));
//            final long sum = result.values()
//                    .stream()
//                    .mapToLong(Item::getPayload)
//                    .sum();
            logger.info("sum={}, expected={}", totalSubItems, keys.size() * nThreads * nIterations);
        } finally {
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);
        }
        logger.info("Done!");
    }

    private void increment(NamedCache cache, Collection<?> keys) {
        final List<Object> toProcess = new ArrayList<>(keys);
        while (!toProcess.isEmpty()) {
            final Map<Object, Item> items = cache.getAll(toProcess);
            logger.info("Got {} items...", items.size());
            final Map<Object, Item> updated = items.entrySet()
                    .stream()
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> {
                                        final Item item = new Item(e.getValue().getId(), e.getValue().getPayload(), e.getValue().getVersion());
                                        List<Long> p = item.getPayload();
                                        if (p == null) {
                                            p = new ArrayList<>();
                                        }
                                        p.add(System.currentTimeMillis());
                                        item.getPayload(p);
                                        return item;
                                    }
                            )
                    );
            for (Item item : items.values()) {
                List<Long> p = item.getPayload();
                if (p == null) {
                    p = new ArrayList<>();
                }
                p.add(System.currentTimeMillis());
                item.getPayload(p);
            }
            final List<Object> copy = new ArrayList<>(toProcess);
            toProcess.clear();
            final Map<Object, Item> result = cache.invokeAll(
                    copy,
                    new VersionedPutAll<>(updated, true, true)
            );
            if (!result.isEmpty()) {
                logger.info("Failed to update {} items", result.size());
            }
            toProcess.addAll(result.keySet());
        }
    }

    private Map<Object, Item> generate(int count) {
        final Map<Object, Item> items = new HashMap<>();
        for (int i = 0; i < count; i++) {
            items.put(
                    sequence.incrementAndGet(),
                    new Item("key#" + i, null)
            );
        }
        return items;
    }
}
