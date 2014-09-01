package org.uze.stores;

import com.google.common.base.Preconditions;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofValue;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.uze.jdbc.Column;
import org.uze.jdbc.ResultSetHelper;
import org.uze.jdbc.TableMetadata;
import org.uze.jdbc.UserTypeColumns;
import org.uze.pof.BinaryHelper;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Uze on 01.09.2014.
 */
public class SelectStatementContext implements PreparedStatementSetter, ResultSetExtractor {

    private final TableMetadata metadata;
    private final List<BinaryEntry> entries;
    private final Map<Object, BinaryEntry> entryIndexMap;
    private PofContext pofContext;

    public PofContext getPofContext() {
        return pofContext;
    }

    public TableMetadata getMetadata() {
        return metadata;
    }

    public SelectStatementContext(TableMetadata metadata, List<BinaryEntry> entries) {
        Objects.requireNonNull(entries);
        Preconditions.checkArgument(!entries.isEmpty());

        this.metadata = metadata;
        this.entries = entries;
        this.pofContext = (PofContext) entries.get(0).getSerializer();
        this.entryIndexMap = new HashMap<>(entries.size(), 1f);
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

            setKeyValue(ps, key, i);
        }
    }

    protected void setKeyValue(PreparedStatement ps, Binary key, int index) throws SQLException {
        final PofValue pofValue = PofValueParser.parse(key, getPofContext());
        final UserTypeColumns keyColumns = getMetadata().getKey();
        final int keySize = keyColumns.getSize();

        if (keySize == 1) {
            final Column column = keyColumns.getColumn(0);

            ps.setObject(index + 1, pofValue.getValue(column.getClazz()), column.getSqlType());
        } else {
            for (int k = 0; k < keySize; k++) {
                final Column column = keyColumns.getColumn(k);
                final PofValue value = pofValue.getChild(k);

                ps.setObject(index + k + 1, value.getValue(column.getClazz()), column.getSqlType());
            }
        }
    }

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
        final ResultSetValueExtractor valueExtractor = new ResultSetValueExtractor(metadata.getValue(), rs);
        try {
            while (rs.next()) {
                final Object key = createKey(rs);
                final Binary value = BinaryHelper.toBinary(valueExtractor, getPofContext());
                final BinaryEntry entry = entryIndexMap.get(key);

                Objects.requireNonNull(entry);

                entry.updateBinaryValue(value);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private Object createKey(ResultSet rs) throws IOException, SQLException {
        final UserTypeColumns key = metadata.getKey();
        if (key.getSize() == 1) {
            final String name = key.getName(0);
            final Column column = metadata.getColumn(name);
            final int columnIndex = rs.findColumn(name);
            return ResultSetHelper.getValue(rs, columnIndex, column.getClazz());
        }

        return BinaryHelper.toBinary(new ResultSetValueExtractor(key, rs), getPofContext());
    }

    private Object createKey(Binary key) throws IOException {
        final PofValue pofValue = PofValueParser.parse(key, getPofContext());

        if (pofValue instanceof SimplePofValue) {
            return pofValue.getValue();
        }

        return BinaryHelper.toBinary(new UserTypeValueExtractor(metadata.getKey(), pofValue), getPofContext());
    }
}
