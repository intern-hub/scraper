package com.internhub.data.scrapers.positions;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.search.GoogleSearch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GreedyPositionScraper implements PositionScraper {
    private static int MAX_DEPTH = 4;
    private static int MAX_ENTRY_LINKS = 5;
    private static int MAX_TOTAL_LINKS = 100;

    private WebDriver m_driver;
    private GoogleSearch m_google;

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
    }

    @Override
    public List<Position> fetch(Company company) {
        // This information about the company will come in handy later
        String companyName = company.getName();
        String companyAbbrev = companyName.toLowerCase()
                .replace(" ", "")
                .replace(".", "")
                .replace("&", "");
        URL companyWebsite;
        try {
            companyWebsite = new URL(company.getWebsite());
        } catch (MalformedURLException mue) {
            throw new RuntimeException(mue);
        }

        // Create priority queue used to determine which links to crawl first
        PriorityQueue<CandidatePosition> frontier = new PriorityQueue<>(new CandidateHeuristicComparator());
        Set<String> visited = new HashSet<>();

        // Start with the first ${MAX_ENTRY_LINKS} links that Google finds for the company
        try {
           for (URL url : m_google.search(company.getName() + " internship apply", MAX_ENTRY_LINKS)) {
               if (isInCompanyDomain(url, companyWebsite, companyAbbrev)) {
                    String link = fixLink(url.toString(), null, null);
                    frontier.add(new CandidatePosition(link, 1));
                    visited.add(link);
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        List<Position> results = new ArrayList<>();
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
            } catch (MalformedURLException mue) {
                // TODO: Log that we got an invalid URL
                continue;
            }
            String currentBase = currentURL.getProtocol() + "://" + currentURL.getHost();

            // TODO: Replace println calls with proper logging
            System.out.println("[" + numTotalLinks + "] Visiting " +
                    currentLink + " (" + candidate.getDepth() + ") ...");

            // Use Selenium to fetch the page and wait a bit for it to load
            m_driver.get(currentLink);
            try {
                 Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
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

            // TODO: Check if body is a valid application
            // TODO: Switch to iframes and repeat the process with those

            if (candidate.getDepth() < MAX_DEPTH) {
                List<Elements> anchorBundles = new ArrayList<>();
                anchorBundles.add(body.select("a[href]"));
                // TODO: Collect links for iframes as well

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
                        } catch (MalformedURLException mue) {
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
