package com.internhub.data.selenium;

import org.openqa.selenium.WebDriver;

public class MyWebDriverPoolWrapper implements AutoCloseable {
    private final MyWebDriver mDriverAdapter;
    private final MyWebDriverPool mPool;

    public MyWebDriverPoolWrapper(MyWebDriver adapter, MyWebDriverPool pool) {
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
