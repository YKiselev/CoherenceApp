package org.uze.strategy;

import com.tangosol.util.BinaryEntry;

/**
 * Created by Uze on 16.08.2014.
 */
public interface BinaryEntryStoreStrategy {

    void load(Iterable<BinaryEntry> entries);

    void store(Iterable<BinaryEntry> entries);

    void erase(Iterable<BinaryEntry> entries);
}
