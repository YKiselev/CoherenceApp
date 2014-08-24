package org.uze.stores;

import org.uze.jdbc.TableMetadata;
import org.uze.jdbc.TableMetadataBuilder;

import java.sql.Types;

/**
 * Created by Uze on 16.08.2014.
 */
public class Test1Store extends BaseJdbcBinaryEntryStore {

    private static final TableMetadata TABLE_METADATA = new TableMetadataBuilder("TEST1")
        .column("ID", Types.BIGINT, long.class)
        .key(0)
        .column("NAME", Types.VARCHAR, String.class)
        .build();

    public Test1Store() {
        setTableMetadata(TABLE_METADATA);
    }
}
