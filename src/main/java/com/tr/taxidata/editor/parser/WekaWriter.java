package com.tr.taxidata.editor.parser;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class WekaWriter {
    private List<Landmark> landmarks;
    private static final int MODULO_FACTOR = 10;

    public WekaWriter(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    public List<Landmark> getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    public String write() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@RELATION taxi");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("@ATTRIBUTE xValue REAL");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("@ATTRIBUTE yValue REAL");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("@DATA");
        stringBuilder.append(System.getProperty("line.separator"));
        IntStream.range(0, this.landmarks.size()).forEach(index -> {
            if (index % MODULO_FACTOR == 0) {
                stringBuilder.append(this.landmarks.get(index).getX()).append(", ").append(this.landmarks.get(index).getY());
                stringBuilder.append(System.getProperty("line.separator"));
            }
        });
        /*this.landmarks.forEach(landmark -> {
            stringBuilder.append(landmark.getX()).append(", ").append(landmark.getY());
            stringBuilder.append(System.getProperty("line.separator"));
        });*/
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WekaWriter that = (WekaWriter) o;
        return Objects.equals(landmarks, that.landmarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(landmarks);
    }

    @Override
    public String toString() {
        return "WekaWriter{" +
                "landmarks=" + landmarks +
                '}';
    }
}
