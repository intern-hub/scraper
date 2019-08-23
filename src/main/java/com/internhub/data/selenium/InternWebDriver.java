package com.internhub.data.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.TimeUnit;

public class InternWebDriver implements AutoCloseable {
    private WebDriver mDriver;

    public InternWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.addArguments(
                "start-maximized",
                "enable-automation",
                "--incognito",
                "--no-sandbox",
                "--disable-extensions",
                "--disable-gpu",
                "--disable-infobars",
                "--disable-dev-shm-usage",
                "--disable-browser-side-navigation",
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64 " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/70.0.3538.77 Safari/537.36"
        );
        this.mDriver = new ChromeDriver(options);
        mDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        mDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    public WebDriver getDriver() {
        return mDriver;
    }

    public void close() {
        mDriver.quit();
    }
}
