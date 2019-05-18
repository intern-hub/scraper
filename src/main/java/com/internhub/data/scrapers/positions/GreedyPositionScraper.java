package com.internhub.data.scrapers.positions;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.search.GoogleSearch;
import com.internhub.data.verifiers.PositionVerifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GreedyPositionScraper implements PositionScraper {
    private static final String INTERNSHIP_SEARCH_TERM = "%s internship apply";

    private static final int MAX_DEPTH = 4;
    private static final int MAX_ENTRY_LINKS = 5;
    private static final int MAX_TOTAL_LINKS = 100;
    private static final int PAGE_LOAD_DELAY = 2000;

    private static final Logger logger = LoggerFactory.getLogger(GreedyPositionScraper.class);

    private WebDriver m_driver;
    private GoogleSearch m_google;
    private PositionVerifier m_verifier;

    public GreedyPositionScraper() {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.addArguments(
                "--incognito",
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64 " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/70.0.3538.77 Safari/537.36"
        );
        this.m_driver = new ChromeDriver(options);
        this.m_google = new GoogleSearch();
        this.m_verifier = new PositionVerifier();
    }

    @Override
    public List<Position> fetch(Company company) throws MalformedURLException {
        // This information about the company will come in handy later
        String companyName = company.getName();
        String companyAbbrev = companyName.toLowerCase()
                .replace(" ", "")
                .replace(".", "")
                .replace("&", "");
        URL companyWebsite = new URL(company.getWebsite());

        // Create priority queue used to determine which links to crawl first
        List<Position> results = new ArrayList<>();
        PriorityQueue<CandidatePosition> frontier = new PriorityQueue<>(new CandidateHeuristicComparator());
        Set<String> visited = new HashSet<>();

        // Start with the first ${MAX_ENTRY_LINKS} links that Google finds for the company
        try {
            String searchTerm = String.format(INTERNSHIP_SEARCH_TERM, companyName);
            for (URL url : m_google.search(searchTerm, MAX_ENTRY_LINKS)) {
                if (isInCompanyDomain(url, companyWebsite, companyAbbrev)) {
                    String link = fixLink(url.toString(), null, null);
                    frontier.add(new CandidatePosition(link, 1));
                    visited.add(link);
                }
            }
        } catch (IOException ex) {
            logger.error("Initial search failed.", ex);
        }

        int numTotalLinks = 0;
        while (!frontier.isEmpty() && numTotalLinks < MAX_TOTAL_LINKS) {
            // Pop our best candidate off of the frontier
            CandidatePosition candidate = frontier.remove();

            // Convert the link to a URL representation so we can
            // extract the base of the link (e.g. <scheme>://<host>)
            String currentLink = candidate.getLink();
            URL currentURL;
            try {
                currentURL = new URL(currentLink);
            } catch (MalformedURLException ex) {
                logger.error(currentLink + " is malformed.", ex);
                continue;
            }
            String currentBase = currentURL.getProtocol() + "://" + currentURL.getHost();

            logger.info(String.format(
                    "[%d/%d] Visiting %s (depth = %d) ...",
                    numTotalLinks + 1, MAX_TOTAL_LINKS, currentLink, candidate.getDepth()));

            // Use Selenium to fetch the page and wait a bit for it to load
            m_driver.get(currentLink);
            try {
                 Thread.sleep(PAGE_LOAD_DELAY);
            } catch (InterruptedException ex) {
                logger.error("Could not wait for page to load.", ex);
            }
            String pageSource = m_driver.getPageSource();

            // Determine whether a page is explorable without parsing the HTML
            // by doing some primitive checks first
            String pageSourceLower = pageSource.toLowerCase();
            boolean explorable = false;
            for (String keyword : new String[] { "career", "job", "intern"}) {
                if (pageSourceLower.contains(keyword)) {
                    explorable = true;
                    break;
                }
            }
            if (!explorable)
                continue;

            Document document = Jsoup.parse(pageSource);
            Elements body = fixHTMLBody(document.select("body"));

            // Convert iframes to JSoup HTML elements
            List<WebElement> iframes = m_driver.findElements(By.tagName("iframe"));
            List<Elements> iframeBodies = new ArrayList<>();
            for (WebElement iframe : iframes) {
                m_driver.switchTo().frame(iframe);
                Document iframeHTML = Jsoup.parse(m_driver.getPageSource());
                Elements iframeBody = fixHTMLBody(iframeHTML.select("body"));
                iframeBodies.add(iframeBody);
                m_driver.switchTo().defaultContent();
            }

            Elements verified = null;
            if (m_verifier.isPositionValid(currentLink, body)) {
                // In many cases, we will just be able to verify the
                // application from the initial page
                verified = body;
            }
            else {
                // Otherwise, we check the iframes of the page to make sure
                // we didn't miss a nested application view
                for (Elements iframeBody : iframeBodies) {
                    if (m_verifier.isPositionValid(currentLink, iframeBody)) {
                        verified = iframeBody;
                        break;
                    }
                }
            }
            if (verified != null) {
                // If the link was able to be verified, then the verifier will have
                // data regarding the link (e.g position title, etc.)
                Position position = new Position();
                position.setLink(currentLink);
                position.setCompany(company);
                position.setTitle(m_verifier.getPositionTitle(currentLink, verified));
                position.setSeason(m_verifier.getPositionSeason(currentLink, verified));
                position.setYear(m_verifier.getPositionYear(currentLink, verified));
                position.setDegree(m_verifier.getPositionDegree(currentLink, verified));
                position.setLocation(m_verifier.getPositionLocation(currentLink, verified));
                results.add(position);
            }

            if (candidate.getDepth() < MAX_DEPTH) {
                // Collect links from all page bodies including those of iframes
                List<Elements> anchorBundles = new ArrayList<>();
                anchorBundles.add(body.select("a[href]"));
                for (Elements iframeBody : iframeBodies) {
                    anchorBundles.add(iframeBody.select("a[href]"));
                }

                for (Elements anchors : anchorBundles) {
                    for (Element anchor : anchors) {
                        String childLink = anchor.attr("href");

                        // Throw out links that aren't actually valid
                        if (childLink == null || childLink.length() <= 2 ||
                                childLink.charAt(0) == '#' || childLink.startsWith("javascript:void") ||
                                childLink.startsWith("mailto")) {
                            continue;
                        }

                        // Fix relative paths, etc.
                        childLink = fixLink(childLink, currentLink, currentBase);

                        // Make sure that the link can be transposed to a valid URL
                        URL childURL;
                        try {
                            childURL = new URL(childLink);
                        } catch (MalformedURLException ex) {
                            continue;
                        }

                        // Make sure we are either in the company's career website OR
                        // we are on a job listings board
                        if (!isInCompanyDomain(childURL, companyWebsite, companyAbbrev) &&
                                !childURL.getHost().contains("taleo") &&
                                !childURL.getHost().contains("jobvite") &&
                                !childURL.getHost().contains("workday") &&
                                !childURL.getHost().contains("greenhouse") &&
                                !childURL.getHost().contains("icims"))
                        {
                            continue;
                        }

                        // Don't bother with following links to images or PDFs
                        if (childLink.endsWith(".pdf") || childLink.endsWith(".jpg") || childLink.endsWith(".png")) {
                            continue;
                        }

                        // Ignore links that have already been visited
                        if (visited.contains(childLink)) {
                            continue;
                        }

                        frontier.add(new CandidatePosition(childLink, candidate.getDepth() + 1));
                        visited.add(childLink);
                    }
                }
            }

            numTotalLinks++;
        }

        return results;
    }

    private boolean isInCompanyDomain(URL url, URL companyURL, String companyAbbrev) {
        return url.getHost().equals(companyURL.getHost()) || companyURL.getHost().contains(companyAbbrev);
    }

    private Elements fixHTMLBody(Elements html) {
        Elements header = html.select("header");
        if (header != null) {
            header.remove();
        }
        Elements footer = html.select("footer");
        if (footer != null) {
            footer.remove();
        }
        return html;
    }

    private String fixLink(String link, String parent, String parentBase) {
        // Fix relative links
        if (link.charAt(0) == '/') {
            link = parentBase + link;
        }
        if (link.charAt(0) == '.') {
           if (parent.charAt(parent.length() - 1) == '/') {
               link = parent + link;
           }
           else {
               link = parent + '/' + link;
           }
        }

        // Trim ending slash in link
        if (link.charAt(link.length() - 1) == '/') {
            link = link.substring(0, link.length() - 1);
        }

        return link.trim();
    }
}
