package org.uze.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.uze.coherence.pof.TradePO;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Uze on 11.12.13.
 */
public class TradePORowMapper implements RowMapper<TradePO> {

    @Override
    public TradePO mapRow(ResultSet resultSet, int i) throws SQLException {
        TradePO result = new TradePO();

        result.setId(resultSet.getLong("ID"));
        result.setName(resultSet.getString("NAME"));
        result.setDate(resultSet.getDate("CREATIONDATE"));
        result.setCounterpartId(resultSet.getLong("COUNTERPARTID"));

        return result;
    }
}
