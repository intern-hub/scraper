package com.internhub.data.selenium;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;


public class InternWebDriverPool implements AutoCloseable {
    private final static int DEFAULT_DRIVERS = 10;

    private final Semaphore mAvailable;
    private final BlockingQueue<InternWebDriver> mDrivers;

    public InternWebDriverPool() {
        this(DEFAULT_DRIVERS);
    }

    public InternWebDriverPool(int numDrivers) {
        mAvailable = new Semaphore(numDrivers, true);
        mDrivers = new ArrayBlockingQueue<InternWebDriver>(numDrivers);
    }

    public InternWebDriverPoolConnection acquire() throws InterruptedException {
        mAvailable.acquire();
        InternWebDriver driver;
        if (mDrivers.isEmpty()) {
            driver = new InternWebDriver();
        } else {
            driver = mDrivers.take();
        }

        return new InternWebDriverPoolConnection(driver, this);
    }

    void release(InternWebDriver driver) {
        mAvailable.release();
        mDrivers.offer(driver);
    }

    public void close() {
        for (InternWebDriver driver : mDrivers) {
            driver.close();
        }
    }
}
