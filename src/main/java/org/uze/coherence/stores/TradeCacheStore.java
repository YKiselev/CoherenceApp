package org.uze.coherence.stores;

import org.uze.mappers.TradePORowMapper;
import org.uze.coherence.pof.TradePO;

import java.sql.Date;

/**
 * Created by Юрий on 10.12.13.
 */
public class TradeCacheStore extends MyBaseCacheStore<Long, TradePO> {

    @Override
    protected void insert(Long key, TradePO value) {
        exec("INSERT INTO TRADES(id, name, creationDate, counterpartId)VALUES(?,?,?,?)", key, value.getName(),
                new Date(value.getDate().getTime()), value.getCounterpartId());
    }

    @Override
    protected int update(Long key, TradePO value) {
        return exec("UPDATE TRADES SET name = ?, creationDate = ?, counterpartId = ? where id = ?", value.getName(),
                new Date(value.getDate().getTime()), value.getCounterpartId(), key);
    }

    @Override
    protected TradePO find(Long key) {
        return single("SELECT id, name, creationDate, counterpartId FROM TRADES WHERE id = ?", new TradePORowMapper(), key);
    }

    @Override
    protected void delete(Long key) {
        exec("DELETE FROM TRADES WHERE id=?", key);
    }
}
