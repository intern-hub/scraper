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
    @SuppressWarnings("Duplicates")
    // Took out company parameter
    public void bulkUpdate(List<Position> newPositions) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for (int k = 0; k < newPositions.size(); k++) {
                String sql =  "SELECT * FROM positions WHERE link=\"" + newPositions.get(k).getLink() + "\"";
                List results = session.createSQLQuery(sql).list();
                if(results.size() == 0) {
                    Query query = session.createSQLQuery("INSERT INTO positions VALUES(:id, :degree, :link, :location, :season, :title, :year, :company_id)");
                    query.setParameter("id", newPositions.get(k).getId());
                    query.setParameter("degree", newPositions.get(k).getDegree());
                    query.setParameter("link", newPositions.get(k).getLink());
                    query.setParameter("location", newPositions.get(k).getLocation());
                    switch(newPositions.get(k).getSeason()) {
                        case SUMMER:
                            query.setParameter("season", 0);
                        case WINTER:
                            query.setParameter("season", 1);
                        case FALL:
                            query.setParameter("season", 2);
                        case SPRING:
                            query.setParameter("season", 3);

                    }
                    query.setParameter("title", newPositions.get(k).getTitle());
                    query.setParameter("year", newPositions.get(k).getYear());
                    query.setParameter("company_id", newPositions.get(k).getCompany().getId());
                    query.executeUpdate();
                } else {
                    // update existing position with $INFO
                    Query query = session.createSQLQuery("UPDATE positions SET degree=:degree, location=:location, season=:season, title=:title, year=:year, company_id=:company_id WHERE link=:link");
                    query.setParameter("degree", newPositions.get(k).getDegree());
                    query.setParameter("link", newPositions.get(k).getLink());
                    query.setParameter("location", newPositions.get(k).getLocation());
                    switch(newPositions.get(k).getSeason()) {
                        case SUMMER:
                            query.setParameter("season", 0);
                        case WINTER:
                            query.setParameter("season", 1);
                        case FALL:
                            query.setParameter("season", 2);
                        case SPRING:
                            query.setParameter("season", 3);

                    }
                    query.setParameter("title", newPositions.get(k).getTitle());
                    query.setParameter("year", newPositions.get(k).getYear());
                    query.setParameter("company_id", newPositions.get(k).getCompany().getId());
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
