package org.uze.jdbc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Objects;

/**
 * Created by Uze on 17.08.2014.
 */
public class TableMetadata {

    private final String tableName;
    private final ImmutableList<String> keyColumns;
    private final ImmutableList<String> allColumns;
    private final ImmutableMap<String, Column> columns;

    public String getTableName() {
        return tableName;
    }

    public List<String> getKeyColumns() {
        return keyColumns;
    }

    public List<String> getColumns() {
        return allColumns;
    }

    public Column getColumn(String name) {
        return columns.get(name);
    }

    TableMetadata(String tableName, Iterable<TableMetadataBuilder.BuilderColumn> columns, Iterable<String> keyColumns, Iterable<String> bodyColumns) {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(columns);
        Objects.requireNonNull(keyColumns);
        Objects.requireNonNull(bodyColumns);

        final ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
        final ImmutableMap.Builder<String, Column> builder = ImmutableMap.builder();
        for (TableMetadataBuilder.BuilderColumn c : columns) {
            listBuilder.add(c.getName());
            builder.put(c.getName(), new Column(c.getSqlType(), c.getClazz()));
        }

        this.tableName = tableName;
        this.columns = builder.build();
        this.allColumns = listBuilder.build();
        this.keyColumns = ImmutableList.copyOf(keyColumns);
        this.bodyColumns = ImmutableList.copyOf(bodyColumns);
    }

    static class Column {

        private final int sqlType;
        private final Class clazz;

        public int getSqlType() {
            return sqlType;
        }

        public Class getClazz() {
            return clazz;
        }

        Column(int sqlType, Class clazz) {
            this.sqlType = sqlType;
            this.clazz = clazz;
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
