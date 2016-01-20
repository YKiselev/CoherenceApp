package org.uze.caches;

import org.uze.client.Counterpart;
import org.uze.coherence.pof.CounterpartPO;

/**
 * Created by Uze on 11.12.13.
 */
public class CounterpartCache extends CacheAccess<Long, Counterpart, CounterpartPO> {

    public CounterpartCache() {
        super("Counterparts");
    }

    @Override
    protected Counterpart fromObject(CounterpartPO src) {
        if (src == null) {
            return null;
        }
        Counterpart result = new Counterpart();

        result.setId(src.getId());
        result.setName(src.getName());

        return result;
    }

    @Override
    protected CounterpartPO toObject(Counterpart src) {
        if (src == null) {
            return null;
        }
        CounterpartPO result = new CounterpartPO();

        result.setId(src.getId());
        result.setName(src.getName());

        return result;
    }
}
