package org.uze.stores;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofValue;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.uze.jdbc.*;
import org.uze.pof.BinaryHelper;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Uze on 19.08.2014.
 */
public class BaseJdbcBinaryEntryStore extends AbstractBinaryEntryStore {

    private int batchSize = 1000;
    private JdbcTemplate jdbcTemplate;
    private TableMetadata tableMetadata;
    private StatementBuilder statementBuilder;
    private final Map<Integer, String> selectStatements = new HashMap<>();

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TableMetadata getTableMetadata() {
        return tableMetadata;
    }

    public void setTableMetadata(TableMetadata tableMetadata) {
        this.tableMetadata = tableMetadata;
    }

    public StatementBuilder getStatementBuilder() {
        return statementBuilder;
    }

    public void setStatementBuilder(StatementBuilder statementBuilder) {
        this.statementBuilder = statementBuilder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadAll(Set set) {
        for (List<BinaryEntry> batch : Iterables.partition((Set<BinaryEntry>) set, batchSize)) {
            loadBatch(batch);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void storeAll(Set set) {
        for (List<BinaryEntry> batch : Iterables.partition((Set<BinaryEntry>) set, batchSize)) {
            storeBatch(batch);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void eraseAll(Set set) {
        for (List<BinaryEntry> batch : Iterables.partition((Set<BinaryEntry>) set, batchSize)) {
            eraseBatch(batch);
        }
    }

    private void loadBatch(List<BinaryEntry> entries) {
        int chunk = batchSize;
        int left = entries.size();
        int offset = 0;
        while (left > 0) {
            while (left < chunk) {
                chunk = chunk / 2 - 1;
            }
            if (chunk < 1) {
                chunk = 1;
            }

            final String sql = getSelectStatement(chunk);
            final SelectChunk selectChunk = new SelectChunk(tableMetadata, entries.subList(offset, offset + chunk));

            jdbcTemplate.query(sql, selectChunk, selectChunk);

            left -= chunk;
            offset += chunk;
        }
    }

    private void storeBatch(List<BinaryEntry> entries) {
        final BinaryEntry entry = entries.get(0);

        Binary key = entry.getBinaryKey();
        Binary value = entry.getBinaryValue();

        jdbcTemplate.batchUpdate("", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

            }

            @Override
            public int getBatchSize() {
                return 0;
            }
        });
    }

    private void eraseBatch(List<BinaryEntry> entries) {

    }

    private String getSelectStatement(int keyCount) {
        String result = selectStatements.get(keyCount);
        if (result == null) {
            result = statementBuilder.buildSelectStatement(tableMetadata, keyCount);
            selectStatements.put(keyCount, result);
        }
        return result;
    }

    static class SelectChunk implements PreparedStatementSetter, RowCallbackHandler {

        private final TableMetadata metadata;
        private final List<BinaryEntry> entries;
        private final Map<Object, BinaryEntry> entryIndexMap = new HashMap<>();
        private PofContext pofContext;

        SelectChunk(TableMetadata metadata, List<BinaryEntry> entries) {
            Objects.requireNonNull(entries);
            Preconditions.checkArgument(!entries.isEmpty());

            this.metadata = metadata;
            this.entries = entries;
            this.pofContext = (PofContext) entries.get(0).getSerializer();
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
            for (int i = 0; i < entries.size(); i++) {
                final BinaryEntry entry = entries.get(i);
                final Binary key = entry.getBinaryKey();

                try {
                    entryIndexMap.put(createKey(key), entry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                final PofValue pofValue = PofValueParser.parse(key, pofContext);
                final List<String> keyColumns = metadata.getKey().getNames();
                final int keySize = keyColumns.size();
                if (keySize == 1) {
                    final Column column = metadata.getColumn(keyColumns.get(0));
                    ps.setObject(i + 1, pofValue.getValue(column.getClazz()), column.getSqlType());
                } else {
                    for (int k = 0; k < keySize; k++) {
                        final Column column = metadata.getColumn(keyColumns.get(k));
                        final PofValue value = pofValue.getChild(k);

                        ps.setObject(i + k + 1, value.getValue(column.getClazz()), column.getSqlType());
                    }
                }
            }
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            final Object key;
            try {
                key = createKey(rs);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            final Binary value;
            try {
                value = BinaryHelper.toBinary(new ResultSetValueExtractor(metadata.getValue(), rs), pofContext);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final BinaryEntry entry = entryIndexMap.get(key);
            Objects.requireNonNull(entry);

            entry.updateBinaryValue(value);
        }

        private Object createKey(ResultSet rs) throws IOException, SQLException {
            final UserTypeColumns key = metadata.getKey();
            if (key.getSize() == 1) {
                final String name = key.getName(0);
                final Column column = metadata.getColumn(name);
                final int columnIndex = rs.findColumn(name);
                return ResultSetHelper.getValue(rs, columnIndex, column.getClazz());
            }

            return BinaryHelper.toBinary(new ResultSetValueExtractor(key, rs), pofContext);
        }

        private Object createKey(Binary key) throws IOException {
            final PofValue pofValue = PofValueParser.parse(key, pofContext);

            if (pofValue instanceof SimplePofValue) {
                return pofValue.getValue();
            }

            return BinaryHelper.toBinary(new UserTypeValueExtractor(metadata.getKey(), pofValue), pofContext);
        }
    }

    static class UpdateChunk implements BatchPreparedStatementSetter {

        private final TableMetadata metadata;
        private final int batchSize;
        private final List<BinaryEntry> entries;

        UpdateChunk(TableMetadata metadata, int batchSize, List<BinaryEntry> entries) {
            this.metadata = metadata;
            this.batchSize = batchSize;
            this.entries = entries;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            final BinaryEntry entry = entries.get(i);
            final PofValue pofValue = PofValueParser.parse(entry.getBinaryKey(), (PofContext) entry.getSerializer());
            final List<String> keyColumns = metadata.getKey().getNames();
            final int keySize = keyColumns.size();
            if (keySize == 1) {
                final Column column = metadata.getColumn(keyColumns.get(0));
                ps.setObject(i + 1, pofValue.getValue(column.getClazz()), column.getSqlType());
            } else {
                for (int k = 0; k < keySize; k++) {
                    final Column column = metadata.getColumn(keyColumns.get(k));
                    final PofValue value = pofValue.getChild(k);

                    ps.setObject(i + k + 1, value.getValue(column.getClazz()), column.getSqlType());
                }
            }
        }

        @Override
        public int getBatchSize() {
            return batchSize;
        }
    }
}
