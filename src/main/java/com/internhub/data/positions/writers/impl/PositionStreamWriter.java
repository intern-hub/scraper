package com.internhub.data.positions.writers.impl;

import com.internhub.data.models.Position;
import com.internhub.data.positions.writers.IPositionWriter;

import java.io.PrintStream;
import java.util.List;

public class PositionStreamWriter implements IPositionWriter {
    private PrintStream mStream;

    public PositionStreamWriter(PrintStream stream) {
        mStream = stream;
    }

    @Override
    public synchronized void save(Position newPosition) {
        mStream.println("Scraped position: " + newPosition);
    }

    @Override
    public synchronized void save(List<Position> newPositions) {
        mStream.println("Scraped " + newPositions.size() + " positions: " + newPositions);
    }
}
