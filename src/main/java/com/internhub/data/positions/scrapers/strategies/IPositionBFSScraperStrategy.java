package com.internhub.data.positions.scrapers.strategies;

import com.internhub.data.pages.Page;

public interface IPositionBFSScraperStrategy {
    int MAX_DEPTH = 4;
    int MAX_TOTAL_LINKS = 100;
    int PAGE_LOAD_DELAY_MS = 2000;


    Page getPage(String link);
}
