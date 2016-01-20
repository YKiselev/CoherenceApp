package org.uze.coherence.jdbc;

import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.io.pof.reflect.SimplePofValue;
import com.tangosol.util.Binary;
import org.uze.coherence.pof.BinaryHelper;
import org.uze.coherence.stores.ResultSetValueExtractor;
import org.uze.coherence.stores.UserTypeValueExtractor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Uze on 02.09.2014.
 */
public final class KeyHelper {

    private KeyHelper() {
    }

    private Object create(ResultSet rs, UserTypeColumns columns, PofContext pofContext) throws IOException, SQLException {
        if (columns.getSize() == 1) {
            final String name = columns.getName(0);
            final Column column = columns.getColumn(name);
            final int columnIndex = rs.findColumn(name);
            return ResultSetHelper.getValue(rs, columnIndex, column.getClazz());
        }

        return BinaryHelper.toBinary(new ResultSetValueExtractor(columns, rs), pofContext);
    }

    private Object create(Binary key, UserTypeColumns columns, PofContext pofContext) throws IOException {
        final PofValue pofValue = PofValueParser.parse(key, pofContext);

        if (pofValue instanceof SimplePofValue) {
            return pofValue.getValue();
        }

        return BinaryHelper.toBinary(new UserTypeValueExtractor(columns, pofValue), pofContext);
    }

}
