package com.internhub.data;

import com.internhub.data.managers.CompanyManager;
import com.internhub.data.managers.PositionManager;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.scrapers.CompanyScraper;
import com.internhub.data.scrapers.MTScraper;
import com.internhub.data.scrapers.impl.RedditCompanyScraper;
import com.internhub.data.scrapers.impl.DefaultPositionScraper;
import com.internhub.data.scrapers.PositionScraper;

import com.internhub.data.scrapers.impl.mt.MTPositionScraper;
import com.internhub.data.selenium.CloseableWebDriverAdapter;
import org.apache.commons.cli.*;
import org.apache.commons.exec.OS;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
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
        CompanyManager companyManager = new CompanyManager();
        CompanyScraper companyScraper = new RedditCompanyScraper();
        companyManager.bulkUpdate(companyScraper.fetch());
    }

    private static void scrapePositions() {
        CompanyManager companyManager = new CompanyManager();
        List<Company> companies = companyManager.selectAll();
        scrapePositions0(companies);
    }

    private static void scrapePositions(String companyName) {
        CompanyManager companyManager = new CompanyManager();
        List<Company> companies = companyManager.selectByName(companyName);
        if (companies.isEmpty()) {
            logger.error(String.format("Company %s does not exist in the database.", companyName));
            return;
        }
        scrapePositions0(companies);
    }

    private static void scrapePositions0(List<Company> companies) {
        MTScraper scraper = new MTPositionScraper();
        scraper.load(companies);
        scraper.scrape();
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
