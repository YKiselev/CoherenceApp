package org.uze.stores;

import com.tangosol.net.cache.CacheStore;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchUpdateUtils;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.uze.jdbc.QueryBuilder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Uze on 10.12.13.
 */
public abstract class JdbcCacheStore<K, V> implements CacheStore {

    private final String tableName;
    private final DataSource dataSource;
    private final int maxBatchSize;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ParsedSql insertQuery;
    private final ParsedSql updateQuery;
    private final String deleteQuery;
    private final String selectQuery;
    private final String selectKeysQuery;
    private final PlatformTransactionManager txManager;

    protected abstract RowMapper<V> getRowMapper();

    protected abstract RowMapper<K> getKeyRowMapper();

    protected abstract K getKey(V value);

    protected abstract Object[] decomposeKey(K key);

    public JdbcCacheStore(String tableName, ColumnInfo[] columnInfos) {
        this.tableName = tableName;
        this.dataSource = null;// DataSourceFactory.getDataSource();
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.maxBatchSize = 100;

        final QueryBuilder queryBuilder = new QueryBuilder(tableName, columnInfos);

        this.insertQuery = NamedParameterUtils.parseSqlStatement(queryBuilder.buildInsertQuery());
        this.updateQuery = NamedParameterUtils.parseSqlStatement(queryBuilder.buildUpdateQuery());
        this.deleteQuery = queryBuilder.buildDeleteQuery();
        this.selectQuery = queryBuilder.buildSelectQuery();
        this.selectKeysQuery = queryBuilder.buildSelectKeysQuery();

        this.txManager = new DataSourceTransactionManager(dataSource);
    }

    private int getMaxBatchSize() {
        return maxBatchSize;
    }

    protected NamedParameterJdbcTemplate getTemplate() {
        return jdbcTemplate;
    }

