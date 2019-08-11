package com.internhub.data.positions;

import com.internhub.data.models.Company;
import java.util.concurrent.Future;

public interface IPositionMTScraper {
    Future fetch(Company company);
}
