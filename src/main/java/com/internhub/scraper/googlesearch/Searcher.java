package com.internhub.scraper.googlesearch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Searcher {
    private static final String SEARCH_URL =
            "https://www.google.com/search?hl=en&q=%s&num=%d&btnG=Google+Search&tbs=0&safe=off&tbm=";
    private static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0";

    public List<URL> search(String query, int count) throws IOException {
        List<URL> links = new ArrayList<>();

        // TODO:
        //  Make request to google with appropriate search terms,
        //  parse HTML using BS4, throw out useless results

        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String searchUrl = String.format(SEARCH_URL, encodedQuery, count);

        Document searchHtml = fetchPage(searchUrl);
        Elements searchArea = searchHtml.body().select("#search");
        Elements searchLinks;
        if (searchArea.isEmpty()) {
            searchHtml.body().select("#gbar").remove();
            searchLinks = searchHtml.body().select("a[href]");
        }
        else {
            searchLinks = searchArea.select("a[href]");
        }

        System.out.println(query + " ... " + count);
        for (Element anchor : searchLinks) {
            String link = anchor.attr("href");
            URL url = convertLink(link);
            if (url == null) {
                continue;
            }
            System.out.println(url);
        }

        return links;
    }

    private URL convertLink(String link) {
        try {
            URL url = new URL(link);
            String domain = url.getHost();
            if (domain.length() > 0 &&
                    (!domain.contains("google") || domain.contains("careers.google"))) {
                return url;
            }
            return null;
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    private Document fetchPage(String pageUrl) throws IOException {
        return Jsoup
                .connect(pageUrl)
                .userAgent(USER_AGENT)
                .get();
    }
}
