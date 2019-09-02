package com.internhub.data;

import com.internhub.data.companies.readers.ICompanyReader;
import com.internhub.data.companies.readers.impl.CompanyHibernateReader;
import com.internhub.data.companies.writers.impl.CompanyHibernateWriter;
import com.internhub.data.companies.writers.ICompanyWriter;
import com.internhub.data.positions.scrapers.ScheduledPositionScraper;
import com.internhub.data.positions.scrapers.IPositionScraper;
import com.internhub.data.positions.scrapers.strategies.impl.GoogleInitialLinkStrategy;
import com.internhub.data.models.Company;
import com.internhub.data.companies.scrapers.ICompanyScraper;
import com.internhub.data.companies.scrapers.impl.RedditCompanyScraper;

import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSStrategy;
import com.internhub.data.positions.writers.IPositionWriter;
import com.internhub.data.positions.writers.impl.PositionHibernateWriter;
import com.internhub.data.selenium.InternWebDriverPool;
import org.apache.commons.cli.*;
import org.apache.commons.exec.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
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
            driverName = "chromedriver_macos";
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
        ICompanyWriter companyWriter = new CompanyHibernateWriter();
        ICompanyScraper companyScraper = new RedditCompanyScraper();
        companyWriter.save(companyScraper.fetch());
    }

    private static void scrapePositions() {
        ICompanyReader companyReader = new CompanyHibernateReader();
        scrapePositions(companyReader.getAll());
    }

    private static void scrapePositions(String nameString) {
        ICompanyReader companyReader = new CompanyHibernateReader();
        List<Company> companies = Arrays.stream(nameString.split(","))
                .map(companyReader::getByName)
                .filter((c) -> !c.isEmpty())
                .map((c) -> c.get(0))
                .collect(Collectors.toList());
        scrapePositions(companies);
    }

    private static void scrapePositions(List<Company> companies) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(12);
        CountDownLatch latch = new CountDownLatch(companies.size());
        try (InternWebDriverPool pool = new InternWebDriverPool(6)) {
            for (Company company : companies) {
                IPositionScraper scraper = new ScheduledPositionScraper(new GoogleInitialLinkStrategy(),
                        new PositionBFSStrategy(pool), executor);
                IPositionWriter writer = new PositionHibernateWriter();
                scraper.scrape(company, (position) -> {
                    if (position != null) {
                        writer.save(position);
                    }
                    else {
                        logger.info(String.format("%s finished. Waiting on %d remaining companies.",
                                company.getName(), latch.getCount() - 1));
                        latch.countDown();
                    }
                });
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Count down latch failed.", e);
        }
        executor.shutdownNow();
    }

    public static void main(String[] args) {
        initChromeDriver();

        Options options = new Options();
        options.addOption("c", "companies", false, "scrape all possible companies");
        options.addOption("p", "positions", false, "scrape positions for all companies in the database");
        options.addOption("s", "specific", true, "scrape positions for a comma-delimited list of companies");
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
