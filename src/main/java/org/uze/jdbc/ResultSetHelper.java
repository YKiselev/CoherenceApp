package org.uze.jdbc;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Uze on 30.08.2014.
 */
public class ResultSetHelper {

    private static final ImmutableMap<Class, Getter> GETTERS;

    static {
        GETTERS = new ImmutableMap.Builder<Class, Getter>()
            .put(Byte.class, ByteGetter.INSTANCE)
            .put(byte.class, ByteGetter.INSTANCE)
            .put(Short.class, ShortGetter.INSTANCE)
            .put(short.class, ShortGetter.INSTANCE)
            .put(Integer.class, IntegerGetter.INSTANCE)
            .put(int.class, IntegerGetter.INSTANCE)
            .put(Long.class, LongGetter.INSTANCE)
            .put(long.class, LongGetter.INSTANCE)
            .put(Double.class, DoubleGetter.INSTANCE)
            .put(double.class, DoubleGetter.INSTANCE)
            .put(Float.class, FloatGetter.INSTANCE)
            .put(float.class, FloatGetter.INSTANCE)
            .put(BigDecimal.class, BigDecimalGetter.INSTANCE)
            .put(Date.class, DateGetter.INSTANCE)
            .build();
    }

    private ResultSetHelper() {
    }

    public static Object getValue(ResultSet resultSet, int column, Class clazz) throws SQLException {
        final Getter getter = GETTERS.get(clazz);
        if (getter != null) {
            return getter.getValue(resultSet, column);
        }
        return resultSet.getObject(column);
    }

    interface Getter {

        Object getValue(ResultSet resultSet, int column) throws SQLException;
    }

    enum ByteGetter implements Getter {
        INSTANCE;

        @Override
        public Object getValue(ResultSet resultSet, int column) throws SQLException {
            return resultSet.getByte(column);
        }
    }

    enum ShortGetter implements Getter {
        INSTANCE;

        @Override
        public Object getValue(ResultSet resultSet, int column) throws SQLException {
            return resultSet.getShort(column);
        }
    }

    enum IntegerGetter implements Getter {
        INSTANCE;

        @Override
        public Object getValue(ResultSet resultSet, int column) throws SQLException {
            return resultSet.getInt(column);
        }
    }

    enum LongGetter implements Getter {
        INSTANCE;

        @Override
        public Object getValue(ResultSet resultSet, int column) throws SQLException {
            return resultSet.getLong(column);
        }
    }

    enum DoubleGetter implements Getter {
        INSTANCE;

        @Override
        public Object getValue(ResultSet resultSet, int column) throws SQLException {
            return resultSet.getDouble(column);
        }
    }

    enum FloatGetter implements Getter {
        INSTANCE;

        @Override
        public Object getValue(ResultSet resultSet, int column) throws SQLException {
            return resultSet.getFloat(column);
        }
    }

    enum BigDecimalGetter implements Getter {
        INSTANCE;

        @Override
        public Object getValue(ResultSet resultSet, int column) throws SQLException {
            return resultSet.getBigDecimal(column);
        }
    }

    enum DateGetter implements Getter {
        INSTANCE;

        @Override
        public Object getValue(ResultSet resultSet, int column) throws SQLException {
            Object value = resultSet.getObject(column);
            if (value instanceof Date) {
                value = new Date(((java.sql.Date) value).getTime());
            }
            return value;
        }
    }
}
