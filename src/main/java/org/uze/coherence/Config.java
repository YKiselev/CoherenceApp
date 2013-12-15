package org.uze.coherence;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Created by Uze on 11.12.13.
 */
public final class Config {

    private enum Holder {
        INSTANCE;

        final Configuration configuration;

        Holder() {
            try {
                this.configuration = new XMLConfiguration(ClassLoader.getSystemResource("app-config.xml"));
            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Configuration getInstance() {
        return Holder.INSTANCE.configuration;
    }

    private Config() {
    }
}
