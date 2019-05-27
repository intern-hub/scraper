package com.internhub.data.managers;

import com.internhub.data.models.Company;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class CompanyManager {
    private static SessionFactory factory;

    static {
        String config = CompanyManager.class.getClassLoader().getResource("hibernate.cfg.xml").toExternalForm();
        factory = new Configuration().configure(config).buildSessionFactory();
    }

    // Get all of the companies that are saved in the SQL database
    public List<Company> selectAll() {
        List<Company> results = new ArrayList<>();
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query<Company> query = session.createQuery("from Company ORDER BY name", Company.class);
            results.addAll(query.list());
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            he.printStackTrace();
        } finally {
            session.close();
        }
        return results;
    }

    // For each new transient company object {$NAME, $WEBSITE}:
    // If a company doesn't exist in the database with $NAME, add it
    // Otherwise, update existing company object to have the same $WEBSITE
    public void bulkUpdate(List<Company> newCompanies) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for(int i = 0; i < newCompanies.size(); i++) {
                Company newCompany = newCompanies.get(i);
                Query<Company> query = session.createQuery("from Company where name = :name", Company.class);
                query.setParameter("name", newCompany.getName());
                List<Company> existing = query.list();
                if (existing.isEmpty()) {
                    // New company is now persistent
                    session.save(newCompany);
                } else {
                    // New company is still left transient,
                    // so we replace it with the old, persistent company object
                    Company oldCompany = existing.get(0);
                    oldCompany.setWebsite(newCompany.getWebsite());
                    session.update(oldCompany);
                    newCompanies.set(i, oldCompany);
                }
            }
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
