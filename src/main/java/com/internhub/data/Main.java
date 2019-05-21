package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.models.Season;
import com.internhub.data.scrapers.companies.CompanyScraper;
import com.internhub.data.scrapers.companies.RedditCompanyScraper;
import com.internhub.data.scrapers.positions.GreedyPositionScraper;
import com.internhub.data.scrapers.positions.PositionScraper;
import com.internhub.data.managers.PositionManager;
import com.internhub.data.managers.PositionEntity1;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.jsoup.Jsoup;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

public class Main {

    public static PositionManager pm = new PositionManager();

    public static void main(String[] args) {


        /*
        CompanyScraper comScraper = new RedditCompanyScraper();
        for (Company company : comScraper.fetch()) {
            System.out.println(company.getName() + " @ " + company.getWebsite());
        }
         */

        // System.setProperty("webdriver.chrome.driver", "src/main/Resources/chromedriver");

        //PositionScraper posScraper = new GreedyPositionScraper();
        //Company test = new Company();
        //test.setName("Capital One");
        //test.setWebsite("https://www.capitalonecareers.com/");
        //int count = 5;
        Position standard = new Position(12345678, "www.abcdefghijklmnop.com", new Company(99, "Dary Dillespie, Inc.", "www.daryd.com"),
                "Software intern", Season.SUMMER, 2019, "BS", "Utah");
        List<Position> newPositions = new ArrayList<Position>(Arrays.asList(standard));
        /*
        for (Position position : posScraper.fetch(test)) {
            newPositions.add(position);
            count++;
            if( count > 5 ) {
                break;
            }
        }
        */

        pm.bulkUpdate(newPositions);

    }
}
