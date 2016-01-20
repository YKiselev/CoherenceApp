package org.uze.coherence.stores;

import com.tangosol.net.cache.CacheStore;
import org.apache.commons.configuration.Configuration;
import org.uze.coherence.Config;

import java.util.Collection;
import java.util.Map;

/**
 * User: Uze
 * Date: 10.12.13
 * Time: 0:18
 */
public class MyStore implements CacheStore {

    private final CacheStore delegate;

    public MyStore(String cacheName) {
        System.out.println(getClass().getName() + ".<init>(" + cacheName + ") called");

        final Configuration store = Config.getInstance().subset("stores." + cacheName);
        final String className = store.getString("class");
        try {
            @SuppressWarnings("unchecked")
            Class<? extends CacheStore> storeClass = (Class<? extends CacheStore>) Class.forName(className);

            delegate = storeClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void store(Object key, Object value) {
        System.out.println("store(" + key + ", " + value + ")");
        delegate.store(key, value);
    }

    @Override
    public void storeAll(Map map) {
        System.out.println("storeAll(" + map.keySet() + ")");
        delegate.storeAll(map);
    }

    @Override
    public void erase(Object key) {
        System.out.println("erase(" + key + ")");
        delegate.erase(key);
    }

    @Override
    public void eraseAll(Collection collection) {
        System.out.println("eraseAll(" + collection + ")");
        delegate.eraseAll(collection);
    }

    @Override
    public Object load(Object key) {
        System.out.println("load(" + key + ")");
        return delegate.load(key);
    }

    @Override
    public Map loadAll(Collection collection) {
        System.out.println("loadAll(" + collection + ")");
        return delegate.loadAll(collection);
    }
}
