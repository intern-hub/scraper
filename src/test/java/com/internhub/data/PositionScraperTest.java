package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.scrapers.MTScraper;
import com.internhub.data.scrapers.PositionScraper;
import com.internhub.data.scrapers.impl.DefaultPositionScraper;
import com.internhub.data.scrapers.impl.mt.MTPositionScraper;
import com.internhub.data.selenium.CloseableWebDriverAdapter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PositionScraperTest {

    @Before
    public void setup() {
        Main.initChromeDriver();
    }

    @Test
    public void testPositionScraper() {
        try (CloseableWebDriverAdapter driverAdapter = new CloseableWebDriverAdapter()) {
            PositionScraper positionScraper = new DefaultPositionScraper(driverAdapter.getDriver());

            Company google = new Company();
            google.setName("Google");
            google.setWebsite("https://google.com");
            positionScraper.fetch(google);
        }
    }

    @Test
    public void testPositionScraperMT() {
        Company google = getCompany("google", "https://google.com");
        Company facebook = getCompany("facebook", "https://facebook.com");
        Company amazon = getCompany("amazon", "https://amazon.com");
        List<Company> companies = new ArrayList<>();
        companies.add(google);
        companies.add(facebook);
        companies.add(amazon);

        MTScraper scraper = new MTPositionScraper();
        scraper.load(companies);
        Collection<Position> positions = scraper.scrape();
    }

    private Company getCompany(String name, String url) {
        Company company = new Company();
        company.setName(name);
        company.setWebsite(url);
        return company;
    }
}
