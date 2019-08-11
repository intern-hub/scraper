package com.internhub.data;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.IPositionMTScraper;
import com.internhub.data.positions.scrapers.IPositionScraper;
import com.internhub.data.positions.scrapers.PositionMTScraper;
import com.internhub.data.positions.scrapers.PositionScraper;
import com.internhub.data.positions.scrapers.strategies.impl.GoogleInitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSMTStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSStrategy;
import com.internhub.data.selenium.MyWebDriver;
import com.internhub.data.selenium.MyWebDriverPool;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Future;

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
        List<Company> companies = buildCompanies();
        List<Position> positions = Lists.newArrayList();
        List<Future> futures = Lists.newArrayList();
        try (MyWebDriverPool pool = new MyWebDriverPool()) {
            IPositionMTScraper IPositionScraper = new PositionMTScraper(
                    new GoogleInitialLinkStrategy(),
                    new PositionBFSMTStrategy(pool));
            for (Company company : companies) {
                futures.addAll(IPositionScraper.fetch(company));
            }
        }
        for (Position position : positions) {
            logger.info(position.toString());
        }
    }

    @Test
    public void testPositionScraper() {
        List<Company> companies = buildCompanies();
        List<Position> positions = Lists.newArrayList();
        try (MyWebDriver driverAdapter = new MyWebDriver()) {
            IPositionScraper IPositionScraper = new PositionScraper(
                    new GoogleInitialLinkStrategy(),
                    new PositionBFSStrategy(driverAdapter.getDriver()));
            for (Company company : companies) {
                positions.addAll(IPositionScraper.fetch(company));
            }
        }
        for (Position position : positions) {
            logger.info(position.toString());
        }
    }

    private List<Company> buildCompanies() {
        List<Company> ret = Lists.newArrayList();
        Company amazon = new Company();
        amazon.setName("Amazon");
        amazon.setWebsite("https://amazon.com");

        Company alarm = new Company();
        amazon.setName("Alarm");
        amazon.setWebsite("https://alarm.com");

        Company uber = new Company();
        amazon.setName("Uber");
        amazon.setWebsite("https://uber.com");

        Company lyft = new Company();
        amazon.setName("Lyft");
        amazon.setWebsite("https://lyft.com");

        ret.add(amazon);
        ret.add(alarm);
        ret.add(uber);
        ret.add(lyft);

        return ret;
    }
}
