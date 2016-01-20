package org.uze.coherence.stores;

import com.tangosol.net.cache.BinaryEntryStore;
import com.tangosol.util.BinaryEntry;

import java.util.Collections;

/**
 * Created by Uze on 15.08.2014.
 */
public abstract class AbstractBinaryEntryStore implements BinaryEntryStore {

    @Override
    public void load(BinaryEntry binaryEntry) {
        loadAll(Collections.singleton(binaryEntry));
    }

    @Override
    public void store(BinaryEntry binaryEntry) {
        storeAll(Collections.singleton(binaryEntry));
    }

    @Override
    public void erase(BinaryEntry binaryEntry) {
        eraseAll(Collections.singleton(binaryEntry));
    }
}
