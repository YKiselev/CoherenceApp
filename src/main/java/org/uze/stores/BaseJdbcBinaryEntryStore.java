package org.uze.stores;

import com.google.common.collect.Iterables;
import com.tangosol.io.ByteArrayReadBuffer;
import com.tangosol.io.ByteArrayWriteBuffer;
import com.tangosol.io.ReadBuffer;
import com.tangosol.io.Serializer;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofHandler;
import com.tangosol.io.pof.PofParser;
import com.tangosol.io.pof.RawQuad;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofValue;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.BinaryWriteBuffer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.uze.jdbc.StatementBuilder;
import org.uze.jdbc.TableMetadata;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

            jdbcTemplate.query(sql, new SelectSetter(tableMetadata, entries, offset, chunk), new SelectRowHandler());

            left -= chunk;
            offset += chunk;
        }
    }

    private void storeBatch(List<BinaryEntry> entries) {

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

    static class SelectSetter implements PreparedStatementSetter {

        private final TableMetadata metadata;
        private final List<BinaryEntry> entries;
        private final int offset;
        private final int chunk;

        SelectSetter(TableMetadata metadata, List<BinaryEntry> entries, int offset, int chunk) {
            this.metadata = metadata;
            this.entries = entries;
            this.offset = offset;
            this.chunk = chunk;
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
            final List<String> keyColumns = metadata.getKeyColumns();
            final TableMetadata.Column[] columns = new TableMetadata.Column[keyColumns.size()];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = metadata.getColumn(keyColumns.get(i));
            }

            for (int i = 0; i < chunk; i++) {
                final BinaryEntry entry = entries.get(offset + i);
                final Binary key = entry.getBinaryKey();
                final PofValue pofValue = PofValueParser.parse(key, (PofContext) entry.getSerializer());

                for (int k = 0; k < keyColumns.size(); k++) {
                    final TableMetadata.Column column = columns[k];
                    final PofValue value = pofValue instanceof SimplePofValue ? pofValue : pofValue.getChild(k);

                    ps.setObject(i + k + 1, value.getValue(column.getClazz()), column.getSqlType());
                }
            }
        }
    }

    static class SelectRowHandler implements RowCallbackHandler {

        @Override
        public void processRow(ResultSet rs) throws SQLException {

        }
    }
}
