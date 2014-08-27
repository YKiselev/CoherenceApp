package org.uze.stores;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.tangosol.io.ReadBuffer;
import com.tangosol.io.Serializer;
import com.tangosol.io.WrapperBufferOutput;
import com.tangosol.io.WriteBuffer;
import com.tangosol.io.pof.*;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofValue;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.BinaryWriteBuffer;
import com.tangosol.util.ExternalizableHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.uze.jdbc.StatementBuilder;
import org.uze.jdbc.TableMetadata;
import org.uze.pof.CounterpartPO;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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

            System.out.println("Select chunk: " + chunk);
            jdbcTemplate.query(sql, selectChunk, selectChunk);

            left -= chunk;
            offset += chunk;
        }
    }

    private void storeBatch(List<BinaryEntry> entries) {
        final BinaryEntry entry = entries.get(0);

        Binary key = entry.getBinaryKey();
        Binary value = entry.getBinaryValue();

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
        private final Map<Binary, BinaryEntry> key2entryMap = new HashMap<>();
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
                    key2entryMap.put(createKey(key, pofContext), entry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

//                if (ExternalizableHelper.isIntDecorated(key)) {
//                    int deco = ExternalizableHelper.extractIntDecoration(key);
//                    System.out.println("Key decorated with " + deco);
//                }

                final PofValue pofValue = PofValueParser.parse(key, (PofContext) entry.getSerializer());
                int tid = pofValue.getTypeId();

                final List<String> keyColumns = metadata.getKeyColumnNames();
                final int keySize = keyColumns.size();
                if (keySize == 1) {
                    final TableMetadata.Column column = metadata.getColumn(keyColumns.get(0));
                    ps.setObject(i + 1, pofValue.getValue(column.getClazz()), column.getSqlType());
                } else {
                    for (int k = 0; k < keySize; k++) {
                        final TableMetadata.Column column = metadata.getColumn(keyColumns.get(k));
                        final PofValue value = pofValue.getChild(k);

                        ps.setObject(i + k + 1, value.getValue(column.getClazz()), column.getSqlType());
                    }
                }
            }
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            final Binary key;
            try {
                key = createKey(rs);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            final Binary value;
            try {
                value = createValue(rs);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //System.out.println("Forged binary: " + value);

            final BinaryEntry entry = key2entryMap.get(key);
            Objects.requireNonNull(entry);
            //entry.setValue(rs.getString("NAME"));
            //Binary b = entry.getBinaryValue();
            entry.updateBinaryValue(value);
            //Test1.parse(b, pofContext);

            System.out.println("New row!");
        }

        private Binary createValue(ResultSet rs) throws IOException, SQLException {
            final BinaryWriteBuffer b = new BinaryWriteBuffer(200);
            final WriteBuffer.BufferOutput bo = b.getBufferOutput();
            final PofWriter writer = new PofBufferWriter.UserTypeWriter(bo, pofContext, 1002, -1);

            bo.writeByte(21);

            int index = 0;
            for (String name : metadata.getColumnNames()) {
                final TableMetadata.Column column = metadata.getColumn(name);
                if (column.isKeyOnly()) {
                    continue;
                }
                writer.writeObject(index, rs.getObject(name));
                index++;
            }

            writer.writeRemainder(null);

            return b.toBinary();
        }

//        private Binary createValue(ResultSet rs) throws IOException, SQLException {
//            final ByteArrayOutputStream os = new ByteArrayOutputStream();
//            final DataOutputStream dos = new DataOutputStream(os);
//            final WrapperBufferOutput wbo = new WrapperBufferOutput(dos);
//            final PofWriter writer = new PofBufferWriter.UserTypeWriter(wbo, pofContext, 1002, -1);
//
//            dos.writeByte(7);
//            writer.setVersionId(0);
//            int index = 0;
//            for (String name : metadata.getColumnNames()) {
//                final TableMetadata.Column column = metadata.getColumn(name);
//                if (column.isKeyOnly()) {
//                    continue;
//                }
//                writer.writeObject(index, rs.getObject(name));
//                index++;
//            }
//            writer.writeRemainder(null);
//
//            return new Binary(os);
//        }

        private Binary createKey(ResultSet rs) throws IOException, SQLException {
            final BinaryWriteBuffer b = new BinaryWriteBuffer(200);
            final WriteBuffer.BufferOutput bo = b.getBufferOutput();
            final PofWriter writer = new PofBufferWriter(bo, pofContext);

            //bo.writeByte(21);

            writer.writeObject(0, rs.getLong("ID"));
            //writer.writeString(1, cp.getName());

            //writer.writeRemainder(null);

            return b.toBinary();
        }

//        private Binary createKey(ResultSet rs) throws IOException, SQLException {
//            final ByteArrayOutputStream os = new ByteArrayOutputStream();
//            final DataOutputStream dos = new DataOutputStream(os);
//            final PofWriter writer = new PofBufferWriter(new WrapperBufferOutput(dos), pofContext);
//
//            //pofContext.serialize(new WrapperBufferOutput(dos), rs.getLong("ID"));
//
//            writer.writeObject(0, rs.getLong("ID"));
//            //writer.writeRemainder(null);
//
//            return new Binary(os);
//        }

        private Binary createKey(Binary key, PofContext pofContext) throws IOException {
//            final ByteArrayOutputStream os = new ByteArrayOutputStream();
//            final DataOutputStream dos = new DataOutputStream(os);
//            final PofWriter writer = new PofBufferWriter(new WrapperBufferOutput(dos), pofContext);

            final BinaryWriteBuffer b = new BinaryWriteBuffer(200);
            final WriteBuffer.BufferOutput bo = b.getBufferOutput();
            final PofWriter writer = new PofBufferWriter(bo, pofContext);

            final PofValue pofValue = PofValueParser.parse(key, pofContext);
            if (pofValue instanceof SimplePofValue) {
                writer.writeObject(0, pofValue.getValue());
            } else {
                final List<String> keyColumns = metadata.getKeyColumnNames();
                final int keySize = keyColumns.size();
                for (int k = 0; k < keySize; k++) {
                    final TableMetadata.Column column = metadata.getColumn(keyColumns.get(k));
                    final PofValue value = pofValue.getChild(k);

                    writer.writeObject(k, value.getValue(column.getClazz()));
                }
            }

            return b.toBinary();
        }
    }

    static class Test1 extends ExternalizableHelper implements PofConstants {

        public static PofValue parse(ReadBuffer buf, PofContext ctx) {
            ReadBuffer.BufferInput in = buf.getBufferInput();
            ReadBuffer bufDeco = null;
            long nDecoMask = 0L;
            int of;
            int cb;
            try {
                int nType = in.readUnsignedByte();
                switch (nType) {
                    case 13:
                        readInt(in);
                        in.readUnsignedByte();
                        of = in.getOffset();
                        cb = buf.length() - of;
                        break;
                    case 18:
                    case 19:
                        nDecoMask = nType == 18 ? in.readByte() : in.readPackedLong();
                        if ((nDecoMask & 1L) == 0L) {
                            throw new EOFException("Decorated binary is missing a value");
                        }

                        cb = readInt(in);
                        of = in.getOffset();

                        int ofDeco = of + cb;
                        bufDeco = buf.getReadBuffer(ofDeco, buf.length() - ofDeco);

                        buf = buf.getReadBuffer(of, cb);
                    case 21:
                        of = 1;
                        cb = buf.length() - 1;
                        break;
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 20:
                    default:
                        of = 0;
                        cb = buf.length();
                }
            } catch (IOException e) {
                throw ensureRuntimeException(e);
            }

//            AbstractPofValue valueRoot = (AbstractPofValue)parseValue(null, buf.getReadBuffer(of, cb), ctx, of);
//
//            valueRoot.setOriginalBuffer(buf);
//            valueRoot.setDecorations(nDecoMask, bufDeco);

            return null;
        }

    }
}
