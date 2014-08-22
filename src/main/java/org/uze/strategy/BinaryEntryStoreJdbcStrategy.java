package org.uze.strategy;

import com.google.common.collect.Iterables;
import com.tangosol.util.BinaryEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.uze.jdbc.TableMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Uze on 16.08.2014.
 */
public class BinaryEntryStoreJdbcStrategy implements BinaryEntryStoreStrategy {

    private int batchSize = 1000;
    private JdbcTemplate jdbcTemplate;
    private TableMetadata tableMetadata;

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
    public void load(Iterable<BinaryEntry> entries) {
        jdbcTemplate.query("", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                //
            }
        });
        for (List<BinaryEntry> batch : Iterables.partition(entries, batchSize)) {

        }
    }

    @Override
    public void store(Iterable<BinaryEntry> entries) {
        for (List<BinaryEntry> batch : Iterables.partition(entries, batchSize)) {

        }
    }

    @Override
    public void erase(Iterable<BinaryEntry> entries) {
        for (List<BinaryEntry> batch : Iterables.partition(entries, batchSize)) {

        }
    }
}
