package com.internhub.scraper.companies;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.SearchPaginator;
import net.dean.jraw.references.SubredditReference;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import java.util.*;

public class CompanyScraper {
    private static final String REDDIT_CLIENT_ID = "WlcJSnReHivl-Q";
    private static final String REDDIT_CLIENT_SECRET = "ohxqq8KaDWMlAsWHPm-u-lOCDUw";

    private RedditClient m_reddit;

    public CompanyScraper() {
        // Setup the Reddit client
        UserAgent userAgent = new UserAgent("bot", "com.internhub.scraper", "1.0.0", "dmhacker");
        NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
        this.m_reddit = OAuthHelper.automatic(networkAdapter, Credentials.userless(REDDIT_CLIENT_ID, REDDIT_CLIENT_SECRET, UUID.randomUUID()));
    }

    public Set<Company> fetch() {
        Set<Company> companies = new HashSet<>();
        scrapeReddit(companies);
        return companies;
    }

    private void scrapeReddit(Set<Company> companies) {
        // Find all official salary sharing threads in /r/cscareerquestions
        SubredditReference subreddit = m_reddit.subreddit("cscareerquestions");
        SearchPaginator paginator = subreddit.search().query("Salary Sharing thread").build();
        for (Listing<Submission> submissions : paginator) {
            for (Submission submission : submissions) {
                if (submission.getTitle().startsWith("[OFFICIAL] Salary Sharing thread")) {
                    scrapeRedditSubmission(companies, submission.getId());
                }
            }
        }
    }

    private void scrapeRedditSubmission(Set<Company> companies, String submissionId) {
        // For each salary sharing thread/submission, find all top-level region comments
        RootCommentNode root = m_reddit.submission(submissionId).comments();
        for (CommentNode node : root.getReplies()) {
            String region = node.getSubject().getBody();
            // Stick to just looking at US-based comments
            if (region != null && region.contains("Region -") && region.contains("US")) {
                for (CommentNode child : ((List<CommentNode>) node.getReplies())) {
                    scrapeRedditComment(companies, child.getSubject().getBody());
                }
            }
        }
    }

    private void scrapeRedditComment(Set<Company> companies, String comment) {
        // Clean up the comment by removing formatting
        String scomment = comment
                .replaceAll("**", "") // Strip bolded words
                .replaceAll(" *", "") // Strip bullet points
                .trim();

    }
}
