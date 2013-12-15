package org.uze.caches;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.uze.client.Counterpart;
import org.uze.client.Trade;
import org.uze.pof.CounterpartPO;
import org.uze.pof.TradePO;

import java.util.Set;

/**
 * Created by Юрий on 10.12.13.
 */
public abstract class CacheAccess<K, V, PO> {

    public static final CacheAccess<Long, Counterpart, CounterpartPO> COUNTERPARTS = new CounterpartCache();
    public static final CacheAccess<Long, Trade, TradePO> TRADES = new TradeCache();

    private static final CacheAccess[] KNOWN_CACHES = new CacheAccess[]{
            COUNTERPARTS, TRADES
    };

    private final NamedCache cache;

    public static CacheAccess<?, ?, ?> getCache(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null!");
        }
        for (CacheAccess<?, ?, ?> cache : KNOWN_CACHES) {
            if (name.equals(cache.cache.getCacheName())) {
                return cache;
            }
        }
        throw new IllegalArgumentException("Unknown caches: " + name);
    }

    public CacheAccess(String cacheName) {
        this.cache = CacheFactory.getCache(cacheName);
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        return fromObject((PO) cache.get(key));
    }

    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        return fromObject((PO) cache.put(key, toObject(value)));
    }

    @SuppressWarnings("unchecked")
    public V remove(K key) {
        return fromObject((PO) cache.remove(key));
    }

    public Set keySet() {
        return cache.keySet();
    }

    protected abstract V fromObject(PO src);

    protected abstract PO toObject(V src);
}
