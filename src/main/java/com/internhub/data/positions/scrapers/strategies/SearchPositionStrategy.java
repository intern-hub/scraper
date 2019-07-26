package com.internhub.data.positions.scrapers.strategies;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;

import java.util.List;

public interface SearchPositionStrategy {
    public List<Position> fetchWithInitialLinks(Company company, List<String> initialLinks);
}
