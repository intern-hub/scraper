package com.internhub.data.scrapers.positions;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;

import java.util.List;

public interface PositionScraper {
    List<Position> fetch(Company company);
}
