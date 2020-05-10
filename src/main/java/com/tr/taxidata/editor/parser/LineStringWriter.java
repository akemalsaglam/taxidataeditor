package com.tr.taxidata.editor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LineStringWriter {

    private List<Landmark> landmarks;
    private Landmark closestLandmark;
    private int moduloFactor;

    public LineStringWriter(List<Landmark> landmarks, Landmark closestLandmark) {
        this.landmarks = landmarks;
        this.closestLandmark = closestLandmark;
    }

    public LineStringWriter(List<Landmark> landmarks, Landmark closestLandmark, int moduloFactor) {
        this.landmarks = landmarks;
        this.closestLandmark = closestLandmark;
        this.moduloFactor = moduloFactor;
    }

    public List<Landmark> getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    public Landmark getClosestLandmark() {
        return closestLandmark;
    }

    public void setClosestLandmark(Landmark closestLandmark) {
        this.closestLandmark = closestLandmark;
    }

    public String write() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("LINESTRING (");
        stringBuilder.append(this.closestLandmark.getLongitude());
        stringBuilder.append(" ");
        stringBuilder.append(this.closestLandmark.getLatitude());
        stringBuilder.append(", ");

        List<Landmark> selectedLandmarks = new ArrayList<>();

        IntStream.range(0, this.landmarks.size()).forEach(index -> {
            if (moduloFactor == 0 || (index % moduloFactor == 0)) {
                selectedLandmarks.add(this.landmarks.get(index));

            }
        });

        IntStream.range(0, selectedLandmarks.size()).forEach(index -> {
            stringBuilder.append(selectedLandmarks.get(index).getX());
            stringBuilder.append(" ");
            stringBuilder.append(selectedLandmarks.get(index).getY());
            if (index != (selectedLandmarks.size() - 1)) {
                stringBuilder.append(", ");
            }
        });

        stringBuilder.append(")");
        //System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    public String writeWithTimeData() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.closestLandmark.getLongitude())
                .append(",")
                .append(this.closestLandmark.getLatitude())
                .append(",")
                .append(0)
                .append(System.lineSeparator());


        List<Landmark> orderedLandmarks = this.landmarks.stream()
                .sorted(Comparator.comparing(Landmark::getDate))
                .collect(Collectors.toList());

        IntStream.range(0, orderedLandmarks.size()).forEach(index -> {
            if (moduloFactor == 0 || (index % moduloFactor == 0)) {
                stringBuilder.append(orderedLandmarks.get(index).getLongitude())
                        .append(",")
                        .append(orderedLandmarks.get(index).getLatitude())
                        .append(",")
                        .append(orderedLandmarks.get(index).getDate())
                        .append(System.lineSeparator());
            }
        });
        return stringBuilder.toString();
    }

}
