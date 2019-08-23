package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Position;

import java.util.List;

public interface PositionCallback {
    void run(List<Position> positions);
}
