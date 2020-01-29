package com.tr.taxidata.editor.parser;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.IntStream;

public class WekaWriter {
    private List<Landmark> landmarks;
    private static final int MODULO_FACTOR = 10;
    private Landmark closestLandmark;

    public WekaWriter(List<Landmark> landmarks, Landmark closestLandmark) {
        this.landmarks = landmarks;
        this.closestLandmark = closestLandmark;
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
        stringBuilder.append(this.closestLandmark.getLongitude()).append(", ").append(this.closestLandmark.getLatitude());
        stringBuilder.append(System.getProperty("line.separator"));

        IntStream.range(0, this.landmarks.size()).forEach(index -> {
            if (index % MODULO_FACTOR == 0) {
                stringBuilder.append(this.landmarks.get(index).getX()).append(", ").append(this.landmarks.get(index).getY());
                stringBuilder.append(System.getProperty("line.separator"));
            }
        });
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    public void writeFile() throws IOException {
        String path = WekaWriter.class.getClassLoader().getResource("resources").getPath();
        File wkt = new File(path + "/deneme.arff");

        if (wkt.exists()) wkt.delete();
        wkt.createNewFile();

        FileWriter wktstream = new FileWriter(wkt, true);
        wktstream.append("deneme");

        wktstream.close();
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
