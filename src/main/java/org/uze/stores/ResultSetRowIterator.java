package org.uze.stores;

import com.google.common.base.Preconditions;
import org.uze.jdbc.ResultSetHelper;
import org.uze.jdbc.TableMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Uze on 30.08.2014.
 */
public class ResultSetRowIterator extends AbstractIterator<Object> {

    private final TableMetadata metadata;
    private final ResultSet resultSet;
    private final List<String> columns;
    private int index;

    ResultSetRowIterator(TableMetadata metadata, ResultSet resultSet, boolean keyMode) {
        this.metadata = metadata;
        this.resultSet = resultSet;
        this.columns = keyMode ? metadata.getKeyColumnNames() : metadata.getValueColumnNames();
    }

    @Override
    public boolean hasNext() {
        return index < columns.size();
    }

    @Override
    public Object next() {
        Preconditions.checkArgument(index < columns.size());

        final String name = columns.get(index);

        index++;

        final TableMetadata.Column column = metadata.getColumn(name);

        try {
            final int columnIndex = resultSet.findColumn(name);

            return ResultSetHelper.getValue(resultSet, columnIndex, column.getClazz());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
