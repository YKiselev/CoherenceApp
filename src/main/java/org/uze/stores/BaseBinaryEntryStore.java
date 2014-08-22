package org.uze.stores;

import com.google.common.collect.Iterables;
import com.tangosol.net.cache.BinaryEntryStore;
import com.tangosol.util.BinaryEntry;
import org.uze.strategy.BinaryEntryStoreStrategy;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Uze on 15.08.2014.
 */
public class BaseBinaryEntryStore implements BinaryEntryStore {

    private BinaryEntryStoreStrategy strategy;

    public BaseBinaryEntryStore() {
    }

    public BaseBinaryEntryStore(BinaryEntryStoreStrategy strategy) {
        this.strategy = strategy;
    }

    public BinaryEntryStoreStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(BinaryEntryStoreStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void load(BinaryEntry binaryEntry) {
        loadAll(Collections.singleton(binaryEntry));
    }

    @Override
    public void loadAll(Set set) {
        strategy.load(Iterables.filter(set, BinaryEntry.class));
    }

    @Override
    public void store(BinaryEntry binaryEntry) {
        storeAll(Collections.singleton(binaryEntry));
    }

    @Override
    public void storeAll(Set set) {
        strategy.store(Iterables.filter(set, BinaryEntry.class));
    }

    @Override
    public void erase(BinaryEntry binaryEntry) {
        eraseAll(Collections.singleton(binaryEntry));
    }

    @Override
    public void eraseAll(Set set) {
        strategy.erase(Iterables.filter(set, BinaryEntry.class));
    }
}
