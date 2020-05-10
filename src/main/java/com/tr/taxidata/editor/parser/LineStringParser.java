package com.tr.taxidata.editor.parser;

import com.tr.taxidata.editor.model.TaxiData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LineStringParser {
    private Map<Long, TaxiData> positions;
    private List<Landmark> landmarks;

    public LineStringParser(Map<Long, TaxiData> position) {
        this.positions = position;
        landmarks = new ArrayList<>();
    }

    public List<Landmark> getLandmarks() {
        return this.landmarks;
    }

    public void parse() {
        positions.forEach((key, value) -> {
            String coordinatesString = value.getPosition().substring(11, value.getPosition().length() - 1);
            String[] coordinates = coordinatesString.split(",");
            Landmark landmark = new Landmark();
            landmark.setLatitude(Double.parseDouble(coordinates[0].split(" ")[1].trim()));
            landmark.setLongitude(Double.parseDouble(coordinates[0].split(" ")[0].trim()));
            landmark.setDate(value.getDate());
            landmark.setTimeInSecond(value.getTimeInSecond());
            this.landmarks.add(landmark);
        });

    }
}
