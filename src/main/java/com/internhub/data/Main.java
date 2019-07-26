package com.internhub.data;

import com.internhub.data.companies.readers.CompanyReader;
import com.internhub.data.companies.readers.impl.CompanyHibernateReader;
import com.internhub.data.companies.writers.impl.CompanyHibernateWriter;
import com.internhub.data.companies.writers.CompanyWriter;
import com.internhub.data.positions.scrapers.SearchPositionScraper;
import com.internhub.data.positions.scrapers.strategies.impl.GoogleInitialLinkStrategy;
import com.internhub.data.positions.writers.PositionWriter;
import com.internhub.data.positions.writers.impl.PositionHibernateWriter;
import com.internhub.data.models.Company;
import com.internhub.data.companies.scrapers.CompanyScraper;
import com.internhub.data.companies.scrapers.impl.RedditCompanyScraper;
import com.internhub.data.positions.scrapers.strategies.impl.ShallowSearchPositionStrategy;
import com.internhub.data.positions.scrapers.PositionScraper;

import com.internhub.data.selenium.CloseableWebDriverAdapter;
import org.apache.commons.cli.*;
import org.apache.commons.exec.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;

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
        try (CloseableWebDriverAdapter driverAdapter = new CloseableWebDriverAdapter()) {
            CompanyReader companyReader = new CompanyHibernateReader();
            PositionWriter positionWriter = new PositionHibernateWriter();
            PositionScraper positionScraper = new SearchPositionScraper(
                    new GoogleInitialLinkStrategy(),
                    new ShallowSearchPositionStrategy(driverAdapter.getDriver()));
            for (Company company : companyReader.getAll()) {
                positionWriter.save(positionScraper.fetch(company));
            }
        }
    }

    private static void scrapePositions(String name) {
        try (CloseableWebDriverAdapter driverAdapter = new CloseableWebDriverAdapter()) {
            CompanyReader companyReader = new CompanyHibernateReader();
            PositionWriter positionWriter = new PositionHibernateWriter();
            PositionScraper positionScraper = new SearchPositionScraper(
                    new GoogleInitialLinkStrategy(),
                    new ShallowSearchPositionStrategy(driverAdapter.getDriver()));
            List<Company> companies = companyReader.getByName(name);
            if (companies.isEmpty()) {
                logger.error(String.format("Company %s does not exist in the database.", name));
            } else {
                positionWriter.save(positionScraper.fetch(companies.get(0)));
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
