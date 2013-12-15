package org.uze.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.uze.pof.CounterpartPO;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Uze on 11.12.13.
 */
public class CounterpartPORowMapper implements RowMapper<CounterpartPO> {

    @Override
    public CounterpartPO mapRow(ResultSet resultSet, int i) throws SQLException {
        CounterpartPO result = new CounterpartPO();

        result.setId(resultSet.getLong("ID"));
        result.setName(resultSet.getString("NAME"));

        return result;
    }
}
