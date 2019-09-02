package com.internhub.data.positions.writers;

import com.internhub.data.models.Position;

import java.util.List;

public interface IPositionWriter {
    void save(Position newPosition);
    void save(List<Position> newPositions);
}
