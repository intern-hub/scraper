package com.internhub.data.scrapers.impl;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.scrapers.Page;
import com.internhub.data.scrapers.PageScraper;
import com.internhub.data.scrapers.PositionScraper;
import com.internhub.data.scrapers.ScraperUtils;
import com.internhub.data.search.GoogleSearch;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class DefaultPositionScraper implements PositionScraper {
    private static final String INTERNSHIP_SEARCH_TERM = "%s internship apply";

    private static final int MAX_DEPTH = 4;
    private static final int TOP_N_GOOGLE_LINKS = 4;
    private static final int MAX_TOTAL_LINKS = 20;
    private static final int PAGE_LOAD_DELAY = 2000;

    private static final Logger logger = LoggerFactory.getLogger(DefaultPositionScraper.class);

    private WebDriver mDriver;
    private GoogleSearch mGoogle;

    public DefaultPositionScraper(WebDriver driver) {
        this.mDriver = driver;
        this.mGoogle = new GoogleSearch();
    }

    @Override
    public List<Position> fetch(Company company) {
        // Create priority queue used to determine which links to crawl first
        List<Position> results = Lists.newArrayList();
        PriorityQueue<CandidatePosition> candidates = new PriorityQueue<>(new CandidateComparator());
        Set<String> visited = new HashSet<>();

        // start by adding google results, then analyzing each link
        addGoogleResults(company, candidates, visited);

        // cap number of links to analyze
        int numTotalLinks = 0;
        while (!candidates.isEmpty() && numTotalLinks < MAX_TOTAL_LINKS) {
            // Get our best candidate
            CandidatePosition candidate = candidates.remove();
            String currentLink = candidate.getLink();

            logger.info(String.format(
                    "[%d/%d] Visiting %s (depth = %d) ...",
                    numTotalLinks + 1, MAX_TOTAL_LINKS, currentLink, candidate.getDepth()));

            // Get page with selenium
            Page page = getPage(currentLink);

            // Scrape page
            PageScraper scraper = new PageScraper();
            PageScraper.PageScrapeResult result = scraper.scrapePage(page, company);

            // Diagnostics on result position
            Position position = result.position;
            handlePosition(position, results, numTotalLinks);

            // only add more links if we haven't reached max depth yet
            if (candidate.getDepth() < MAX_DEPTH) {
                Collection<String> nextPositions = result.nextPositions;
                for (String link : nextPositions) {
                    if (visited.contains(link)) {
                        continue;
                    }
                    candidates.add(new CandidatePosition(link, candidate.getDepth() + 1));
                    visited.add(link);
                }
            }
            numTotalLinks++;
        }
        return results;
    }

    /**
     * Returns a processed page with all necessary information describing a web page
     */
    private Page getPage(String link) {
        // Use Selenium to fetch the page and wait a bit for it to load
        try {
            mDriver.get(link);
            Thread.sleep(PAGE_LOAD_DELAY);
        } catch (TimeoutException ex) {
            logger.error("Skipping page due to timeout issues.", ex);
            return null;
        } catch (InterruptedException ex) {
            logger.error("Could not wait for page to load.", ex);
            return null;
        }

        Page page = new Page(mDriver.getPageSource(), link);
        page.process(mDriver);
        return page;
    }


    /**
     * Add google results to list (number of results depends on TOP_N_GOOGLE_LINKS). Marks links visited as well.
     * <p>
     * // TODO refactor
     */
    private void addGoogleResults(Company company, Collection<CandidatePosition> candidates, Set<String> visited) {
        // Start by finding TOP_N_GOOGLE_LINKS links that Google finds for the company and adding to list
        String searchTerm = String.format(INTERNSHIP_SEARCH_TERM, company.getName());
        for (URL url : mGoogle.search(searchTerm, TOP_N_GOOGLE_LINKS)) {
            if (ScraperUtils.isInCompanyDomain(url, company.getWebsiteURL(), company.getAbbreviation())) {
                String link = ScraperUtils.fixLink(url.toString());
                candidates.add(new CandidatePosition(link, 1));
                visited.add(link);
            }
        }
    }

    /**
     * Handles position result
     */
    private void handlePosition(Position position, List<Position> results, int numTotalLinks) {
        if (position != null) {
            results.add(position);
            logger.info(String.format("[%d/%d] Identified valid position.", numTotalLinks + 1, MAX_TOTAL_LINKS));
            logger.info(String.format("[%d/%d] Title is %s.", numTotalLinks + 1, MAX_TOTAL_LINKS, position.getTitle()));
            logger.info(String.format("[%d/%d] Season & year is %s %d.", numTotalLinks + 1, MAX_TOTAL_LINKS, position.getSeason(), position.getYear()));
            logger.info(String.format("[%d/%d] Location is %s.", numTotalLinks + 1, MAX_TOTAL_LINKS, position.getLocation()));
            logger.info(String.format("[%d/%d] Minimum degree is %s.", numTotalLinks + 1, MAX_TOTAL_LINKS, position.getDegree()));
        } else {
            logger.info(String.format("[%d/%d] Unable to find position.", numTotalLinks + 1, MAX_TOTAL_LINKS));
        }
    }
}
