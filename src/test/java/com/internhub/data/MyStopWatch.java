package com.internhub.data;

import java.util.concurrent.TimeUnit;

import org.junit.AssumptionViolatedException;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;

public class MyStopWatch extends Stopwatch {

    private static void log(Description description, String status, long nanos) {
        String testName = description.getMethodName();
        System.out.println(String.format("Test %s %s, spent %d seconds",
                testName, status, TimeUnit.NANOSECONDS.toSeconds(nanos)));
    }

    @Override
    protected void succeeded(long nanos, Description description) {
        log(description, "Passed", nanos);
    }

    @Override
    protected void failed(long nanos, Throwable e, Description description) {
        log(description, "Failed", nanos);
    }

    @Override
    protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
        log(description, "Skipped", nanos);
    }

    @Override
    protected void finished(long nanos, Description description) {
        log(description, "Finished", nanos);
    }
}
