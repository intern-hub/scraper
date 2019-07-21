package com.internhub.data.positions.scrapers.impl;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.pages.Page;
import com.internhub.data.positions.pages.PageScraper;
import com.internhub.data.positions.scrapers.QueuedPositionScraper;
import com.internhub.data.positions.scrapers.candidates.CandidateComparator;
import com.internhub.data.positions.scrapers.candidates.DepthAwareCandidate;
import com.internhub.data.util.ScraperUtils;
import com.internhub.data.search.GoogleSearch;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class DepthAwarePositionScraper extends QueuedPositionScraper<DepthAwareCandidate> {
    private static final String INTERNSHIP_SEARCH_TERM = "%s internship apply";

    private static final int MAX_DEPTH = 4;
    private static final int TOP_N_GOOGLE_LINKS = 4;
    private static final int MAX_TOTAL_LINKS = 100;
    private static final int PAGE_LOAD_DELAY = 2000;

    private static final Logger logger = LoggerFactory.getLogger(DepthAwarePositionScraper.class);

    private WebDriver mDriver;
    private GoogleSearch mGoogle;
    private int mTotalLinks;
    private PageScraper mPageScraper;
    private PageScraper.PageScrapeResult mLastPageResult;

    public DepthAwarePositionScraper(WebDriver driver) {
        super(new PriorityQueue<>(new CandidateComparator<>()));

        mDriver = driver;
        mGoogle = new GoogleSearch();
        mPageScraper = new PageScraper();
    }

    @Override
    protected Collection<DepthAwareCandidate> getInitialCandidates(Company company) {
        // Start by finding TOP_N_GOOGLE_LINKS links that Google finds for the company and adding to list
        List<DepthAwareCandidate> candidates = Lists.newArrayList();
        String searchTerm = String.format(INTERNSHIP_SEARCH_TERM, company.getName());
        for (URL url : mGoogle.search(searchTerm, TOP_N_GOOGLE_LINKS)) {
            if (ScraperUtils.isInCompanyDomain(url, company.getWebsiteURL(), company.getAbbreviation())) {
                String link = ScraperUtils.fixLink(url.toString());
                candidates.add(new DepthAwareCandidate(link, 1));
            }
        }
        return candidates;
    }

    @Override
    protected Position getPositionFromCandidate(Company company, DepthAwareCandidate candidate) {
        logger.info(String.format(
                "[%d/%d] Visiting %s (depth = %d) ...",
                mTotalLinks + 1, MAX_TOTAL_LINKS, candidate.getLink(), candidate.getDepth()));

        Page page = getPage(candidate.getLink());
        mLastPageResult = mPageScraper.scrapePage(page, company);

        mTotalLinks++;

        reportPosition(mLastPageResult.position);

        return mLastPageResult.position;
    }

    @Override
    protected Collection<DepthAwareCandidate> getNextCandidates(Company company, DepthAwareCandidate candidate) {
        if (candidate.getDepth() >= MAX_DEPTH) {
            return Lists.newArrayList();
        }

        return mLastPageResult.nextPositions.stream()
                .filter((link) -> !visited.contains(link))
                .map((link) -> new DepthAwareCandidate(link, candidate.getDepth() + 1))
                .collect(Collectors.toList());
    }

    @Override
    protected void prepareFetch() {
        mTotalLinks = 0;
    }

    @Override
    protected boolean shouldKeepFetching() {
        return mTotalLinks < MAX_TOTAL_LINKS;
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
     * Logs info about found position
     */
    private void reportPosition(Position position) {
        if (position != null) {
            logger.info(String.format("[%d/%d] Identified valid position.", mTotalLinks, MAX_TOTAL_LINKS));
            logger.info(String.format("[%d/%d] Title is %s.", mTotalLinks, MAX_TOTAL_LINKS, position.getTitle()));
            logger.info(String.format("[%d/%d] Season & year is %s %d.", mTotalLinks, MAX_TOTAL_LINKS, position.getSeason(), position.getYear()));
            logger.info(String.format("[%d/%d] Location is %s.", mTotalLinks, MAX_TOTAL_LINKS, position.getLocation()));
            logger.info(String.format("[%d/%d] Minimum degree is %s.", mTotalLinks, MAX_TOTAL_LINKS, position.getDegree()));
        } else {
            logger.info(String.format("[%d/%d] Unable to find position.", mTotalLinks, MAX_TOTAL_LINKS));
        }
    }
}
