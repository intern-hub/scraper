package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.scrapers.PositionScraper;
import com.internhub.data.scrapers.impl.DefaultPositionScraper;
import com.internhub.data.selenium.CloseableWebDriverAdapter;
import org.junit.Before;
import org.junit.Test;

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
}
