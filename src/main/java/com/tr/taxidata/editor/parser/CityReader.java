package com.tr.taxidata.editor.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CityReader {

    public static List<Landmark> readCityWktAndGetLandmarks() throws IOException {

        InputStream stream = CityReader.class.getClassLoader().getResourceAsStream("bursa-map.wkt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = reader.readLine();
        List<Landmark> cityLandmarks = new ArrayList<>();
        while (line != null) {

            String[] lineString = line.split("LINESTRING");
            String values = lineString[1];
            values = values.trim();
            String coordinateText = values.substring(1, values.length() - 2);
            String[] twoDimensionCoordinate = coordinateText.split(",");
            Arrays.stream(twoDimensionCoordinate).forEach(coordinates -> {
                coordinates = coordinates.trim();
                String[] xy = coordinates.split(" ");
                Landmark landmark = new Landmark();
                landmark.setLongitude(Double.parseDouble(xy[0]));
                landmark.setLatitude(Double.parseDouble(xy[1]));
                cityLandmarks.add(landmark);
            });

            line = reader.readLine();
        }
        reader.close();
        return cityLandmarks;
    }
}
