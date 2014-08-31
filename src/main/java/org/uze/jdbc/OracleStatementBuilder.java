package org.uze.jdbc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * Created by Uze on 18.08.2014.
 */
public class OracleStatementBuilder extends DefaultStatementBuilder {

    @Override
    public String buildMergeStatement(TableMetadata metadata) {
        Objects.requireNonNull(metadata);

        final UserTypeColumns key = metadata.getKey();
        final UserTypeColumns value = metadata.getValue();
        final StringBuilder sb = new StringBuilder();

        sb.append("MERGE INTO ").append(metadata.getTableName()).append(" T USING (SELECT ");

        int count = 0;
        for (String columnName : metadata.getAllColumns()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append("? as ").append(columnName);
            count++;
        }

        sb.append(" FROM DUAL) S ON (");

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
        for (String columnName : metadata.getAllColumns()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append(columnName);
            count++;
        }

        sb.append(") VALUES (");
        count = 0;
        for (String columnName : metadata.getAllColumns()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append("S.").append(columnName);
            count++;
        }
        sb.append(")");

        return sb.toString();
    }
}
