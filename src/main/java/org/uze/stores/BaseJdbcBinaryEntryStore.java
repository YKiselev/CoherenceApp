package org.uze.stores;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.uze.strategy.BinaryEntryStoreJdbcStrategy;

/**
 * Created by Uze on 19.08.2014.
 */
public class BaseJdbcBinaryEntryStore extends BaseBinaryEntryStore {

    public BaseJdbcBinaryEntryStore() {
        super(new BinaryEntryStoreJdbcStrategy());
    }

//    public int getBatchSize() {
//        return getStrategy().getb batchSize;
//    }
//
//    public void setBatchSize(int batchSize) {
//        this.batchSize = batchSize;
//    }
//
//    public NamedParameterJdbcTemplate getJdbcTemplate() {
//        return jdbcTemplate;
//    }
//
//    public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
}
