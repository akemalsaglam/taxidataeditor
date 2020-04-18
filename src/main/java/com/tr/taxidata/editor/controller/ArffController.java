package com.tr.taxidata.editor.controller;

import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import com.tr.taxidata.editor.parser.*;
import com.tr.taxidata.editor.service.TaxiDataService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(path = "/taxidata")
public class TaxiDataController {

    private TaxiDataService taxiDataService;

    public TaxiDataController(TaxiDataService taxiDataService) {
        this.taxiDataService = taxiDataService;
    }

    @GetMapping(path = "/")
    public List<TaxiData> getAll() {
        return taxiDataService.findAll();
    }

    @GetMapping(path = "/{id}")
    public TaxiData get(@PathVariable("id") Long id) {
        return taxiDataService.getById(id).orElse(new TaxiData());
    }

    @GetMapping(path = "/{id}/monthly/{month}/")
    public List<TaxiData> getMonth1DataByTaxiId(@PathVariable("id") Long id, @PathVariable("month") int month) {
        return taxiDataService.getMonthlyDataByTaxiIdAndMonth(id, month);
    }

    @GetMapping(path = "/{id}/monthly/{month}/linestring")
    public String getMonthlyDataByTaxiIdLineString(@PathVariable("id") Long id, @PathVariable("month") int month) throws IOException {
        System.out.println("fetching data from db...");
        List<TaxiData> taxiData = taxiDataService.getMonthlyDataByTaxiIdAndMonth(id, month);
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
        List<TaxiData> taxiData = taxiDataService.getMonthlyDataByTaxiIdAndMonth(id, month);
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
            List<TaxiData> taxiData = taxiDataService.getMonthlyDataByTaxiIdAndMonth(taxi.getTaxiId(), month);
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

        printSettingsByTaxi(topTaxis);

        byte[] data = zipBytes(topTaxisLineStrings);
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
        List<TaxiData> taxiData = taxiDataService.getMonthlyDataByTaxiIdAndMonth(id, month);

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
        List<TaxiData> taxiData = taxiDataService.getMonthlyDataByTaxiIdAndMonth(id, month);

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

            List<TaxiData> taxiData = taxiDataService.getMonthlyDataByTaxiIdAndMonth(taxi.getTaxiId(), month);

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

        StringBuilder stringBuilder = getArffStringBuilder(arffStringWithoutHeader);

        byte[] data = stringBuilder.toString().getBytes();
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=taxi_lineStrings_month" + month + "_top" + limit + ".arff")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping(path = "monthly/{month}/all/top/{limit}/download")
    public ResponseEntity<ByteArrayResource> getMonthlyDataByForTop(@PathVariable("month") int month, @PathVariable("limit") long limit) throws IOException {
        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxisByLimit(month, limit);
        ForkJoinPool forkJoinPool = new ForkJoinPool(20);
        Map<String, byte[]> topTaxisLineStrings = new HashMap<>();
        List<StringBuilder> arffStringWithoutHeader = new ArrayList<>();

        forkJoinPool.submit(() -> topTaxis.parallelStream().forEach(taxi -> {
            List<TaxiData> taxiData = taxiDataService.getMonthlyDataByTaxiIdAndMonth(taxi.getTaxiId(), month);
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

            WekaWriter wekaWriter = new WekaWriter(landmarks, closestLandmark);
            arffStringWithoutHeader.add(wekaWriter.getArffStringWithoutHeader());

        })).join();

        printSettingsByTaxi(topTaxis);
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

    @GetMapping(path = "monthly/{month}/week1/all/top/{limit}/download")
    public ResponseEntity<ByteArrayResource> getMonthlyWeek1DataByForTop(@PathVariable("month") int month, @PathVariable("limit") long limit) throws IOException {
        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxisByLimit(month, limit);
        ForkJoinPool forkJoinPool = new ForkJoinPool(20);
        Map<String, byte[]> topTaxisLineStrings = new HashMap<>();
        List<StringBuilder> arffStringWithoutHeader = new ArrayList<>();

        forkJoinPool.submit(() -> topTaxis.parallelStream().forEach(taxi -> {
            List<List<TaxiData>> taxiDataList = null;
            try {
                taxiDataList = taxiDataService.getMonthlyWeek1DataByTaxiIdAndMonth(taxi.getTaxiId(), month);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            AtomicInteger day= new AtomicInteger(1);
            taxiDataList.forEach(taxiData->{
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
                topTaxisLineStrings.put(taxi.getTaxiId().toString()+"_"+day, data);

                WekaWriter wekaWriter = new WekaWriter(landmarks, closestLandmark);
                arffStringWithoutHeader.add(wekaWriter.getArffStringWithoutHeader());

                day.getAndIncrement();
            });

        })).join();

        printSettingsByTaxi(topTaxis);
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
