package org.uze.stores;

import com.google.common.base.Preconditions;
import org.uze.jdbc.Column;
import org.uze.jdbc.ResultSetHelper;
import org.uze.jdbc.UserTypeColumns;
import org.uze.pof.BinaryHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created by Uze on 30.08.2014.
 */
class ResultSetValueExtractor implements BinaryHelper.ValueExtractor {

    private final UserTypeColumns columns;
    private final ResultSet resultSet;

    ResultSetValueExtractor(UserTypeColumns columns, ResultSet resultSet) {
        this.columns = columns;
        this.resultSet = resultSet;
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
                return new ResultSetRowIterator();
            }
        };
    }

    /**
     */
    private class ResultSetRowIterator extends AbstractIterator<Object> {

        private int index;

        @Override
        public boolean hasNext() {
            return index < columns.getSize();
        }

        @Override
        public Object next() {
            Preconditions.checkArgument(index < columns.getSize());

            final String name = columns.getName(index);
            final Column column = columns.getColumn(name);

            index++;

            try {
                final int columnIndex = resultSet.findColumn(name);

                return ResultSetHelper.getValue(resultSet, columnIndex, column.getClazz());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
