package com.internhub.data.positions.writers;

import com.internhub.data.models.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface IPositionWriter {
    Logger logger = LoggerFactory.getLogger(IPositionWriter.class);

    void save(Position newPosition);
    void save(List<Position> newPositions);
}
