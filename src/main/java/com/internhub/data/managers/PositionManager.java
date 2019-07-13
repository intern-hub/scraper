package com.internhub.data.managers;

import com.internhub.data.models.Position;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;
import org.hibernate.query.Query;

public class PositionManager {
    private static SessionFactory factory;

    static {
        String config = PositionManager.class.getClassLoader().getResource("hibernate.cfg.xml").toExternalForm();
        factory = new Configuration().configure(config).buildSessionFactory();
    }

    // For each new scraped, transient position object {$LINK, $INFO}:
    // If position with $LINK exists, update existing position with $INFO.
    // Otherwise, add the entire position to the database
    public void bulkUpdate(List<Position> newPositions) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            for (int i = 0; i < newPositions.size(); i++) {
                Position newPosition = newPositions.get(i);
                Query<Position> query = session.createQuery("from Position where link = :link", Position.class);
                query.setParameter("link", newPosition.getLink());
                List<Position> existing = query.list();
                if (existing.isEmpty()) {
                    // New position is persistent
                    session.save(newPosition);
                } else {
                    // New position is still left transient,
                    // so we replace it with the old, persistent position object
                    Position oldPosition = existing.get(0);
                    oldPosition.setCompany(newPosition.getCompany());
                    oldPosition.setTitle(newPosition.getTitle());
                    oldPosition.setSeason(newPosition.getSeason());
                    oldPosition.setYear(newPosition.getYear());
                    oldPosition.setDegree(newPosition.getDegree());
                    oldPosition.setLocation(newPosition.getLocation());
                    session.update(oldPosition);
                    newPositions.set(i, oldPosition);
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
