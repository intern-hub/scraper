package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.scrapers.companies.CompanyScraper;
import com.internhub.data.scrapers.companies.RedditCompanyScraper;
import com.internhub.data.scrapers.positions.GreedyPositionScraper;
import com.internhub.data.scrapers.positions.PositionScraper;

public class Main {
    public static void main(String[] args) {
        /*
        CompanyScraper scraper = new RedditCompanyScraper();
        for (Company company : scraper.fetch()) {
            System.out.println(company.getName() + " @ " + company.getWebsite());
        }
        */
        PositionScraper scraper = new GreedyPositionScraper();
        Company test = new Company();
        test.setName("Capital One");
        test.setWebsite("https://www.capitalonecareers.com/");
        for (Position position : scraper.fetch(test)) {
            System.out.println(position.getLink());
        }
    }
}
