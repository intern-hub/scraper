package com.internhub.data.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;

import java.net.MalformedURLException;
import java.util.List;

public interface PositionScraper {
    List<Position> fetch(Company company) throws MalformedURLException;
}
