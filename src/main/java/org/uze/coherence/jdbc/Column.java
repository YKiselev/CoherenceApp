package org.uze.coherence.jdbc;

/**
* Created by Uze on 31.08.2014.
*/
public class Column {

    private final int sqlType;
    private final Class clazz;
    private final boolean keyOnly;

    public int getSqlType() {
        return sqlType;
    }

    public Class getClazz() {
        return clazz;
    }

    public boolean isKeyOnly() {
        return keyOnly;
    }

    Column(int sqlType, Class clazz, boolean keyOnly) {
        this.sqlType = sqlType;
        this.clazz = clazz;
        this.keyOnly = keyOnly;
    }

    @Override
    public String toString() {
        return "Column{" +
            "sqlType=" + sqlType +
            ", clazz=" + clazz +
            '}';
    }
}
