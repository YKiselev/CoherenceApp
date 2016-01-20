package org.uze.coherence.jdbc;

import com.tangosol.io.pof.reflect.PofValue;
import org.springframework.dao.DataAccessException;

import java.sql.PreparedStatement;

/**
 * Created by Uze on 24.08.2014.
 */
public interface Statement {

    void setKey(PreparedStatement ps, PofValue key, int index) throws DataAccessException;
}
