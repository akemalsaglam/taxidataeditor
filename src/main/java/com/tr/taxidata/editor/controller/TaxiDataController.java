package com.tr.taxidata.editor.controller;

import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import com.tr.taxidata.editor.parser.*;
import com.tr.taxidata.editor.service.TaxiDataService;
import com.tr.taxidata.editor.util.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/taxidata")
public class TaxiDataController {

    private TaxiDataService taxiDataService;

    public TaxiDataController(TaxiDataService taxiDataService) {
        this.taxiDataService = taxiDataService;
    }


    /*@GetMapping(path = "/{id}/monthly/{month}/")
    public List<TaxiData> getMonth1DataByTaxiId(@PathVariable("id") Long id, @PathVariable("month") int month) {
        return taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(id, month);
    }

    @GetMapping(path = "/{id}/monthly/{month}/linestring")
    public String getMonthlyDataByTaxiIdLineString(@PathVariable("id") Long id, @PathVariable("month") int month) throws IOException {
        System.out.println("fetching data from db...");
        List<TaxiData> taxiData = taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(id, month);
        List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());

        System.out.println("parsing linestrings...");
        LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
        lineStringParser.parse();

        System.out.println("transforming latitudes/longitudes to x,y coordinates...");
        List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());

        // TODO: we can calculate closest point by including all points
        System.out.println("finding closest point in map...");
        List<Landmark> cityLandmarks = CityReader.readCityWktAndGetLandmarks();
        Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), cityLandmarks);

        System.out.println("writing results...");
        LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark);
        return lineStringWriter.write();
    }

    @GetMapping(path = "/{id}/monthly/{month}/linestring/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ByteArrayResource> getMonthlyDataByTaxiIdLineStringDownload(@PathVariable("id") Long id, @PathVariable("month") int month, HttpServletResponse response) throws IOException {
        System.out.println("fetching data from db...");
        List<TaxiData> taxiData = taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(id, month);
        List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());

        System.out.println("parsing linestrings...");
        LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
        lineStringParser.parse();

        System.out.println("transforming latitudes/longitudes to x,y coordinates...");
        List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());

        System.out.println("finding closest point in map...");
        List<Landmark> cityLandmarks = CityReader.readCityWktAndGetLandmarks();
        Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), cityLandmarks);

        System.out.println("writing results...");
        LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark, 10);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        byte[] data = lineStringWriter.write().getBytes();
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=taxi-" + id + ".wkt")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping(path = "monthly/{month}/linestring/top/{limit}/download")
    public ResponseEntity<ByteArrayResource> getMonthlyDataByTaxiIdLineStringForTop(@PathVariable("month") int month, @PathVariable("limit") long limit) throws IOException {
        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxisByLimit(month, limit);
        ForkJoinPool forkJoinPool = new ForkJoinPool(20);
        Map<String, byte[]> topTaxisLineStrings = new HashMap<>();

        forkJoinPool.submit(() -> topTaxis.parallelStream().forEach(taxi -> {
            List<TaxiData> taxiData = taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(taxi.getTaxiId(), month);
            List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());

            LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
            lineStringParser.parse();

            List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());

            List<Landmark> cityLandmarks = null;
            try {
                cityLandmarks = CityReader.readCityWktAndGetLandmarks();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), cityLandmarks);

            LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark, 10);

            byte[] data = lineStringWriter.write().getBytes();
            topTaxisLineStrings.put(taxi.getTaxiId().toString(), data);
        })).join();

        TaxiDataHelper.printSettingsByTaxi(topTaxis);

        byte[] data = ZipHelper.zipBytes(topTaxisLineStrings);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=taxi_lineStrings_month" + month + "_top" + limit + ".zip")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping(path = "/{id}/monthly/{month}/arff")
    public String getMonthlyDataByTaxiIdArff(@PathVariable("id") Long id, @PathVariable("month") int month) throws IOException {
        System.out.println("fetching data from db...");
        List<TaxiData> taxiData = taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(id, month);

        System.out.println("parsing linestrings...");
        List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());
        LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
        lineStringParser.parse();

        System.out.println("transforming latitudes/longitudes to x,y coordinates...");
        List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());

        System.out.println("finding closest point in map...");
        List<Landmark> cityLandmarks = CityReader.readCityWktAndGetLandmarks();
        Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), cityLandmarks);

        System.out.println("writing weka results...");
        WekaWriter wekaWriter = new WekaWriter(landmarks, closestLandmark);
        return wekaWriter.write();

    }

    @GetMapping(path = "/{id}/monthly/{month}/arff/download")
    public ResponseEntity<ByteArrayResource> getMonthlyDataByTaxiIdArffDownload(@PathVariable("id") Long id, @PathVariable("month") int month) throws IOException {
        System.out.println("fetching data from db...");
        List<TaxiData> taxiData = taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(id, month);

        System.out.println("parsing linestrings...");
        List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());
        LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
        lineStringParser.parse();

        System.out.println("transforming latitudes/longitudes to x,y coordinates...");
        List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());

        System.out.println("finding closest point in map...");
        List<Landmark> cityLandmarks = CityReader.readCityWktAndGetLandmarks();
        Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), cityLandmarks);

        System.out.println("writing weka results...");
        WekaWriter wekaWriter = new WekaWriter(landmarks, closestLandmark);

        byte[] data = wekaWriter.write().getBytes();
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=taxi-" + id + ".arff")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping(path = "monthly/{month}/arff/top/{limit}/download")
    public ResponseEntity<ByteArrayResource> getMonthlyTopTaxiArffDownload(@PathVariable("month") int month, @PathVariable("limit") long limit) throws IOException {

        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxisByLimit(month, limit);
        ForkJoinPool forkJoinPool = new ForkJoinPool(20);
        List<StringBuilder> arffStringWithoutHeader = new ArrayList<>();
        forkJoinPool.submit(() -> topTaxis.parallelStream().forEach(taxi -> {

            List<TaxiData> taxiData = taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(taxi.getTaxiId(), month);

            List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());
            LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
            lineStringParser.parse();

            List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());

            List<Landmark> cityLandmarks = null;
            try {
                cityLandmarks = CityReader.readCityWktAndGetLandmarks();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), cityLandmarks);

            WekaWriter wekaWriter = new WekaWriter(landmarks, closestLandmark);
            arffStringWithoutHeader.add(wekaWriter.getArffStringWithoutHeader());
        })).join();

        StringBuilder stringBuilder = ArffHelper.getArffStringBuilder(arffStringWithoutHeader);

        byte[] data = stringBuilder.toString().getBytes();
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=taxi_lineStrings_month" + month + "_top" + limit + ".arff")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(data.length)
                .body(resource);
    }
*/
    //***************************************************************
    @GetMapping(path = "month/{month}/day1/taxi/{limit}/download")
    public ResponseEntity<ByteArrayResource> getMonthlyDataByForTop(@PathVariable("month") int month, @PathVariable("limit") long limit) throws IOException {
        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxisByLimit(month, limit);
        Map<String, byte[]> fileNameAndContent = new HashMap<>();
        Map<Long, Long> meanSpeedByTaxi = new HashMap<>();
        List<String> aggregatedLineStrings = new ArrayList<>();

        List<Landmark> cityLandmarks = null;
        try {
            cityLandmarks = CityReader.readCityWktAndGetLandmarks();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(20);
        int mod10 = 0;
        List<Landmark> finalCityLandmarks = cityLandmarks;
        forkJoinPool.submit(() -> topTaxis.parallelStream().forEach(taxi -> {
            List<TaxiData> taxiData = taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(taxi.getTaxiId(), month);
            long meanSpeed = Math.round(taxiData.stream().map(TaxiData::getSpeed).mapToDouble(speed -> speed).average().getAsDouble());
            meanSpeedByTaxi.put(taxiData.get(0).getTaxiId(), meanSpeed);
            //List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());
            Map<Long, TaxiData> taxiDataPositions = taxiData.stream().collect(Collectors.toMap(TaxiData::getId, taxiDatax->taxiDatax));

            LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
            lineStringParser.parse();

            List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());
            landmarks.removeAll(landmarks.stream().filter(landmark -> landmark.getLongitude() < MapHelper.MIN_LONGITUDE
                    || landmark.getLongitude() > MapHelper.MAX_LONGITUDE
                    || landmark.getLatitude() < MapHelper.MIN_LATITUDE
                    || landmark.getLatitude() > MapHelper.MAX_LATITUDE).collect(Collectors.toList()));


            Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), finalCityLandmarks);

            LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark, mod10);

            String lineString = lineStringWriter.write();
            aggregatedLineStrings.add(lineString);
            byte[] data = lineString.getBytes();
            fileNameAndContent.put("taxi-" + taxi.getTaxiId() + ".wkt", data);

        })).join();

        fileNameAndContent.put("settings.txt", TaxiDataHelper.getSimulationSettingsForTaxi(topTaxis, meanSpeedByTaxi).getBytes());
        fileNameAndContent.put("allLineStrings.txt", aggregatedLineStrings.stream().collect(Collectors.joining(System.lineSeparator())).getBytes());
        String zipName = new StringBuilder()
                .append(limit).append("taxi-month")
                .append(month).append("-simulation.zip").toString();
        return ResponseEntityHelper.getZipResponeEntity(fileNameAndContent, month, limit, zipName);
    }

    @GetMapping(path = "month/{month}/week1/taxi/{limit}/download")
    public ResponseEntity<ByteArrayResource> getMonthlyWeek1DataByForTop(@PathVariable("month") int month, @PathVariable("limit") long limit) throws IOException {
        Map<String, byte[]> fileNameAndContent = new HashMap<>();
        List<StringBuilder> arffStringWithoutHeader = new ArrayList<>();

        List<Landmark> cityLandmarks = null;
        try {
            cityLandmarks = CityReader.readCityWktAndGetLandmarks();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int notMod = 0;
        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxisByLimit(month, limit);
        ForkJoinPool forkJoinPool = new ForkJoinPool(20);
        List<Landmark> finalCityLandmarks = cityLandmarks;
        forkJoinPool.submit(() -> topTaxis.parallelStream().forEach(taxi -> {
            List<List<TaxiData>> taxiDataList = null;
            try {
                taxiDataList = taxiDataService.getMonthlyWeek1DataByTaxiIdAndMonth(taxi.getTaxiId(), month);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            AtomicInteger day = new AtomicInteger(1);
            taxiDataList.forEach(taxiData -> {
                //List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());
                Map<Long, TaxiData> taxiDataPositions = taxiData.stream().collect(Collectors.toMap(TaxiData::getId, taxiDatax->taxiDatax));

                LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
                lineStringParser.parse();

                List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());
                landmarks.removeAll(landmarks.stream().filter(landmark -> landmark.getLongitude() < MapHelper.MIN_LONGITUDE
                        || landmark.getLongitude() > MapHelper.MAX_LONGITUDE
                        || landmark.getLatitude() < MapHelper.MIN_LATITUDE
                        || landmark.getLatitude() > MapHelper.MAX_LATITUDE).collect(Collectors.toList()));


                Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), finalCityLandmarks);

                LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark, notMod);

                byte[] data = lineStringWriter.write().getBytes();
                fileNameAndContent.put("taxi-" + taxi.getTaxiId() + "-" + day + ".wkt", data);

                WekaWriter wekaWriter = new WekaWriter(landmarks, closestLandmark);
                arffStringWithoutHeader.add(wekaWriter.getArffStringWithoutHeader());

                day.getAndIncrement();
            });

        })).join();

        StringBuilder arffStringBuilder = ArffHelper.getArffStringBuilder(arffStringWithoutHeader);
        fileNameAndContent.put(limit + "taxi-month" + month + ".arff", arffStringBuilder.toString().getBytes());
        String zipName = new StringBuilder()
                .append(limit).append("taxi-month")
                .append(month).append("-training.zip").toString();
        return ResponseEntityHelper.getZipResponeEntity(fileNameAndContent, month, limit, zipName);
    }

    @GetMapping(path = "month/{month}/day1/taxi/{limit}/timedata/download")
    public ResponseEntity<ByteArrayResource> getMonthlyTimeDataByForTop(@PathVariable("month") int month, @PathVariable("limit") long limit) throws IOException {
        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxisByLimit(month, limit);
        Map<String, byte[]> fileNameAndContent = new HashMap<>();
        Map<Long, Long> meanSpeedByTaxi = new HashMap<>();
        List<String> aggregatedLineStrings = new ArrayList<>();

        List<Landmark> cityLandmarks = null;
        try {
            cityLandmarks = CityReader.readCityWktAndGetLandmarks();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(20);
        int mod = 0;
        List<Landmark> finalCityLandmarks = cityLandmarks;
        forkJoinPool.submit(() -> topTaxis.parallelStream().forEach(taxi -> {
            List<TaxiData> taxiData = taxiDataService.getMonthlyDay1DataByTaxiIdAndMonth(taxi.getTaxiId(), month);
            long meanSpeed = Math.round(taxiData.stream().map(TaxiData::getSpeed).mapToDouble(speed -> speed).average().getAsDouble());
            meanSpeedByTaxi.put(taxiData.get(0).getTaxiId(), meanSpeed);
            //HashMap<Timestamp, String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());
            Map<Long, TaxiData> taxiDataPositions = taxiData.stream().collect(Collectors.toMap(TaxiData::getId, taxiDatax->taxiDatax));

            LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
            lineStringParser.parse();

            List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());
            landmarks.removeAll(landmarks.stream().filter(landmark -> landmark.getLongitude() < MapHelper.MIN_LONGITUDE
                    || landmark.getLongitude() > MapHelper.MAX_LONGITUDE
                    || landmark.getLatitude() < MapHelper.MIN_LATITUDE
                    || landmark.getLatitude() > MapHelper.MAX_LATITUDE).collect(Collectors.toList()));


            Landmark closestLandmark = ClosestPositionCalculator.calculate(landmarks.get(0), finalCityLandmarks);

            LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark, mod);

            String lineString = lineStringWriter.writeWithTimeData();
            aggregatedLineStrings.add(lineString);
            byte[] data = lineString.getBytes();
            fileNameAndContent.put("taxi-" + taxi.getTaxiId() + ".wkt", data);

        })).join();

        fileNameAndContent.put("settings.txt", TaxiDataHelper.getSimulationSettingsForTaxi(topTaxis, meanSpeedByTaxi).getBytes());
        fileNameAndContent.put("allLineStrings.txt", aggregatedLineStrings.stream().collect(Collectors.joining(System.lineSeparator())).getBytes());
        String zipName = new StringBuilder()
                .append(limit).append("taxi-month")
                .append(month).append("-simulation.zip").toString();
        return ResponseEntityHelper.getZipResponeEntity(fileNameAndContent, month, limit, zipName);
    }

}
