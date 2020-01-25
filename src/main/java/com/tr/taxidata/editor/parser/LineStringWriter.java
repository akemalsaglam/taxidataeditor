package com.tr.taxidata.editor.parser;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class LineStringWriter {

    private List<Landmark> landmarks;
    private Landmark closestLandmark;
    private static final int MODULO_FACTOR = 10;

    public LineStringWriter(List<Landmark> landmarks, Landmark closestLandmark) {
        this.landmarks = landmarks;
        this.closestLandmark = closestLandmark;
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

        AtomicInteger count = new AtomicInteger();
        IntStream.range(0, this.landmarks.size()).forEach(index -> {
            if (index % MODULO_FACTOR == 0) {
                stringBuilder.append(this.landmarks.get(index).getX());
                stringBuilder.append(" ");
                stringBuilder.append(this.landmarks.get(index).getY());
                if (index != this.landmarks.size() - 1) {
                    stringBuilder.append(", ");
                }
                count.getAndIncrement();
            }
        });
        System.out.println(count.get());
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

}
