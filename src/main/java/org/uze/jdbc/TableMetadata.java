package org.uze.jdbc;

import com.google.common.base.Function;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by Uze on 17.08.2014.
 */
public class TableMetadata {

    private final String tableName;
    private final ImmutableList<String> keyColumnNames;
    private final ImmutableMap<String, Column> columnMap;

    public String getTableName() {
        return tableName;
    }

    public List<String> getKeyColumnNames() {
        return keyColumnNames;
    }

    public ImmutableSet<String> getColumnNames() {
        return columnMap.keySet();
    }

    public ImmutableCollection<Column> getColumns() {
        return columnMap.values();
    }

    public Column getColumn(String name) {
        return columnMap.get(name);
    }

    TableMetadata(String tableName, Iterable<TableMetadataBuilder.BuilderColumn> columns, Iterable<String> keyColumnNames) {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(columns);
        Objects.requireNonNull(keyColumnNames);

        final ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
        final ImmutableMap.Builder<String, Column> builder = ImmutableMap.builder();
        for (TableMetadataBuilder.BuilderColumn c : columns) {
            listBuilder.add(c.getName());
            builder.put(c.getName(), new Column(c.getSqlType(), c.getClazz(), c.isKeyOnly()));
        }

        this.tableName = tableName;
        this.columnMap = builder.build();
        //this.columnNames = listBuilder.build();
        this.keyColumnNames = ImmutableList.copyOf(keyColumnNames);
//        this.keyColumns = Collections.unmodifiableList(Lists.transform(this.keyColumnNames, new Function<String, Column>() {
//            @Override
//            public Column apply(@Nullable String input) {
//                if (input == null) {
//                    return null;
//                }
//                return columnMap.get(input);
//            }
//        }));
    }

    public static class Column {

        private final int sqlType;
        private final Class clazz;
        private final boolean keyOnly;

        public int getSqlType() {
            return sqlType;
        }

        public Class getClazz() {
            return clazz;
        }

        public boolean isKeyOnly() {
            return keyOnly;
        }

        Column(int sqlType, Class clazz, boolean keyOnly) {
            this.sqlType = sqlType;
            this.clazz = clazz;
            this.keyOnly = keyOnly;
        }

        @Override
        public String toString() {
            return "Column{" +
                "sqlType=" + sqlType +
                ", clazz=" + clazz +
                '}';
        }
    }

}
