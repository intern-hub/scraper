package com.internhub.data.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.net.URL;

public class HibernateUtils {
    public static SessionFactory buildSession() {
        URL url = HibernateUtils.class.getClassLoader().getResource("hibernate.cfg.xml");

        if(url == null) {
            throw new SecurityException("Missing configuration file, are you permitted to use this application?");
        }
        String config = url.toExternalForm();
        return new Configuration().configure(config).buildSessionFactory();
    }
}
