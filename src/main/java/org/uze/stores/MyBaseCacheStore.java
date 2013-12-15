package org.uze.stores;

import com.tangosol.net.cache.CacheStore;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.uze.coherence.Config;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by Uze on 10.12.13.
 */
public abstract class MyBaseCacheStore<K, V> implements CacheStore {

    private enum Holder {
        INSTANCE;

        private final DataSource dataSource;

        Holder() {
            try {
                // fuck Oracle
                Locale.setDefault(Locale.ENGLISH);

                BasicDataSource ds = new BasicDataSource();

                Configuration db = Config.getInstance().subset("database");

                ds.setDriverClassName(db.getString("driver"));
                ds.setUsername(db.getString("username"));
                ds.setPassword(db.getString("password"));
                ds.setUrl(db.getString("url"));

                ds.setMaxActive(5);
                ds.setMaxIdle(1);
                ds.setInitialSize(1);
                ds.setValidationQuery(db.getString("validationQuery"));

                this.dataSource = ds;
            } catch (Exception e) {
                throw new RuntimeException("Connection failed", e);
            }
        }
    }

    public MyBaseCacheStore() {

    }

    protected JdbcTemplate getTemplate() {
        return new JdbcTemplate(Holder.INSTANCE.dataSource);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void store(Object key, Object value) {
        int count = update((K) key, (V) value);
        if (count == 0) {
            insert((K) key, (V) value);
        }
    }

    @Override
    public void storeAll(Map map) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void erase(Object key) {
        delete((K) key);
    }

    @Override
    public void eraseAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object load(Object key) {
        return find((K) key);
    }

    @Override
    public Map loadAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    protected abstract void insert(K key, V value);

    protected abstract int update(K key, V value);

    protected abstract V find(K key);

    protected abstract void delete(K key);

    protected final int exec(String sql, Object... args) {
        return getTemplate().update(sql, args);
    }

    protected final <V> List<V> select(String sql, RowMapper<V> mapper, Object... args) {
        return getTemplate().query(sql, args, mapper);
    }

    protected final <V> V single(String sql, RowMapper<V> mapper, Object... args) {
        List<V> result = select(sql, mapper, args);
        if (result.size() != 1) {
            throw new RuntimeException("Not found: " + Arrays.asList(args));
        }
        return result.get(0);
    }

}
