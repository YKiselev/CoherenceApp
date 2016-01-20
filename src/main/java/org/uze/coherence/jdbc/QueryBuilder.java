package org.uze.coherence.jdbc;

import org.uze.coherence.stores.JdbcCacheStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Uze on 30.12.13.
 */
public class QueryBuilder {

    public static final String PARAM_PRIMARY_KEY_BATCH = "primaryKeyBatch";

    private final String tableName;
    private final JdbcCacheStore.ColumnInfo[] columnInfos;
    private final List<JdbcCacheStore.ColumnInfo> primaryKey;

    public QueryBuilder(String tableName, JdbcCacheStore.ColumnInfo[] columnInfos) {
        if (tableName == null) {
            throw new IllegalArgumentException("tableName");
        }
        if (columnInfos == null || columnInfos.length == 0) {
            throw new IllegalArgumentException("columnInfos");
        }

        this.tableName = tableName;
        this.columnInfos = columnInfos;

        this.primaryKey = new ArrayList<JdbcCacheStore.ColumnInfo>();
        for (JdbcCacheStore.ColumnInfo info : columnInfos) {
            if (info.isPk()) {
                this.primaryKey.add(info);
            }
        }
        if (primaryKey.isEmpty()) {
            throw new IllegalArgumentException("No primary key columns for table \"" + tableName + "\"");
        }
    }

    /**
     * Builds insert sql statement
     * e.g. INSERT INTO Table1 (Id, Name) VALUES (:id, :name)
     *
     * @return
     */
    public String buildInsertQuery() {
        final StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ").append(tableName).append(" (");

        int columns = 0;
        for (JdbcCacheStore.ColumnInfo info : columnInfos) {
            if (!info.isInsertable()) {
                continue;
            }

            if (columns > 0) {
                sb.append(',');
            }
            sb.append(info.getTableColumnName());
            columns++;
        }

        sb.append(") VALUES (");

        int vars = 0;
        for (JdbcCacheStore.ColumnInfo info : columnInfos) {
            if (!info.isInsertable()) {
                continue;
            }

            if (vars > 0) {
                sb.append(',');
            }

            sb.append(':').append(info.getBeanFieldName());
            vars++;
        }

        sb.append(")");

        if (columns != vars) {
            throw new RuntimeException("Columns (" + columns + ") != Variables (" + vars + ")");
        }

        return sb.toString();
    }

    /**
     * Builds update sql statement
     * e.g. UPDATE Table1 SET Id = :id, Name=:name
     *
     * @return
     */
    public String buildUpdateQuery() {
        final StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ").append(tableName).append(" SET ");

        int columns = 0;
        for (JdbcCacheStore.ColumnInfo info : columnInfos) {
            if (!info.isUpdateable()) {
                continue;
            }

            if (columns > 0) {
                sb.append(", ");
            }
            sb.append(info.getTableColumnName()).append(" = :").append(info.getBeanFieldName());
            columns++;
        }

        sb.append(" WHERE ");

        int keyFields = 0;
        for (JdbcCacheStore.ColumnInfo info : primaryKey) {
            if (keyFields > 0) {
                sb.append(" AND ");
            }
            sb.append(info.getTableColumnName()).append(" = :").append(info.getBeanFieldName());
            keyFields++;
        }

        return sb.toString();
    }

    /**
     * Builds delete SQL statenemt for batches (no named parameters)
     * e.g. DELETE FROM Table1 WHERE C1 = ? AND C2 = ?
     *
     * @return delete query
     */
    public String buildDeleteQuery() {
        final StringBuilder sb = new StringBuilder();

        sb.append("DELETE FROM ").append(tableName).append(" WHERE ");

        int columns = 0;
        for (JdbcCacheStore.ColumnInfo info : primaryKey) {
            if (columns > 0) {
                sb.append(" AND ");
            }
            sb.append(info.getTableColumnName()).append(" = ?");
            columns++;
        }

        return sb.toString();
    }

    /**
     * Builds select sql query for querying many items i.e. using IN clause (complex if primary key is complex)
     *
     * @return
     */
    public String buildSelectQuery() {
        final boolean complexFlag = primaryKey.size() > 1;
        final StringBuilder sb = new StringBuilder();

        sb.append("SELECT\n");

        int columns = 0;
        for (JdbcCacheStore.ColumnInfo info : columnInfos) {
            if (columns > 0) {
                sb.append(",");
            }
            sb.append(info.getTableColumnName()).append("\n");
            columns++;
        }

        sb.append("\nFROM ").append(tableName).append("\nWHERE\n");

        if (complexFlag) {
            sb.append('(');
        }

        int keyFields = 0;
        for (JdbcCacheStore.ColumnInfo info : primaryKey) {
            if (keyFields > 0) {
                sb.append(',');
            }
            sb.append(info.getTableColumnName());
            keyFields++;
        }

        if (complexFlag) {
            sb.append(')');
        }
        sb.append(" IN (:").append(PARAM_PRIMARY_KEY_BATCH).append(')');

        return sb.toString();
    }

    /**
     * Builds select sql query for querying existing keys
     * e.g. SELECT Id FROM Table1 WHERE Id IN (:primaryKeyBatch)
     *
     * @return
     */
    public String buildSelectKeysQuery() {
        final boolean complexFlag = primaryKey.size() > 1;
        final StringBuilder sb = new StringBuilder();

        sb.append("SELECT\n");

        int columns = 0;
        for (JdbcCacheStore.ColumnInfo info : primaryKey) {
            if (columns > 0) {
                sb.append(",");
            }
            sb.append(info.getTableColumnName()).append("\n");
            columns++;
        }

        sb.append("\nFROM ").append(tableName).append("\nWHERE\n");

        if (complexFlag) {
            sb.append('(');
        }

        int keyFields = 0;
        for (JdbcCacheStore.ColumnInfo info : primaryKey) {
            if (keyFields > 0) {
                sb.append(',');
            }
            sb.append(info.getTableColumnName());
            keyFields++;
        }

        if (complexFlag) {
            sb.append(')');
        }

        sb.append(" IN (:").append(PARAM_PRIMARY_KEY_BATCH).append(')');

        return sb.toString();
    }
}
