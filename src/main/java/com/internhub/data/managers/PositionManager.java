package com.internhub.data.managers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.util.List;
import org.hibernate.query.Query;

public class PositionManager {
    private static SessionFactory factory;

    static {
        //factory = new Configuration().configure().buildSessionFactory();
        factory = new Configuration().configure(new File("/Users/roshan/scraper/src/main/Resources/hibernate.cgf.xml")).buildSessionFactory();
    }

    // For each new scraped, transient position object {$LINK, $INFO}:
    // If position with $LINK exists, update existing position with $INFO.
    // Otherwise, add the entire position to the database
    @SuppressWarnings("Duplicates")
    // Took out company parameter
    public static void bulkUpdate(List<Position> newPositions) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for (int k = 0; k < newPositions.size(); k++) {
                String sql =  "SELECT * FROM  positions WHERE link=\"" + newPositions.get(k).getLink() + "\"";
                List results = session.createSQLQuery(sql).list();
                //String insertSql = "INSERT INTO positions VALUES (" +  newPositions.get(k).getId() + ", " + newPositions.get(k).getLink() + ", " +
                //        newPositions.get(k).getLocation() + ", " + newPositions.get(k).getSeason() + ", " + newPositions.get(k).getTitle() + ", " + newPositions.get(k).getYear() + ", " + newPositions.get(k).getCompany().getId() + ");";
                //System.out.println(insertSql);
                //session.createSQLQuery(insertSql);

                String sql2 =  "SELECT * FROM  positions WHERE link=\"" + "www.yahoo.com" + "\"";
                List results2 = session.createSQLQuery(sql).list();
                //System.out.println(results2);
                //session.save(newPositions.get(k));
                //session.getTransaction().commit();
                /*
                PositionEntity1 pme1 = new PositionEntity1(newPositions.get(k).getId(), newPositions.get(k).getCompany().getName(), newPositions.get(k).getLink());
                pme1.setId(newPositions.get(k).getId());
                pme1.setDegree(newPositions.get(k).getDegree());
                pme1.setLink(newPositions.get(k).getLink());
                pme1.setLocation(newPositions.get(k).getLocation());
                pme1.setSeason(newPositions.get(k).getSeason());
                pme1.setTitle(newPositions.get(k).getTitle());
                pme1.setYear(newPositions.get(k).getYear());
                pme1.setCompany(newPositions.get(k).getCompany().getId());
                session.save(pme1);
                session.getTransaction().commit();

                 */

                Query query = session.createSQLQuery("INSERT INTO positions VALUES(:id, :degree, :link, :location, :season, :title, :year, :company_id)");
                query.setParameter("id", newPositions.get(k).getId());
                query.setParameter("degree", newPositions.get(k).getDegree());
                query.setParameter("link", newPositions.get(k).getLink());
                query.setParameter("location", newPositions.get(k).getLocation());
                query.setParameter("season", 0);
                query.setParameter("title", newPositions.get(k).getTitle());
                query.setParameter("year", newPositions.get(k).getYear());
                query.setParameter("company_id", newPositions.get(k).getCompany().getId());
                query.executeUpdate();



                /*
                if(results == null) {
                    String insertSql = "INSERT INTO positions VALUES (" +  newPositions.get(k).getId() + ", " + newPositions.get(k).getLink() + ", " +
                            newPositions.get(k).getLocation() + ", " + newPositions.get(k).getSeason() + ", " + newPositions.get(k).getTitle() + ", " + newPositions.get(k).getYear() + ", " + newPositions.get(k).getCompany().getId() + ");";
                    System.out.println(insertSql);
                    session.createSQLQuery(insertSql);
                    //session.save(newPositions.get(k));
                    session.getTransaction().commit();
                }
                */

            }
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
