package org.uze.strategy;

import com.tangosol.util.BinaryEntry;

import java.util.Set;

/**
 * Created by Uze on 16.08.2014.
 */
public interface BinaryEntryStoreStrategy {

    void load(Set<BinaryEntry> entries);

    void store(Set<BinaryEntry> entries);

    void erase(Set<BinaryEntry> entries);
}
