package com.internhub.data.positions.writers;

import com.internhub.data.models.Position;

import java.util.List;

public interface PositionWriter {
    public void save(List<Position> newPositions);
}
