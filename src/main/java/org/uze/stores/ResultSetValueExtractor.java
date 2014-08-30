package org.uze.stores;

import org.uze.jdbc.TableMetadata;
import org.uze.pof.BinaryHelper;

import java.sql.ResultSet;
import java.util.Iterator;

/**
* Created by Uze on 30.08.2014.
*/
class ResultSetValueExtractor implements BinaryHelper.ValueExtractor {

    private final TableMetadata metadata;
    private final ResultSet resultSet;
    private final boolean keyMode;

    ResultSetValueExtractor(TableMetadata metadata, ResultSet resultSet, boolean keyMode) {
        this.metadata = metadata;
        this.resultSet = resultSet;
        this.keyMode = keyMode;
    }

    @Override
    public int getUserTypeId() {
        return keyMode ? metadata.getKeyUserTypeId() : metadata.getValueUserTypeId();
    }

    @Override
    public Iterable<Object> getValues() {
        return new Iterable<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return new ResultSetRowIterator(metadata, resultSet, keyMode);
            }
        };
    }
}
