package org.uze.jdbc;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Uze on 24.08.2014.
 */
public class SelectStatement implements PreparedStatementSetter, RowCallbackHandler {

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {

    }

    @Override
    public void processRow(ResultSet rs) throws SQLException {

    }
}
