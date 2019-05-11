package com.internhub.data.managers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class PositionManager {
    private static SessionFactory factory;

    static {
        factory = new Configuration().configure().buildSessionFactory();
    }

    // For each new scraped, transient position object {$LINK, $INFO}:
    // If position with $LINK exists, update existing position with $INFO.
    // Otherwise, add the entire position to the database
    public void bulkUpdate(List<Position> newPositions, Company oldCompany) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            // TODO: Make calls to session here - push new positions, update existing positions

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