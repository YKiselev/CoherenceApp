package org.uze.caches;

import org.uze.client.Trade;
import org.uze.coherence.pof.TradePO;

/**
 * Created by Uze on 11.12.13.
 */
public class TradeCache extends CacheAccess<Long, Trade, TradePO> {

    public TradeCache() {
        super("Trades");
    }

    @Override
    protected Trade fromObject(TradePO src) {
        if (src == null) {
            return null;
        }
        Trade result = new Trade();

        result.setId(src.getId());
        result.setName(src.getName());
        result.setDate(src.getDate());
        if (src.getCounterpartId() != null) {
            result.setCounterpart(COUNTERPARTS.get(src.getCounterpartId()));
        }

        return result;
    }

    @Override
    protected TradePO toObject(Trade src) {
        if (src == null) {
            return null;
        }
        TradePO result = new TradePO();

        result.setId(src.getId());
        result.setName(src.getName());
        result.setDate(src.getDate());
        if (src.getCounterpart() != null) {
            result.setCounterpartId(src.getCounterpart().getId());
        }

        return result;
    }
}
