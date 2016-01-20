package org.uze.coherence.stores;

import com.google.common.collect.Iterables;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.uze.coherence.jdbc.*;

import java.sql.PreparedStatement;
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
            final SelectStatementContext ctx = statementBuilder.newSelectStatementContext(tableMetadata,
                entries.subList(offset, offset + chunk));

            jdbcTemplate.query(sql, ctx, ctx);

            left -= chunk;
            offset += chunk;
        }
    }

    private void storeBatch(List<BinaryEntry> entries) {
        for (BinaryEntry entry : entries) {
            final Binary key = entry.getBinaryKey();
            final Binary value = entry.getBinaryValue();

            // update

            // if not updated - insert
        }


        jdbcTemplate.update("", new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {

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

    static class UpdateChunk implements PreparedStatementSetter {

        private final TableMetadata metadata;
        private final List<BinaryEntry> entries;

        UpdateChunk(TableMetadata metadata, List<BinaryEntry> entries) {
            this.metadata = metadata;
            this.entries = entries;
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
//            final BinaryEntry entry = entries.get(i);
//            final PofValue pofValue = PofValueParser.parse(entry.getBinaryKey(), (PofContext) entry.getSerializer());
//            final List<String> keyColumns = metadata.getKey().getNames();
//            final int keySize = keyColumns.size();
//            if (keySize == 1) {
//                final Column column = metadata.getColumn(keyColumns.get(0));
//                ps.setObject(i + 1, pofValue.getValue(column.getClazz()), column.getSqlType());
//            } else {
//                for (int k = 0; k < keySize; k++) {
//                    final Column column = metadata.getColumn(keyColumns.get(k));
//                    final PofValue value = pofValue.getChild(k);
//
//                    ps.setObject(i + k + 1, value.getValue(column.getClazz()), column.getSqlType());
//                }
//            }
        }
    }
}
