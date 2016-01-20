package org.uze.coherence.stores;

import com.google.common.base.Preconditions;
import com.tangosol.io.pof.reflect.PofValue;
import org.uze.coherence.util.AbstractIterator;
import org.uze.coherence.jdbc.Column;
import org.uze.coherence.jdbc.UserTypeColumns;
import org.uze.coherence.pof.BinaryHelper;

import java.util.Iterator;

/**
 * Created by Uze on 30.08.2014.
 */
public class UserTypeValueExtractor implements BinaryHelper.ValueExtractor {

    private final UserTypeColumns columns;
    private final PofValue pofValue;

    public UserTypeValueExtractor(UserTypeColumns columns, PofValue pofValue) {
        this.columns = columns;
        this.pofValue = pofValue;
    }

    @Override
    public int getUserTypeId() {
        return columns.getUserTypeId();
    }

    @Override
    public Iterable<Object> getValues() {
        return new Iterable<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return new PofValueIterator();
            }
        };
    }

    /**
     */
    private class PofValueIterator extends AbstractIterator<Object> {

        private int index;

        @Override
        public boolean hasNext() {
            return index < columns.getSize();
        }

        @Override
        public Object next() {
            Preconditions.checkArgument(index >= 0 && index < columns.getSize());

            final Column column = columns.getColumn(index);
            final PofValue value = pofValue.getChild(index);

            index++;

            return value.getValue(column.getClazz());
        }
    }
}
