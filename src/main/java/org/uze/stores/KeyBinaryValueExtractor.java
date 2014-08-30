package org.uze.stores;

import com.tangosol.io.pof.reflect.PofValue;
import org.uze.jdbc.TableMetadata;
import org.uze.pof.BinaryHelper;

import java.util.Iterator;

/**
* Created by Uze on 30.08.2014.
*/
class KeyBinaryValueExtractor implements BinaryHelper.ValueExtractor {

    private final TableMetadata metadata;
    private final PofValue pofValue;

    KeyBinaryValueExtractor(TableMetadata metadata, PofValue pofValue) {
        this.metadata = metadata;
        this.pofValue = pofValue;
    }

    @Override
    public int getUserTypeId() {
        return metadata.getKeyUserTypeId();
    }

    @Override
    public Iterable<Object> getValues() {
        return new Iterable<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return new PofValueIterator(metadata, pofValue);
            }
        };
    }
}
