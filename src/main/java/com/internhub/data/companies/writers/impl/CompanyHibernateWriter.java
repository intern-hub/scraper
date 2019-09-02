package com.internhub.data.companies.writers.impl;

import com.google.common.collect.Lists;
import com.internhub.data.companies.writers.ICompanyWriter;
import com.internhub.data.models.Company;
import com.internhub.data.util.HibernateUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class CompanyHibernateWriter implements ICompanyWriter {
    private static SessionFactory factory = HibernateUtils.buildSession();

    @Override
    public void save(Company newCompany) {
        save(Lists.newArrayList(newCompany));
    }

    // For each company object:
    // If a company doesn't exist in the database with $NAME, add it
    // Otherwise, update existing company object to have the same $WEBSITE
    @Override
    public void save(List<Company> newCompanies) {
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
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            ex.printStackTrace();
        }
    }
}