    private TransactionStatus getTransaction() {
        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return txManager.getTransaction(dtd);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void store(Object key, Object value) {
        TransactionStatus ts = getTransaction();
        try {
            SqlParameterSource ps = new BeanPropertySqlParameterSource(value);
            final int updateCount = getTemplate().update(updateQuery.toString(), ps);
            if (updateCount != 1) {
                final int insertCount = getTemplate().update(insertQuery.toString(), ps);
                if (updateCount + insertCount != 1) {
                    throw new RuntimeException("Store operation failed for key " + key);
                }
            }
            txManager.commit(ts);
        } catch (RuntimeException ex) {
            txManager.rollback(ts);
            throw ex;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void storeAll(Map map) {
        TransactionStatus ts = getTransaction();
        try {
            final int batchSize = getMaxBatchSize();
            if (batchSize == 0 || batchSize >= map.size()) {
                storeBatch(map);
            } else {
                final Map<K, V> batch = new HashMap<K, V>(batchSize);
                while (!map.isEmpty()) {
                    final Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
                    while (it.hasNext() && batch.size() < batchSize) {
                        Map.Entry<K, V> entry = it.next();
                        batch.put(entry.getKey(), entry.getValue());
                        it.remove();
                    }

                    if (!batch.isEmpty()) {
                        storeBatch(batch);
                        batch.clear();
                    }
                }
            }
            txManager.commit(ts);
        } catch (RuntimeException ex) {
            txManager.rollback(ts);
            throw ex;
        }
    }

    private int batchUpdate(ParsedSql parsedSql, SqlParameterSource[] batchArgs) {
        if (batchArgs.length <= 0) {
            return 0;
        }
        final String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, batchArgs[0]);
        return getTemplate().getJdbcOperations().execute(sqlToUse, new BatchCallback(parsedSql, batchArgs));
    }

    private void storeBatch(Map<K, V> map) {
        final SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(map.values().toArray());
        final int updateCount = batchUpdate(updateQuery, batch);
        if (updateCount < batch.length) {
            Map<K, V> insertMap = map;
            if (updateCount > 0) {
                final MapSqlParameterSource paramSource = new MapSqlParameterSource();
                paramSource.addValue(QueryBuilder.PARAM_PRIMARY_KEY_BATCH, map.keySet());
                final List<K> existingKeys = getTemplate().query(selectKeysQuery, paramSource, getKeyRowMapper());
                insertMap = new HashMap<K, V>(map);

                // remove keys of existent rows
                insertMap.keySet().removeAll(existingKeys);
            }

            final SqlParameterSource[] insertBatch = SqlParameterSourceUtils.createBatch(insertMap.values().toArray());
            final int insertCount = batchUpdate(insertQuery, insertBatch);

            if (updateCount + insertCount != batch.length) {
                throw new RuntimeException("\"" + tableName + "\": Incomplete store: " + updateCount +
                        " update(s) + " + insertCount + " insert(s) != " + batch.length + " (total batch size)");
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void erase(Object key) {
        getTemplate().getJdbcOperations().update(deleteQuery, decomposeKey((K) key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void eraseAll(Collection collection) {
        TransactionStatus ts = getTransaction();
        try {
            final int batchSize = getMaxBatchSize();
            if (batchSize == 0 || batchSize >= collection.size()) {
                eraseBatch(collection);
            } else {
                final List<K> batch = new ArrayList<K>(batchSize);
                while (!collection.isEmpty()) {
                    final Iterator<K> it = collection.iterator();
                    while (it.hasNext() && batch.size() < batchSize) {
                        batch.add(it.next());
                        it.remove();
                    }

                    if (!batch.isEmpty()) {
                        eraseBatch(batch);
                        batch.clear();
                    }
                }
            }
            txManager.commit(ts);
        } catch (RuntimeException ex) {
            txManager.rollback(ts);
            throw ex;
        }
    }

    protected void eraseBatch(Collection<K> keys) {
        final List<Object[]> batchArgs = new ArrayList<Object[]>();
        for (K key : keys) {
            batchArgs.add(decomposeKey(key));
        }
        getTemplate().getJdbcOperations().batchUpdate(deleteQuery, batchArgs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object load(Object key) {
        Map<K, V> map = loadBatch(Collections.singletonList((K) key));
        return map.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map loadAll(Collection collection) {
        final Map<K, V> result = new HashMap<K, V>();
        final int batchSize = getMaxBatchSize();

        if (batchSize == 0 || batchSize >= collection.size()) {
            result.putAll(loadBatch(collection));
        } else {
            final List<K> batch = new ArrayList<K>(batchSize);
            while (!collection.isEmpty()) {
                final Iterator<K> it = collection.iterator();
                while (it.hasNext() && batch.size() < batchSize) {
                    batch.add(it.next());
                    it.remove();
                }

                if (!batch.isEmpty()) {
                    result.putAll(loadBatch(batch));
                    batch.clear();
                }
            }
        }

        return result;
    }

    protected Map<K, V> loadBatch(Collection<K> keys) {
        final MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue(QueryBuilder.PARAM_PRIMARY_KEY_BATCH, keys);
        final List<V> rows = getTemplate().query(selectQuery, paramSource, getRowMapper());
        final Map<K, V> result = new HashMap<K, V>(rows.size());
        for (V value : rows) {
            result.put(getKey(value), value);
        }
        return result;
    }

    /**
     * Column flags enum
     */
    public enum Flag {
        PK, INSERTABLE, UPDATEABLE
    }

    /**
     * ColumnInfo class
     */
    public static class ColumnInfo {

        private final String tableColumnName;
        private final String beanFieldName;
        private final EnumSet<Flag> flags;

        public String getTableColumnName() {
            return tableColumnName;
        }

        public String getBeanFieldName() {
            return beanFieldName;
        }

        public boolean isPk() {
            return flags.contains(Flag.PK);
        }

        public boolean isInsertable() {
            return flags.contains(Flag.INSERTABLE) || isPk();
        }

        public boolean isUpdateable() {
            return flags.contains(Flag.UPDATEABLE) && !isPk();
        }

        public ColumnInfo(String tableColumnName, String beanFieldName, Flag... flags) {
            this.tableColumnName = tableColumnName;
            this.beanFieldName = beanFieldName;
            this.flags = EnumSet.copyOf(Arrays.asList(flags));

            if (isPk() && !this.flags.contains(Flag.INSERTABLE)) {
                this.flags.add(Flag.INSERTABLE);
            }
        }

        public ColumnInfo(String tableColumnName, Flag... flags) {
            this(tableColumnName, Character.toLowerCase(tableColumnName.charAt(0)) + tableColumnName.substring(1), flags);
        }

        public ColumnInfo(String tableColumnName) {
            this(tableColumnName, Flag.INSERTABLE, Flag.UPDATEABLE);
        }
    }

    /**
     * Batch prepared statement callback
     */
    static class BatchCallback extends BatchUpdateUtils implements PreparedStatementCallback<Integer> {

        private final ParsedSql parsedSql;
        private final SqlParameterSource[] batchArgs;

        BatchCallback(ParsedSql parsedSql, SqlParameterSource[] batchArgs) {
            this.parsedSql = parsedSql;
            this.batchArgs = batchArgs;
        }

        private void setValues(PreparedStatement ps, int i) throws SQLException {
            Object[] values = NamedParameterUtils.buildValueArray(parsedSql, batchArgs[i], null);
            int[] columnTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, batchArgs[i]);
            setStatementParameters(values, ps, columnTypes);
        }

        @Override
        public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
            final int batchSize = batchArgs.length;
            int result = 0;
            if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
                for (int i = 0; i < batchSize; i++) {
                    setValues(ps, i);
                    ps.addBatch();
                }
                int[] batchResult = ps.executeBatch();
                for (int i = batchResult.length - 1; i >= 0; i--) {
                    int value = batchResult[i];
                    // workaround for Oracle driver not JDBC-compliant
                    if (value < 0) {
                        result = ps.getUpdateCount();
                        break;
                    }
                    result += value;
                }
            } else {
                for (int i = 0; i < batchSize; i++) {
                    setValues(ps, i);
                    result += ps.executeUpdate();
                }
            }

            return result;
        }
    }
}
