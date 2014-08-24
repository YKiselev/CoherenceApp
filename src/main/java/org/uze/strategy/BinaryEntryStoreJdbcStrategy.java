package org.uze.strategy;

import com.tangosol.util.BinaryEntry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.uze.jdbc.StatementBuilder;
import org.uze.jdbc.TableMetadata;

import java.util.Set;

/**
 * Created by Uze on 16.08.2014.
 */
public class BinaryEntryStoreJdbcStrategy implements BinaryEntryStoreStrategy {

    private int batchSize = 1000;
    private JdbcTemplate jdbcTemplate;
    private TableMetadata tableMetadata;
    private StatementBuilder statementBuilder;
    private String selectStatement;
    private String insertStatement;
    private String updateStatement;
    private String deleteStatement;
    private String mergeStatement;

    public StatementBuilder getStatementBuilder() {
        return statementBuilder;
    }

    public void setStatementBuilder(StatementBuilder statementBuilder) {
        this.statementBuilder = statementBuilder;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public TableMetadata getTableMetadata() {
        return tableMetadata;
    }

    public void setTableMetadata(TableMetadata tableMetadata) {
        this.tableMetadata = tableMetadata;
    }

    @Override
    public void load(Set<BinaryEntry> entries) {
//        jdbcTemplate.query("", new RowCallbackHandler() {
//            @Override
//            public void processRow(ResultSet resultSet) throws SQLException {
//                //
//            }
//        });
//        for (List<BinaryEntry> batch : Iterables.partition(entries, batchSize)) {
//
//        }
    }

    @Override
    public void store(Set<BinaryEntry> entries) {
//        for (List<BinaryEntry> batch : Iterables.partition(entries, batchSize)) {
//
//        }
    }

    @Override
    public void erase(Set<BinaryEntry> entries) {
//        for (List<BinaryEntry> batch : Iterables.partition(entries, batchSize)) {
//
//        }
    }
}
