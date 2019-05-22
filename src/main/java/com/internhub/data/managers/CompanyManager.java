package com.internhub.data.managers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import org.hibernate.*;
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
            results.addAll(session.createQuery("from Company", Company.class).list());
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
    @SuppressWarnings("Duplicates")
    public void bulkUpdate(List<Company> newCompanies) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for(int i = 0; i < newCompanies.size(); i++) {
                String sql = "SELECT * FROM companies WHERE name=\"" + newCompanies.get(i).getName() + "\"";
                List results = session.createSQLQuery(sql).list();
                if(results.size() == 0) {
                    Query query = session.createSQLQuery("INSERT INTO companies VALUES(:id, :name, :website)");
                    query.setParameter("id", newCompanies.get(i).getId());
                    query.setParameter("name", newCompanies.get(i).getName());
                    query.setParameter("website", newCompanies.get(i).getWebsite());
                    query.executeUpdate();
                } else {
                    // Update existing company object to have the same $WEBSITE
                    Query query = session.createSQLQuery("UPDATE companies SET website = :newWebsite WHERE name=:name");
                    query.setParameter("newWebsite", newCompanies.get(i).getWebsite());
                    query.setParameter("name", newCompanies.get(i).getName());
                    query.executeUpdate();
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
