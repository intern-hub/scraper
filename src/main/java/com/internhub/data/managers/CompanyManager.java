package com.internhub.data.managers;

import com.internhub.data.models.Company;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyManager {
    private static SessionFactory factory;

    static {
        URL url = CompanyManager.class.getClassLoader().getResource("hibernate.cfg.xml");
        if(url == null) {
            throw new SecurityException("Missing configuration file, are you permitted to use this application?");
        }

        String config = url.toExternalForm();
        factory = new Configuration().configure(config).buildSessionFactory();
    }

    // Get all of the companies that are saved in the SQL database
    public List<Company> selectAll() {
        List<Company> results = new ArrayList<>();
        Map<Long, Long> ranking = new HashMap<>();

        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            Query<Company> query = session.createQuery("from Company ORDER BY name", Company.class);
            results.addAll(query.list());
            // Count the number of positions attached to each company
            for (Company company : results) {
                Query countQuery = session.createQuery("select count(*) from Position WHERE company_id = :id");
                countQuery.setParameter("id", company.getId());
                ranking.put(company.getId(), (Long) countQuery.uniqueResult());
            }
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            he.printStackTrace();
        }

        // Order companies first by number of positions attached to them, then alphabetically
        results.sort((a, b) -> (int) (ranking.get(a.getId()) - ranking.get(b.getId())));
        return results;
    }

    // Return a list of companies that have the specified name
    public List<Company> selectByName(String name) {
        List<Company> results = new ArrayList<>();

        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            Query<Company> query = session.createQuery("from Company WHERE name = :name", Company.class);
            query.setParameter("name", name);
            results.addAll(query.list());
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            he.printStackTrace();
        }
        return results;
    }

    // For each new transient company object {$NAME, $WEBSITE}:
    // If a company doesn't exist in the database with $NAME, add it
    // Otherwise, update existing company object to have the same $WEBSITE
    public void bulkUpdate(List<Company> newCompanies) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            for (int i = 0; i < newCompanies.size(); i++) {
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
        }
    }
}
