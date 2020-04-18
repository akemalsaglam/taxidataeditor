package com.tr.taxidata.editor.controller;

import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import com.tr.taxidata.editor.parser.*;
import com.tr.taxidata.editor.service.TaxiDataService;
import com.tr.taxidata.editor.util.ArffHelper;
import com.tr.taxidata.editor.util.MapHelper;
import com.tr.taxidata.editor.util.ResponseEntityHelper;
import com.tr.taxidata.editor.util.ZipHelper;
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

                landmarks.removeAll(landmarks.stream().filter(landmark -> landmark.getLongitude() < MapHelper.MIN_LONGITUDE
                        || landmark.getLongitude() > MapHelper.MAX_LONGITUDE
                        || landmark.getLatitude() < MapHelper.MIN_LATITUDE
                        || landmark.getLatitude() > MapHelper.MAX_LATITUDE).collect(Collectors.toList()));

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

        StringBuilder stringBuilder = ArffHelper.getArffStringBuilder(arffStringWithoutHeader);
        topTaxisLineStrings.put("all_arff",stringBuilder.toString().getBytes());
        byte[] data = ZipHelper.zipBytes(topTaxisLineStrings);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntityHelper.getZipResponeEntity(topTaxisLineStrings, month, limit);
    }

}
