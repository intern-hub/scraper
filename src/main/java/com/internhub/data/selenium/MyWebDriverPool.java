package com.internhub.data.selenium;

import org.openqa.selenium.WebDriver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class MyWebDriverPool implements AutoCloseable {
    private final BlockingQueue<MyWebDriver> mDrivers;

    public MyWebDriverPool() {
        this(5);
    }

    public MyWebDriverPool(int size) {
        mDrivers = new ArrayBlockingQueue<>(size);
        for (int i = 0; i < size; i++) {
            mDrivers.add(new MyWebDriver());
        }
    }

    public MyWebDriverPoolWrapper aquire() throws InterruptedException {
        return new MyWebDriverPoolWrapper(mDrivers.take(), this);
    }

    void release(MyWebDriver driver) {
        mDrivers.offer(driver);
    }

    public void close() {
        for(MyWebDriver driver: mDrivers) {
            driver.close();
        }
    }
}
