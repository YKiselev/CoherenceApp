package org.uze.coherence.stores;

import com.tangosol.net.cache.CacheStore;
import org.uze.coherence.model.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class InMemoryStore implements CacheStore<Object, Item> {

    private final Map<Object, Item> items = new ConcurrentHashMap<>();

    @Override
    public void store(Object o, Item item) {
        items.put(o, item);
    }

    @Override
    public void storeAll(Map<?, ? extends Item> map) {
        items.putAll(map);
    }

    @Override
    public void erase(Object o) {
        items.remove(o);
    }

    @Override
    public void eraseAll(Collection<?> collection) {
        collection.forEach(items::remove);
    }

    @Override
    public Item load(Object o) {
        return items.get(o);
    }

    @Override
    public Map<Object, Item> loadAll(Collection<?> collection) {
        if (collection.contains(123456L)) {
            final Map<Object, Item> m = new HashMap<>();
            m.put(222L, new Item("T000001", Collections.emptyList(), 999));
            m.put(333L, new Item("T000002", Collections.emptyList(), 888));
            return m;
        }
        return items.entrySet()
                .stream()
                .filter(e -> collection.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
