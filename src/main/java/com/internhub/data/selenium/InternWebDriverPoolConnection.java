package com.internhub.data.selenium;

import org.openqa.selenium.WebDriver;

public class InternWebDriverPoolConnection implements AutoCloseable {
    private final InternWebDriver mDriverAdapter;
    private final InternWebDriverPool mPool;

    public InternWebDriverPoolConnection(InternWebDriver adapter, InternWebDriverPool pool) {
        mDriverAdapter = adapter;
        mPool = pool;
    }

    public WebDriver getDriver() {
        return mDriverAdapter.getDriver();
    }

    @Override
    public void close() {
        mPool.release(mDriverAdapter);
    }
}
