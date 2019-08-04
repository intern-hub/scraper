package com.internhub.data.positions.extractors;


import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.pages.Page;
import com.internhub.data.positions.verifiers.PositionVerifier;
import com.internhub.data.util.ScraperUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PositionExtractor {
    public class ExtractionResult {
        public Position position;
        public Collection<String> nextPositions = Lists.newArrayList();
    }

    private PositionVerifier mVerifier;

    public PositionExtractor() {
        mVerifier = new PositionVerifier();
    }

    public ExtractionResult extract(Page page, Company company) {
        ExtractionResult ret = new ExtractionResult();
        if (!isValidPage(page)) {
            return ret;
        }

        ret.position = tryGetPosition(page, company);
        ret.nextPositions = tryGetNextPositions(page, company);

        return ret;
    }

    private Position tryGetPosition(Page page, Company company) {
        Elements element = null;
        String currentLink = page.getLink();

        if (mVerifier.isPositionValid(currentLink, page.getBody())) {
            // In many cases, we will just be able to verify the
            // application from the initial page
            element = page.getBody();
        } else {
            // Otherwise, we check the iframes of the page to make sure
            // we didn't miss a nested application view
            for (Elements iframeBody : page.getIFrameBodies()) {
                if (mVerifier.isPositionValid(currentLink, iframeBody)) {
                    element = iframeBody;
                    break;
                }
            }
        }

        // Couldn't find anything, bail
        if(element == null) {
            return new Position();
        }

        // Get the correct information from the position and return it
        return mVerifier.getPosition(company, page.getLink(), element);
    }

    private Collection<String> tryGetNextPositions(Page page, Company company) {
        Set<String> ret = new HashSet<>();

        // Collect links from all page bodies including those of iframes
        List<Elements> anchorBundles = Lists.newArrayList();
        anchorBundles.add(page.getBody().select("a[href]"));
        for (Elements iframeBody : page.getIFrameBodies()) {
            anchorBundles.add(iframeBody.select("a[href]"));
        }

        for (Elements anchors : anchorBundles) {
            for (Element anchor : anchors) {
                String childLink = anchor.attr("href");
                if (!isAnchorLinkValid(childLink)) {
                    continue;
                }

                // Fix relative paths, etc.
                childLink = ScraperUtils.fixLink(childLink, page.getLink());

                // Make sure that the link can be turned into a valid URL
                URL childURL = ScraperUtils.makeURL(childLink, false);
                if (childURL == null || !isAppLinkValid(childURL, company)) {
                    continue;
                }
                ret.add(childLink);
            }
        }
        return ret;
    }

    private boolean isValidPage(Page page) {
        String pageSource = page.getSource().toLowerCase();
        for (String keyword : new String[]{"career", "job", "intern"}) {
            if (pageSource.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns if an application url is valid to look at
     */
    private boolean isAppLinkValid(URL url, Company company) {
        // Make sure we are either in the company's career website OR
        // we are on a job listings board
        if (!ScraperUtils.isInCompanyDomain(url, company.getWebsiteURL(), company.getAbbreviation()) &&
                !url.getHost().contains("taleo") &&
                !url.getHost().contains("jobvite") &&
                !url.getHost().contains("workday") &&
                !url.getHost().contains("greenhouse") &&
                !url.getHost().contains("icims")) {
            return false;
        }
        return true;
    }

    private boolean isAnchorLinkValid(String link) {
        // Throw out links that aren't actually valid
        if (link == null || link.length() <= 2 ||
                link.charAt(0) == '#' || link.startsWith("javascript:void") ||
                link.startsWith("mailto")) {
            return false;
        }

        // Don't bother with following links to files
        if (link.endsWith(".pdf") ||
                link.endsWith(".mp3") ||
                link.endsWith(".mp4") ||
                link.endsWith(".jpg") ||
                link.endsWith(".png")) {
            return false;
        }

        return true;
    }
}
