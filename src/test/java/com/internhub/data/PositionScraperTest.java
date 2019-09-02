package com.internhub.data;

import com.google.common.collect.Lists;
import com.internhub.data.companies.readers.CompanyReader;
import com.internhub.data.companies.readers.impl.CompanyHibernateReader;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.IPositionScraper;
import com.internhub.data.positions.scrapers.PositionScraper;
import com.internhub.data.positions.scrapers.strategies.InitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.GoogleInitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSMTStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSStrategy;
import com.internhub.data.positions.writers.PositionWriter;
import com.internhub.data.positions.writers.impl.PositionHibernateWriter;
import com.internhub.data.selenium.MyWebDriver;
import com.internhub.data.selenium.MyWebDriverPool;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PositionScraperTest {
    @Rule
    public MyStopWatch myStopWatch = new MyStopWatch();

    Logger logger = LoggerFactory.getLogger(PositionScraperTest.class);

    @Before
    public void setup() {
        Main.initChromeDriver();
    }

    @Test
    public void testPositionMTScraper() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Company> companies = buildCompanies();
        List<Callable<List<Position>>> tasks = Lists.newArrayList();
        try (MyWebDriverPool pool = new MyWebDriverPool()) {
            InitialLinkStrategy linkStrategy = new GoogleInitialLinkStrategy();
            for (Company company : companies) {
                IPositionScraper positionScraper = new PositionScraper(
                        linkStrategy,
                        new PositionBFSMTStrategy(pool));
                tasks.add(() -> positionScraper.fetch(company));
            }
        }

        List<Future<List<Position>>> futures = executor.invokeAll(tasks);
        List<Position> positions = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());

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

    @Test
    public void testHibernateWriter() throws InterruptedException {
        CompanyReader companyReader = new CompanyHibernateReader();
        List<Company> companies = companyReader.getAll();

        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<List<Position>>> tasks = Lists.newArrayList();
        try (MyWebDriverPool pool = new MyWebDriverPool()) {
            InitialLinkStrategy linkStrategy = new GoogleInitialLinkStrategy();
            for (Company company : companies) {
                IPositionScraper positionScraper = new PositionScraper(
                        linkStrategy,
                        new PositionBFSMTStrategy(pool));
                tasks.add(() -> positionScraper.fetch(company));
            }
        }

        List<Future<List<Position>>> futures = executor.invokeAll(tasks);
        List<Position> positions = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());

        PositionWriter writer = new PositionHibernateWriter();
        writer.save(positions);
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
        alarm.setName("Alarm");
        alarm.setWebsite("https://alarm.com");

        Company uber = new Company();
        uber.setName("Uber");
        uber.setWebsite("https://uber.com");

        Company lyft = new Company();
        lyft.setName("Lyft");
        lyft.setWebsite("https://lyft.com");

        Company goldman = new Company();
        goldman.setName("Goldman Sachs");
        goldman.setWebsite("https://goldmansachs.com");

        Company jetbrains = new Company();
        jetbrains.setName("Jetbrains");
        jetbrains.setWebsite("https://jetbrains.com");

        ret.add(goldman);
        ret.add(jetbrains);
        ret.add(amazon);
        ret.add(alarm);
        ret.add(uber);
        ret.add(lyft);

        return ret;
    }
}
