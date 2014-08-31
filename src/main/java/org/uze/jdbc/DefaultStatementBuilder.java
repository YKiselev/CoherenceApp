package org.uze.jdbc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * Created by Uze on 18.08.2014.
 */
public class DefaultStatementBuilder implements StatementBuilder {

    /**
     * Builds SQL select statement with IN clause (complex if key consists of more than one column)
     *
     * @return SQL select statement
     */
    @Override
    public String buildSelectStatement(TableMetadata metadata, int keyCount) {
        Objects.requireNonNull(metadata);
        Preconditions.checkArgument(keyCount > 0);

        final StringBuilder sb = new StringBuilder();

        sb.append("SELECT\n");

        int count = 0;
        for (String columnName : metadata.getValue().getNames()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(columnName);
            count++;
        }

        sb.append("\nFROM ").append(metadata.getTableName());

        addWhereInClause(metadata, keyCount, sb);

        return sb.toString();
    }

    @Override
    public String buildInsertStatement(TableMetadata metadata) {
        Objects.requireNonNull(metadata);

        final StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ").append(metadata.getTableName()).append('(');

        int count = 0;
        for (String columnName : metadata.getValue().getNames()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(columnName);
            count++;
        }

        sb.append(")\nVALUES(?");
        for (int i = 1; i < count; i++) {
            sb.append(",?");
        }

        sb.append(")");

        return sb.toString();
    }

    @Override
    public String buildUpdateStatement(TableMetadata metadata) {
        Objects.requireNonNull(metadata);

        final StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ").append(metadata.getTableName()).append(" SET ");

        int count = 0;
        for (String columnName : metadata.getValue().getNames()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(columnName).append("=?");
            count++;
        }

        final List<String> keyColumns = metadata.getKey().getNames();

        sb.append("\nWHERE ");

        count = 0;
        for (String name : keyColumns) {
            if (count > 0) {
                sb.append("\n  AND ");
            }
            sb.append(name).append("=?");
            count++;
        }

        return sb.toString();
    }

    @Override
    public String buildMergeStatement(TableMetadata metadata) {
        Objects.requireNonNull(metadata);

        final UserTypeColumns key = metadata.getKey();
        final UserTypeColumns value = metadata.getValue();
        final StringBuilder sb = new StringBuilder();

        sb.append("MERGE INTO ").append(metadata.getTableName()).append(" T USING (SELECT ");

        int count = 0;
        for (String columnName : value.getNames()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append("? as ").append(columnName);
            count++;
        }

        sb.append(") S ON (");

        count = 0;
        for (String columnName : key.getNames()) {
            if (count > 0) {
                sb.append(" AND ");
            }
            sb.append("T.").append(columnName).append(" = S.").append(columnName);
            count++;
        }

        sb.append(")\n")
            .append("WHEN MATCHED THEN UPDATE SET ");

        final List<String> nonKeyColumns = Lists.newArrayList(value.getNames());
        nonKeyColumns.removeAll(key.getNames());
        Preconditions.checkArgument(!nonKeyColumns.isEmpty(), "Table has only key columns!");

        count = 0;
        for (String columnName : nonKeyColumns) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append("T.").append(columnName).append(" = S.").append(columnName);
            count++;
        }

        sb.append("\n")
            .append("WHEN NOT MATCHED THEN INSERT (");

        count = 0;
        for (String columnName : value.getNames()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(columnName);
            count++;
        }

        sb.append(") VALUES (");
        count = 0;
        for (String columnName : value.getNames()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append("S.").append(columnName);
            count++;
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Builds SQL delete statement with IN clause (complex if key consists of more than one column)
     *
     * @return SQL delete statement
     */
    @Override
    public String buildDeleteStatement(TableMetadata metadata, int keyCount) {
        Objects.requireNonNull(metadata);
        Preconditions.checkArgument(keyCount > 0);

        final StringBuilder sb = new StringBuilder();

        sb.append("DELETE FROM ").append(metadata.getTableName());

        addWhereInClause(metadata, keyCount, sb);

        return sb.toString();
    }

    /**
     * Builds SQL delete statement with IN clause (complex if key consists of more than one column)
     *
     * @return SQL delete statement
     */
    public void addWhereInClause(TableMetadata metadata, int keyCount, StringBuilder sb) {
        Objects.requireNonNull(metadata);
        Preconditions.checkArgument(keyCount > 0);

        final List<String> keyColumns = metadata.getKey().getNames();
        final boolean complexFlag = keyColumns.size() > 1;

        sb.append("\nWHERE ");

        if (complexFlag) {
            sb.append('(');
        }

        int count = 0;
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
    }

}
