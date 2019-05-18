package com.internhub.data.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GoogleSearch {
    private static final String SEARCH_URL =
            "https://www.google.com/search?hl=en&q=%s&num=%d&btnG=Google+Search&tbs=0&safe=off&tbm=";
    private static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0";
    private static final int PAUSE_DELAY_MIN = 1000;
    private static final int PAUSE_DELAY_MAX = 3000;

    private Random random;

    public GoogleSearch() {
        this.random = new Random();
    }

    public List<URL> search(String query, int count) throws IOException {
        List<URL> links = new ArrayList<>();

        // Encode the query into a URL-friendly string
        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            // Will never happen, UTF-8 is always a valid encoding
            throw new RuntimeException(ex);
        }

        // NOTE: We add 2 to the count to account for possible
        // job posting and related search term sections
        String searchUrl = String.format(SEARCH_URL, encodedQuery, count + 2);

        // Fetch the page and identify the area with search results
        Document searchHtml = fetchPage(searchUrl);
        Elements searchArea = searchHtml.body().select("#search");

        // Find subsections within the search area that actually contain results
        List<Elements> specificAreas = new ArrayList<>();
        for (Element element : searchArea.select("h2")) {
           if (element.text().contains("Web")) {
              specificAreas.add(element.parent().select("a[href]"));
           }
        }

        // Find all anchors within the search areas and add them
        // to our result list if they are valid URLs
        for (Elements anchors : specificAreas) {
            for (Element anchor : anchors) {
                if (links.size() >= count) {
                    break;
                }
                String link = anchor.attr("href");
                try {
                    links.add(constructURL(link));
                } catch (MalformedURLException ex) {
                    // Malformed links should simply be discarded (#, mailto:, etc.)
                }
            }
        }

        // We sleep at a random delay between the given minimum and maximum
        // This is done to fool Google into thinking we are more of a human actor than not
        try {
            Thread.sleep(PAUSE_DELAY_MIN + random.nextInt(PAUSE_DELAY_MAX - PAUSE_DELAY_MIN + 1));
        }
        catch (InterruptedException ex) {
            // Not that important, can be ignored
        }

        return links;
    }

    private URL constructURL(String link) throws MalformedURLException {
        URL url = new URL(link);
        String domain = url.getHost();
        if (domain.length() == 0) {
            throw new MalformedURLException();
        }
        return url;
    }

    private Document fetchPage(String pageUrl) throws IOException {
        return Jsoup.connect(pageUrl).userAgent(USER_AGENT).get();
    }
}
