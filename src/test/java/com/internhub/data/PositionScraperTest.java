package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.positions.scrapers.IPositionScraper;
import com.internhub.data.positions.scrapers.PositionScraper;
import com.internhub.data.positions.scrapers.strategies.impl.GoogleInitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSStrategy;
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
            IPositionScraper IPositionScraper = new PositionScraper(
                    new GoogleInitialLinkStrategy(),
                    new PositionBFSStrategy(driverAdapter.getDriver()));
            Company google = new Company();
            google.setName("Amazon");
            google.setWebsite("https://amazon.com");
            IPositionScraper.fetch(google);
        }
    }
}
