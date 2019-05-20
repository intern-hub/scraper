package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.scrapers.companies.CompanyScraper;
import com.internhub.data.scrapers.companies.RedditCompanyScraper;
import com.internhub.data.scrapers.positions.GreedyPositionScraper;
import com.internhub.data.scrapers.positions.PositionScraper;

import org.jsoup.Jsoup;
import java.io.PrintWriter;

public class Main {


    public static void main(String[] args) {
        CompanyScraper comScraper = new RedditCompanyScraper();
        for (Company company : comScraper.fetch()) {
            System.out.println(company.getName() + " @ " + company.getWebsite());
        }

        System.setProperty("webdriver.chrome.driver", "src/main/Resources/chromedriver");

        PositionScraper posScraper = new GreedyPositionScraper();
        Company test = new Company();
        test.setName("Capital One");
        test.setWebsite("https://www.capitalonecareers.com/");
        for (Position position : posScraper.fetch(test)) {
            System.out.println(position.getLink());
        }

        /*
        String website_html = Jsoup.connect(test.getWebsite()).get(); // returns a string with the html
        PrintWriter out = new PrintWriter("/Users/roshan/capitalone_html.txt");
        out.println(website_html);
         */

    }
}
