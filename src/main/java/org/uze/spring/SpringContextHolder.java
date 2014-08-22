package org.uze.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Uze on 17.08.2014.
 */
public enum SpringContextHolder {

    INSTANCE;

    private final ApplicationContext context;

    SpringContextHolder() {
        context = new ClassPathXmlApplicationContext("/spring/coherence-app-context.xml");
    }

    public static ApplicationContext getApplicationContext() {
        return INSTANCE.context;
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return INSTANCE.context.getBean(name, clazz);
    }

}
