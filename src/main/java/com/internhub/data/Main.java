package com.internhub.data;

import com.google.common.collect.Lists;
import com.internhub.data.companies.readers.CompanyReader;
import com.internhub.data.companies.readers.impl.CompanyHibernateReader;
import com.internhub.data.companies.writers.impl.CompanyHibernateWriter;
import com.internhub.data.companies.writers.CompanyWriter;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.PositionScraper;
import com.internhub.data.positions.scrapers.strategies.InitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.GoogleInitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSMTStrategy;
import com.internhub.data.positions.writers.PositionWriter;
import com.internhub.data.positions.writers.impl.PositionHibernateWriter;
import com.internhub.data.models.Company;
import com.internhub.data.companies.scrapers.CompanyScraper;
import com.internhub.data.companies.scrapers.impl.RedditCompanyScraper;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSStrategy;
import com.internhub.data.positions.scrapers.IPositionScraper;

import com.internhub.data.selenium.MyWebDriver;
import com.internhub.data.selenium.MyWebDriverPool;
import org.apache.commons.cli.*;
import org.apache.commons.exec.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    static void initChromeDriver() {
        // Determine which Chrome driver file to use
        String driverName;
        if (OS.isFamilyWindows()) {
            driverName = "chromedriver.exe";
        } else if (OS.isFamilyMac()) {
            driverName = "chromedriver";
        } else if (OS.isFamilyUnix()) {
            driverName = "chromedriver_linux";
        } else {
            throw new RuntimeException("Unsupported OS detected.");
        }

        // Convert the driver file name to a path in our resources folder
        URL driver = Main.class.getClassLoader().getResource(driverName);
        if (driver == null) {
            throw new RuntimeException("Unable to find path to " + driverName + ".");
        }

        // Set the system property to tell Selenium which driver to use
        System.setProperty("webdriver.chrome.driver", driver.getPath());
    }

    private static void scrapeCompanies() {
        CompanyWriter companyWriter = new CompanyHibernateWriter();
        CompanyScraper companyScraper = new RedditCompanyScraper();
        companyWriter.save(companyScraper.fetch());
    }

    private static void scrapePositions() {
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

        List<Future<List<Position>>> futures = Lists.newArrayList();
        try {
            futures = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    private static void scrapePositions(String name) {
        CompanyReader companyReader = new CompanyHibernateReader();
        List<Company> companies = companyReader.getByName(name);
        if (companies.isEmpty()) {
            logger.error(String.format("Unable to find a company by the name of %s.", name));
        } else {
            assert(companies.size() == 1);
            scrapePositions(companies);
        }
    }

    private static void scrapePositions(List<Company> companies) {
        try (MyWebDriver driver = new MyWebDriver()) {
            PositionWriter positionWriter = new PositionHibernateWriter();
            IPositionScraper IPositionScraper = new PositionScraper(
                    new GoogleInitialLinkStrategy(),
                    new PositionBFSStrategy(driver.getDriver()));
            for (Company company : companies) {
                positionWriter.save(IPositionScraper.fetch(company));
            }
        }
    }

    public static void main(String[] args) {
        initChromeDriver();

        Options options = new Options();
        options.addOption("c", "companies", false, "scrape companies & populate companies table");
        options.addOption("p", "positions", false, "scrape positions & populate positions table");
        options.addOption("s", "specific", true, "scrape positions for a specified company");
        options.addOption("h", "help", false, "print help information");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("gradle run --args=", options, true);
                return;
            }
            if (line.hasOption("c")) {
                scrapeCompanies();
            }
            if (line.hasOption("p")) {
                scrapePositions();
            }
            if (line.hasOption("s")) {
                scrapePositions(line.getOptionValue("s"));
            }
        } catch (ParseException exp) {
            throw new RuntimeException(exp);
        }
    }
}
