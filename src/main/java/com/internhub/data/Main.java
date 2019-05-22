package com.internhub.data;

import com.internhub.data.managers.CompanyManager;
import com.internhub.data.managers.PositionManager;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.scrapers.companies.CompanyScraper;
import com.internhub.data.scrapers.companies.RedditCompanyScraper;
import com.internhub.data.scrapers.positions.GreedyPositionScraper;
import com.internhub.data.scrapers.positions.PositionScraper;

import org.apache.commons.cli.*;
import org.apache.commons.exec.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void initChromeDriver() {
        // Determine which Chrome driver file to use
        String driverName;
        if (OS.isFamilyWindows()) {
            driverName = "chromedriver.exe";
        }
        else if (OS.isFamilyMac()) {
            driverName = "chromedriver";
        }
        else if (OS.isFamilyUnix()) {
            driverName = "chromedriver_linux";
        }
        else {
            throw new RuntimeException("Unsupported OS detected.");
        }

        // Convert the driver file name to a path in our resources folder
        URL driver =  Main.class.getClassLoader().getResource(driverName);
        if (driver == null) {
            throw new RuntimeException("Unable to find path to " + driverName + ".");
        }

        // Set the system property to tell Selenium which driver to use
        System.setProperty("webdriver.chrome.driver", driver.getPath());
    }

    private static void scrapeCompanies() {
        CompanyManager companyManager = new CompanyManager();
        CompanyScraper companyScraper = new RedditCompanyScraper();
        companyManager.bulkUpdate(companyScraper.fetch());
    }

    private static void scrapePositions() {
        CompanyManager companyManager = new CompanyManager();
        PositionManager positionManager = new PositionManager();
        PositionScraper positionScraper = new GreedyPositionScraper();
        for (Company company : companyManager.selectAll()) {
            try {
                positionManager.bulkUpdate(positionScraper.fetch(company));
            } catch (MalformedURLException ex) {
                logger.error(
                        String.format("Company %s (%d) has an invalid website: %s",
                                company.getName(),
                                company.getId(),
                                company.getWebsite()
                        ),
                        ex
                );
            }
        }
    }

    public static void main(String[] args) {
        initChromeDriver();

        Options options = new Options();
        options.addOption("c", "companies",  false,"scrape companies & populate companies table");
        options.addOption("p", "positions", false,"scrape positions & populate positions table");
        options.addOption("h", "help", false,"print help information");

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
        } catch(ParseException exp) {
            throw new RuntimeException(exp);
        }
    }
}
