package com.internhub.data.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class CloseableWebDriverAdapter implements AutoCloseable {
    private WebDriver mDriver;

    public CloseableWebDriverAdapter() {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.addArguments(
                "--incognito",
                "--no-sandbox",
                "--disable-extensions",
                "--disable-gpu",
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64 " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/70.0.3538.77 Safari/537.36"
        );
        this.mDriver = new ChromeDriver(options);
    }

    public WebDriver getDriver() {
        return mDriver;
    }

    public void close() {
        mDriver.quit();
    }
}
