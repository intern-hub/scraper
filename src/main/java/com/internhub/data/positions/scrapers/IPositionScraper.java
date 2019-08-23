package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface IPositionScraper {
    void setup(Company company);
    void fetch(Company company, PositionCallback callback);
}
