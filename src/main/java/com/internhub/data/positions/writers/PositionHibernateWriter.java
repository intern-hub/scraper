package com.internhub.data.positions.writers;

import com.internhub.data.models.Position;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.net.URL;
import java.util.List;
import org.hibernate.query.Query;

public class PositionHibernateWriter {
    private static SessionFactory factory;

    static {
        URL url = PositionHibernateWriter.class.getClassLoader().getResource("hibernate.cfg.xml");

        if(url == null) {
            throw new SecurityException("Missing configuration file, are you permitted to use this application?");
        }
        String config = url.toExternalForm();
        factory = new Configuration().configure(config).buildSessionFactory();
    }

    /**
     * Given a list of positions, either inserts position into db or updates existing position entry
     */
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
