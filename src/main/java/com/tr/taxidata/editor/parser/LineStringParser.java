package com.tr.taxidata.editor.parser;

import java.util.ArrayList;
import java.util.List;

public class LineStringParser {
    private List<String> positions;
    private List<Landmark> landmarks;

    public LineStringParser(List<String> position) {
        this.positions = position;
        landmarks = new ArrayList<>();
    }

    public List<String> getPosition() {
        return positions;
    }

    public void setPosition(List<String> position) {
        this.positions = position;
    }

    public List<Landmark> getLandmarks() {
        return this.landmarks;
    }

    public void parse() {
        positions.forEach(position->{
            String coordinatesString = position.substring(11, position.length() - 1);
            String[] coordinates = coordinatesString.split(",");
            Landmark landmark=new Landmark();
            landmark.setLatitude(Double.parseDouble(coordinates[0].split(" ")[1].trim()));
            landmark.setLongitude(Double.parseDouble(coordinates[0].split(" ")[0].trim()));
            this.landmarks.add(landmark);
        });

    }
}
