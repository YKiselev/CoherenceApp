package org.uze.jdbc;

/**
 * Created by Uze on 24.08.2014.
 */
public interface StatementBuilder {
    String buildSelectStatement(TableMetadata metadata, int keyCount);

    String buildInsertStatement(TableMetadata metadata);

    String buildUpdateStatement(TableMetadata metadata);

    String buildMergeStatement(TableMetadata metadata);
}
