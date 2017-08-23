package org.uze.coherence;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.InFilter;
import com.tangosol.util.processor.ConditionalRemove;
import org.uze.coherence.model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ClientApp {

    private final AtomicLong sequence = new AtomicLong(System.nanoTime());

    public static void main(String[] args) throws Exception {
        new ClientApp().run();
    }

    private void run() throws Exception {
        final NamedCache items = CacheFactory.getCache("Items");

        items.addIndex(new ReflectionExtractor("id"), false, null);

        System.out.println("Clearing...");
        items.clear();

        System.out.println("Filling...");
        // Generate and fill
        final List<String> keys = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            final Map<String, Item> map = generate(10_000);
            items.putAll(map);
            keys.addAll(map.keySet());
        }
        System.out.println("Reading...");
        // Read
        final ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            final List<Future<Boolean>> futures = new ArrayList<>();
            for (String key : keys) {
                futures.add(
                        pool.submit(() -> call(items, Collections.singleton(key)))
                );
            }
            int success = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    success++;
                }
            }
            System.out.println("Cache size: " + items.size() + ", " + success + " of " + futures.size() + " was successful");
        } finally {
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private Map<String, Item> generate(int count) {
        final Map<String, Item> items = new HashMap<>();
        for (int i = 0; i < count; i++) {
            final String key = Long.toString(sequence.incrementAndGet());
            items.put(key, new Item(key, "item#" + i));
        }
        return items;
    }

    private boolean call(NamedCache cache, Set<String> keys) throws Exception {
        final InFilter filter = new InFilter(new ReflectionExtractor("id"), keys);
        final Set<Map.Entry<String, Item>> result = cache.entrySet(filter);
        final boolean success = result.size() == keys.size();
        if (!success) {
            System.out.println("Expected " + keys.size() + ", got " + result.size());
        }
        cache.invokeAll(filter, new ConditionalRemove(AlwaysFilter.INSTANCE));
        return success;
    }

}
