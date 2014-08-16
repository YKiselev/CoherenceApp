package org.uze.stores;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.BasicDataSource;
import org.uze.coherence.Config;

import javax.sql.DataSource;
import java.util.Locale;

/**
* Created by Uze on 16.08.2014.
*/
public enum Holder {
    INSTANCE;

    private final DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    Holder() {
        try {
            // fuck Oracle
            Locale.setDefault(Locale.ENGLISH);

            BasicDataSource ds = new BasicDataSource();

            Configuration db = Config.getInstance().subset("database");

            ds.setDriverClassName(db.getString("driver"));
            ds.setUsername(db.getString("username"));
            ds.setPassword(db.getString("password"));
            ds.setUrl(db.getString("url"));

            ds.setMaxActive(5);
            ds.setMaxIdle(1);
            ds.setInitialSize(1);
            ds.setValidationQuery(db.getString("validationQuery"));

            this.dataSource = ds;
        } catch (Exception e) {
            throw new RuntimeException("Connection failed", e);
        }
    }
}
