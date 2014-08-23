package org.uze.jdbc;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;

/**
 * Created by Uze on 18.08.2014.
 */
public class StatementHelper {

    /**
     * Builds SQL select statement with IN clause (complex if key consists of more than one column)
     *
     * @return SQL select statement
     */
    public static String buildSelectStatement(TableMetadata metadata, int keyCount) {
        Objects.requireNonNull(metadata);
        Preconditions.checkArgument(keyCount > 0);

        final List<String> keyColumns = metadata.getKeyColumns();
        final boolean complexFlag = keyColumns.size() > 1;
        final StringBuilder sb = new StringBuilder();

        sb.append("SELECT\n");

        int count = 0;
        for (String columnName : metadata.getColumns()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(columnName);
            count++;
        }

        sb.append("\nFROM ").append(metadata.getTableName()).append("\nWHERE ");

        if (complexFlag) {
            sb.append('(');
        }

        count = 0;
        for (String columnName : keyColumns) {
            if (count > 0) {
                sb.append(',');
            }
            sb.append(columnName);
            count++;
        }

        if (complexFlag) {
            sb.append(')');
        }

        sb.append(" IN (");

        if (complexFlag) {
            int pos = sb.length();
            sb.append('?');
            for (int i = keyColumns.size(); i > 1; i--) {
                sb.append(",?");
            }
            final String keyParams = sb.substring(pos);
            sb.setLength(pos);

            sb.append('(').append(keyParams).append(')');
            for (int i = 1; i < keyCount; i++) {
                sb.append(",(").append(keyParams).append(')');
            }
        } else {
            sb.append('?');
            for (int i = 1; i < keyCount; i++) {
                sb.append(",?");
            }
        }

        sb.append(')');

        return sb.toString();
    }

    public static String buildInsertStatement(TableMetadata metadata) {
        Objects.requireNonNull(metadata);

        final StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ").append(metadata.getTableName()).append('(');

        int count = 0;
        for (String columnName : metadata.getColumns()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(columnName);
            count++;
        }

        sb.append(")\nSELECT ?");
        for (int i = 1; i < count; i++) {
            sb.append(",?");
        }

        sb.append("\nFROM DUAL\nWHERE NOT EXISTS (SELECT NULL FROM ")
            .append(metadata.getTableName());

        appendWhereClause(metadata, sb);

        sb.append(')');

        return sb.toString();
    }

    public static String buildUpdateStatement(TableMetadata metadata) {
        Objects.requireNonNull(metadata);

        final StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ").append(metadata.getTableName()).append(" SET ");

        int count = 0;
        for (String columnName : metadata.getColumns()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(columnName).append("=?");
            count++;
        }

        appendWhereClause(metadata, sb);

        return sb.toString();
    }

    private static void appendWhereClause(TableMetadata metadata, StringBuilder sb) {
        Objects.requireNonNull(metadata);

        final List<String> keyColumns = metadata.getKeyColumns();

        sb.append("\nWHERE ");

        int count = 0;
        for (String name : keyColumns) {
            if (count > 0) {
                sb.append("\n  AND ");
            }
            sb.append(name).append("=?");
            count++;
        }
    }
}
