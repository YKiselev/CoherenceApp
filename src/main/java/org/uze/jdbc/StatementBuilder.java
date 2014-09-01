package org.uze.jdbc;

import com.tangosol.util.BinaryEntry;
import org.uze.stores.SelectStatementContext;

import java.util.List;

/**
 * Created by Uze on 24.08.2014.
 */
public interface StatementBuilder {

    String buildSelectStatement(TableMetadata metadata, int keyCount);

    SelectStatementContext newSelectStatementContext(TableMetadata metadata, List<BinaryEntry> entries);

    String buildInsertStatement(TableMetadata metadata);

    String buildUpdateStatement(TableMetadata metadata);

    /**
     * Implementations must return {@code null} to indicate that merge statement no supported and store must
     * switch to signle update/insert logic
     *
     * @param metadata table metadata
     * @return merge statement or {@code null} if merging not supported
     */
    String buildMergeStatement(TableMetadata metadata);

    String buildDeleteStatement(TableMetadata metadata, int keyCount);
}
