package com.internhub.data;

import com.internhub.data.companies.readers.ICompanyReader;
import com.internhub.data.companies.readers.impl.CompanyHibernateReader;
import com.internhub.data.companies.scrapers.ICompanyScraper;
import com.internhub.data.companies.scrapers.impl.RedditCompanyScraper;
import com.internhub.data.companies.writers.ICompanyWriter;
import com.internhub.data.companies.writers.impl.CompanyHibernateWriter;
import com.internhub.data.companies.writers.impl.CompanyStreamWriter;
import com.internhub.data.models.Company;
import com.internhub.data.positions.scrapers.ExecutablePositionScraper;
import com.internhub.data.positions.scrapers.IPositionScraper;
import com.internhub.data.positions.scrapers.strategies.impl.GoogleInitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.impl.PositionBFSStrategy;
import com.internhub.data.positions.writers.IPositionWriter;
import com.internhub.data.positions.writers.impl.PositionHibernateWriter;
import com.internhub.data.positions.writers.impl.PositionStreamWriter;
import com.internhub.data.selenium.InternWebDriverPool;
import com.internhub.data.util.SeleniumUtils;
import org.apache.commons.cli.*;
import org.apache.tools.ant.types.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void scrapeCompanies(boolean dryRun) {
        ICompanyScraper companyScraper = new RedditCompanyScraper();
        ICompanyWriter companyWriter = dryRun ?
                new CompanyStreamWriter(System.out) : new CompanyHibernateWriter();
        companyWriter.save(companyScraper.fetch());
    }

    private static void scrapePositions(boolean dryRun) {
        ICompanyReader companyReader = new CompanyHibernateReader();
        scrapePositions(companyReader.getAll(), dryRun);
    }

    private static void scrapePositions(String names, boolean dryRun) {
        ICompanyReader companyReader = new CompanyHibernateReader();
        List<Company> companies = Arrays.stream(names.split(","))
                .map(String::trim)
                .map(companyReader::getByName)
                .filter((c) -> !c.isEmpty())
                .map((c) -> c.get(0))
                .collect(Collectors.toList());
        scrapePositions(companies, dryRun);
    }

    private static void scrapePositions(List<Company> companies, boolean dryRun) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(12);
        IPositionWriter writer = dryRun ?
                new PositionStreamWriter(System.out) : new PositionHibernateWriter();
        try (InternWebDriverPool pool = new InternWebDriverPool(6)) {
            CountDownLatch latch = new CountDownLatch(companies.size());
            for (Company company : companies) {
                IPositionScraper scraper = new ExecutablePositionScraper(new GoogleInitialLinkStrategy(),
                        new PositionBFSStrategy(pool), executor);
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
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("Count down latch failed.", e);
            }
        }
        executor.shutdownNow();
    }

    public static void main(String[] args) {
        SeleniumUtils.initChromeDriver();

        // This fixes an issue with Java running in Docker. For some reason,
        // the JVM is not splitting CLI arguments by space.
        if (args.length == 1) {
            args = Commandline.translateCommandline(args[0]);
        }

        Options options = new Options();
        options.addOption("c", "companies", false, "scrape all possible companies");
        options.addOption("p", "positions", false, "scrape positions for all companies in the database");
        options.addOption("s", "specific", true, "scrape positions for a comma-delimited list of companies");
        options.addOption("d", "dry-run", false, "print but do not save any scraped positions or companies");
        options.addOption("h", "help", false, "print help information");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("gradle run --args=", options, true);
                return;
            }

            boolean dryRun = line.hasOption("d");
            if (line.hasOption("c")) {
                scrapeCompanies(dryRun);
            }
            if (line.hasOption("p")) {
                scrapePositions(dryRun);
            }
            if (line.hasOption("s")) {
                scrapePositions(line.getOptionValue("s"), dryRun);
            }
        } catch (ParseException exp) {
            throw new RuntimeException(exp);
        }
    }
}
