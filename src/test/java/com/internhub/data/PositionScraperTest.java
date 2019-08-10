package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.IPositionScraper;
import com.internhub.data.positions.scrapers.PositionScraper;
import com.internhub.data.positions.scrapers.strategies.impl.GoogleInitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSMTStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSStrategy;
import com.internhub.data.selenium.CloseableWebDriverAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PositionScraperTest {
    @Rule
    public MyStopWatch myStopWatch = new MyStopWatch();

    Logger logger = LoggerFactory.getLogger(PositionScraperTest.class);

    @Before
    public void setup() {
        Main.initChromeDriver();
    }

    @Test
    public void testPositionMTScraper() {
        try (CloseableWebDriverAdapter driverAdapter = new CloseableWebDriverAdapter()) {
            IPositionScraper IPositionScraper = new PositionScraper(
                    new GoogleInitialLinkStrategy(),
                    new PositionBFSMTStrategy(driverAdapter.getDriver()));
            Company google = new Company();
            google.setName("Amazon");
            google.setWebsite("https://amazon.com");
            List<Position> positions = IPositionScraper.fetch(google);

            for (Position position : positions) {
                logger.info(position.toString());
            }
        }
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
            List<Position> positions = IPositionScraper.fetch(google);

            for (Position position : positions) {
                logger.info(position.toString());
            }
        }
    }
}
