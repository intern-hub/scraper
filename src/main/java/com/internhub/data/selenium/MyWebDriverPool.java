package com.internhub.data.selenium;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;


public class MyWebDriverPool implements AutoCloseable {
    private final static int MAX_DRIVERS = 10;
    private final Semaphore mAvailable = new Semaphore(MAX_DRIVERS, true);
    private final BlockingQueue<MyWebDriver> mDrivers = new ArrayBlockingQueue<MyWebDriver>(MAX_DRIVERS);

    public MyWebDriverPoolWrapper aquire() throws InterruptedException {
        mAvailable.acquire();
        MyWebDriver driver;
        if (mDrivers.isEmpty()) {
            driver = new MyWebDriver();
        } else {
            driver = mDrivers.take();
        }

        return new MyWebDriverPoolWrapper(driver, this);
    }

    void release(MyWebDriver driver) {
        mAvailable.release();
        mDrivers.offer(driver);
    }

    public void close() {
        for (MyWebDriver driver : mDrivers) {
            driver.close();
        }
    }
}
