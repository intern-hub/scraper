package com.internhub.data.positions.pages;

import com.google.common.collect.Lists;
import com.internhub.data.util.ScraperUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Page {
    private static final Logger logger = LoggerFactory.getLogger(Page.class);
    private static final int MAX_IFRAME_WAIT = 3;

    private String mLink;
    private String mSource;
    private Document mDocument;
    private List<Elements> mIFrameBodies;
    private Elements mBody;

    public Page(String source, String link) {
        mSource = source;
        mLink = link;

        mIFrameBodies = Lists.newArrayList();
        mDocument = null;
        mBody = null;
    }

    public void process(WebDriver driver) {
        mDocument = Jsoup.parse(mSource);
        mBody = ScraperUtils.fixHTMLBody(mDocument.select("body"));
        processIFrames(driver);
    }

    private void processIFrames(WebDriver driver) {
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        for (WebElement iframe : iframes) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, MAX_IFRAME_WAIT);
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframe));
            } catch (TimeoutException ex) {
                logger.error(String.format("Unable to switch to iframe due to timeout. %s", iframe));
                continue;
            }

            Document iframeHTML = Jsoup.parse(driver.getPageSource());
            Elements iframeBody = ScraperUtils.fixHTMLBody(iframeHTML.select("body"));
            mIFrameBodies.add(iframeBody);
            driver.switchTo().defaultContent();
        }
    }

    public String getSource() {
        return mSource;
    }

    public List<Elements> getIFrameBodies() {
        return mIFrameBodies;
    }

    public Elements getBody() {
        return mBody;
    }

    public String getLink() { return mLink; }
}
