package org.uze.jdbc;

import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.util.BinaryEntry;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * Created by Uze on 24.08.2014.
 */
public interface Statement {

    void setKey(PreparedStatement ps, PofValue key, int index) throws DataAccessException;
}
