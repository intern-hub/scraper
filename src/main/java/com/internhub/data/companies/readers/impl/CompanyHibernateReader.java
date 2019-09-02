package com.internhub.data.companies.readers.impl;

import com.internhub.data.companies.readers.ICompanyReader;
import com.internhub.data.models.Company;
import com.internhub.data.util.HibernateUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyHibernateReader implements ICompanyReader {
    private static SessionFactory factory = HibernateUtils.buildSession();

    // Get all of the companies that are saved in the SQL database
    @Override
    public List<Company> getAll() {
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
    @Override
    public List<Company> getByName(String name) {
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
}
