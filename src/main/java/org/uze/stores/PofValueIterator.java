package org.uze.stores;

import com.google.common.base.Preconditions;
import com.tangosol.io.pof.reflect.PofValue;
import org.uze.jdbc.TableMetadata;

import java.util.List;

/**
* Created by Uze on 30.08.2014.
*/
class PofValueIterator extends AbstractIterator<Object> {

    private final TableMetadata metadata;
    private final PofValue pofValue;
    private int index;


    PofValueIterator(TableMetadata metadata, PofValue pofValue) {
        this.metadata = metadata;
        this.pofValue = pofValue;
    }

    @Override
    public boolean hasNext() {
        return index < metadata.getKeyColumnNames().size();
    }

    @Override
    public Object next() {
        final List<String> keyColumns = metadata.getKeyColumnNames();
        Preconditions.checkArgument(index >= 0 && index < keyColumns.size());

        final TableMetadata.Column column = metadata.getColumn(keyColumns.get(index));
        final PofValue value = pofValue.getChild(index);

        index++;

        return value.getValue(column.getClazz());
    }
}
