package com.tr.taxidata.editor.controller;

import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import com.tr.taxidata.editor.parser.*;
import com.tr.taxidata.editor.service.TaxiDataService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(path = "/arff")
public class ArffController {

    private static final double MAX_LATITUDE = 40.2891;
    private static final double MIN_LATITUDE = 40.1652;
    private static final double MAX_LONGITUDE = 29.2001;
    private static final double MIN_LONGITUDE = 28.7994;


    private TaxiDataService taxiDataService;

    public ArffController(TaxiDataService taxiDataService) {
        this.taxiDataService = taxiDataService;
    }

    @GetMapping(path = "monthly/{month}/week1/all/top/{limit}")
    public ResponseEntity<ByteArrayResource> getMonthlyWeek1DataByForTop(@PathVariable("month") int month, @PathVariable("limit") long limit) throws IOException {
        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxisByLimit(month, limit);
        ForkJoinPool forkJoinPool = new ForkJoinPool(20);
        Map<String, byte[]> topTaxisLineStrings = new HashMap<>();
        List<StringBuilder> arffStringWithoutHeader = new ArrayList<>();

        AtomicInteger oufOfBoundCount = new AtomicInteger(0);
        forkJoinPool.submit(() -> topTaxis.parallelStream().forEach(taxi -> {
            List<List<TaxiData>> taxiDataList = null;
            try {
                taxiDataList = taxiDataService.getMonthlyWeek1DataByTaxiIdAndMonth(taxi.getTaxiId(), month);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            AtomicInteger day = new AtomicInteger(1);


            taxiDataList.forEach(taxiData -> {
                List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());

                LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
                lineStringParser.parse();

                List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());


                /*landmarks.forEach(landmark -> {
                    if (landmark.getLongitude() < MIN_LONGITUDE
                            || landmark.getLongitude() > MAX_LONGITUDE
                            || landmark.getLatitude() < MIN_LATITUDE
                            || landmark.getLatitude() > MAX_LATITUDE) {
                        System.out.println("longitude: " + landmark.getLongitude() + ", latitude: " + landmark.getLatitude());
                        oufOfBoundCount.getAndIncrement();
                    }
                });*/

                landmarks.removeAll(landmarks.stream().filter(landmark -> landmark.getLongitude() < MIN_LONGITUDE
                        || landmark.getLongitude() > MAX_LONGITUDE
                        || landmark.getLatitude() < MIN_LATITUDE
                        || landmark.getLatitude() > MAX_LATITUDE).collect(Collectors.toList()));

                List<Landmark> cityLandmarks = null;
                try {
                    cityLandmarks = CityReader.readCityWktAndGetLandmarks();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), cityLandmarks);

                LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark, 10);

                /*byte[] data = lineStringWriter.write().getBytes();
                topTaxisLineStrings.put(taxi.getTaxiId().toString() + "_" + day, data);*/

                WekaWriter wekaWriter = new WekaWriter(landmarks, closestLandmark);
                arffStringWithoutHeader.add(wekaWriter.getArffStringWithoutHeader());

                day.getAndIncrement();
            });

        })).join();

        StringBuilder stringBuilder = getArffStringBuilder(arffStringWithoutHeader);
        topTaxisLineStrings.put("all_arff",stringBuilder.toString().getBytes());
        byte[] data = zipBytes(topTaxisLineStrings);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=taxi_lineStrings_month" + month + "_top" + limit + ".zip")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .contentLength(data.length)
                .body(resource);
    }

    private StringBuilder getArffStringBuilder(List<StringBuilder> arffStringWithoutHeader) {
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
        arffStringWithoutHeader.forEach(stringBuilder::append);
        return stringBuilder;
    }

    private static void printSettingsByTaxi(List<TaxiDataCountDto> topTaxis) {
        AtomicInteger index = new AtomicInteger(1);
        topTaxis.forEach(taxi -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Group").append(index.get()).append(".groupID = taxi").append(index.get()).append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".hostName = taxi-").append(taxi.getTaxiId()).append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".okMaps = 1").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".speed = 8.3, 25").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".nrofHosts = 1").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".movementModel = MapRouteMovement").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".routeFile = data/custom/taxidata/bursa-0101/taxi-").append(taxi.getTaxiId()).append(".wkt").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".routeType = 1").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".routeFirstStop = 0").append(System.lineSeparator());
            stringBuilder.append(" ").append(System.lineSeparator());
            index.getAndIncrement();
            System.out.println(stringBuilder.toString());
        });
    }

    private static byte[] zipBytes(Map<String, byte[]> topTaxisLineStrings) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        topTaxisLineStrings.entrySet().forEach(lineStringEntry -> {
            ZipEntry entry = new ZipEntry("taxi-" + lineStringEntry.getKey() + ".wkt");
            entry.setSize(lineStringEntry.getValue().length);
            try {
                zos.putNextEntry(entry);
                zos.write(lineStringEntry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }

}
