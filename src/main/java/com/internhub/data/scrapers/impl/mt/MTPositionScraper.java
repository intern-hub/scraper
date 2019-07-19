package com.internhub.data.scrapers.impl.mt;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.scrapers.MTScraper;
import com.internhub.data.scrapers.PositionScraper;
import com.internhub.data.scrapers.impl.DefaultPositionScraper;
import com.internhub.data.selenium.CloseableWebDriverAdapter;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MTPositionScraper implements MTScraper {
    private ExecutorService pool;
    private Queue<Company> work;
    private Queue<Position> result;

    public MTPositionScraper() {
        pool = Executors.newFixedThreadPool(20);
        work = new ConcurrentLinkedQueue<>();
        result = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void load(Collection workItems) {
        work.addAll(workItems);
    }

    @Override
    public Collection<Position> scrape() {
        while (!work.isEmpty()) {
            Company item = work.poll();
            pool.execute(() -> {
                try (CloseableWebDriverAdapter driverAdapter = new CloseableWebDriverAdapter()) {
                    PositionScraper scraper = new DefaultPositionScraper(driverAdapter.getDriver());
                    result.addAll(scraper.fetch(item));
                }
            });
        }

        pool.shutdown();
        while (true) {
            try {
                if (pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ignored) { }
        }

        return result;
    }
}
