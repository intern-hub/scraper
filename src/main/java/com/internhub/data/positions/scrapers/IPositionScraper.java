package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;

import java.util.List;

public interface IPositionScraper {
    List<Position> fetch(Company company);
}
