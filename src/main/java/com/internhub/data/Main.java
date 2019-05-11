package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.scrapers.companies.CompanyScraper;
import com.internhub.data.scrapers.companies.RedditCompanyScraper;
import com.internhub.data.scrapers.positions.GreedyPositionScraper;
import com.internhub.data.scrapers.positions.PositionScraper;
import org.apache.commons.exec.OS;

public class Main {


    public static void main(String[] args) {
        // Set Chrome driver for Selenium
        if (OS.isFamilyWindows()) {
            System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        }
        else if (OS.isFamilyMac()) {
            System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        }
        else if (OS.isFamilyUnix()) {
            System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver_linux");
        }
        else {
            throw new RuntimeException("Running on unsupported OS.");
        }

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
