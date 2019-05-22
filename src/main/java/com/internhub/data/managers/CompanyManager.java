package com.internhub.data.managers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;

import java.util.List;
import java.io.File;

public class CompanyManager {
    private static SessionFactory factory;

    static {
        /*
        Configuration configuration = new Configuration();
        configuration.configure(new File("src/main/Resources/hibernate.cgf.xml"));
        configuration.addAnnotatedClass(Position.class);
        ServiceRegistry srvcReg = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(srvcReg);
        */
        factory = new Configuration().configure(new File("/Users/roshan/scraper/src/main/Resources/hibernate.cgf.xml")).buildSessionFactory();
    }

    // For each new transient company object {$NAME, $WEBSITE}:
    // If a company doesn't exist in the database with $NAME, add it
    // Otherwise, update existing company object to have the same $WEBSITE
    @SuppressWarnings("Duplicates")
    public static void bulkUpdate(List<Company> newCompanies) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for(int i = 0; i < newCompanies.size(); i++) {
                String sql = "SELECT * FROM  company WHERE name=\"" + newCompanies.get(i).getName() + "\"";
                List results = session.createSQLQuery(sql).list();
                if(results.size() == 0) {
                    Query query = session.createSQLQuery("INSERT INTO company VALUES(:id, :name, :website)");
                    query.setParameter("id", newCompanies.get(i).getId());
                    query.setParameter("name", newCompanies.get(i).getName());
                    query.setParameter("website", newCompanies.get(i).getWebsite());
                    query.executeUpdate();
                } else {
                    // Update existing company object to have the same $WEBSITE
                    Query query = session.createSQLQuery("UPDATE company SET website = :newWebsite WHERE name=:name");
                    query.setParameter("newWebsite", newCompanies.get(i).getWebsite());
                    query.setParameter("name", newCompanies.get(i).getName());
                    query.executeUpdate();
                }
            }


            // TODO: Make calls to session here

            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            he.printStackTrace();
        } finally {
            session.close();
        }
    }
}
