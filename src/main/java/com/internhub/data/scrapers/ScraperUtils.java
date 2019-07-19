package com.internhub.data.scrapers;

import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class ScraperUtils {

    public static final Logger logger = LoggerFactory.getLogger(ScraperUtils.class);

    public static boolean isInCompanyDomain(URL url, URL companyURL, String companyAbbrev) {
        return url.getHost().equals(companyURL.getHost()) || url.getHost().contains(companyAbbrev);
    }

    public static Elements fixHTMLBody(Elements html) {
        return removeFromHTMLBody(html, "header", "navigation", "footer", ".footer");
    }

    public static Elements removeFromHTMLBody(Elements html, String... queries) {
        for (String query : queries) {
            Elements scrap = html.select(query);
            if (scrap != null)
                scrap.remove();
        }
        return html;
    }

    public static String getBase(String link) {
        URL url = makeURL(link);
        if (url == null)
            return null;
        return url.getProtocol() + "://" + url.getHost();
    }

    public static URL makeURL(String link) {
        try {
            return new URL(link);
        } catch (MalformedURLException ex) {
            logger.info(String.format("Link %s is malformed", link));
            return null;
        }
    }

    public static String fixLink(String link, String parent) {
        String parentBase = getBase(parent);
        // Fix relative links
        if (link.charAt(0) == '/') {
            link = parentBase + link;
        }
        if (link.charAt(0) == '.') {
            if (parent.charAt(parent.length() - 1) == '/') {
                link = parent + link;
            } else {
                link = parent + '/' + link;
            }
        }
        return fixLink(link);
    }

    public static String fixLink(String link) {
        // Trim ending slash in link
        if (link.charAt(link.length() - 1) == '/') {
            link = link.substring(0, link.length() - 1);
        }

        // Remove query parameters and hashbang
        if (link.contains("?")) {
            link = link.split("\\?")[0];
        }
        if (link.contains("#")) {
            link = link.split("#")[0];
        }

        return link.trim();
    }
}
