package org.uze.stores;

import org.uze.mappers.CounterpartPORowMapper;
import org.uze.pof.CounterpartPO;

/**
 * Created by Юрий on 10.12.13.
 */
public class CounterpartCacheStore extends MyBaseCacheStore<Long, CounterpartPO> {

    @Override
    protected void insert(Long key, CounterpartPO value) {
        exec("INSERT INTO COUNTERPARTS(ID, NAME)VALUES(?,?)", key, value.getName());
    }

    @Override
    protected int update(Long key, CounterpartPO value) {
        return exec("UPDATE COUNTERPARTS SET NAME = ? where ID = ?", value.getName(), key);
    }

    @Override
    protected CounterpartPO find(Long key) {
        return single("SELECT ID, NAME FROM COUNTERPARTS WHERE ID = ?", new CounterpartPORowMapper(), key);
    }

    @Override
    protected void delete(Long key) {
        exec("DELETE FROM COUNTERPARTS WHERE ID = ?", key);
    }
}
