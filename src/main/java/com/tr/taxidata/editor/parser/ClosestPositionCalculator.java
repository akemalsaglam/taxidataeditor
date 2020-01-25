package com.tr.taxidata.editor.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class ClosestPositionCalculator {

    public static Landmark calculate(Landmark rootLandmark, List<Landmark> lookUpLandmarks) {
        Map<Double, Landmark> landmarkHypotenuse = new HashMap<>();

        lookUpLandmarks.forEach(landmark -> {
            if (landmark != null) {
                landmarkHypotenuse.put(Math.hypot(rootLandmark.getX() - landmark.getLongitude(), rootLandmark.getY() - landmark.getLatitude()), landmark);
            }
        });

        OptionalDouble key = landmarkHypotenuse.keySet().stream().mapToDouble(v -> v).min();
        return landmarkHypotenuse.get(key.getAsDouble());
    }
}
