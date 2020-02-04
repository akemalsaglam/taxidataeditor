package com.tr.taxidata.editor.controller;

import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import com.tr.taxidata.editor.parser.*;
import com.tr.taxidata.editor.service.TaxiDataService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @GetMapping(path = "/{id}/monthly/{month}/linestring/download")
    @ResponseBody
    public String getMonthlyDataByTaxiIdLineStringDownload(@PathVariable("id") Long id, @PathVariable("month") int month, HttpServletResponse response) throws IOException {
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
        LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        return lineStringWriter.write();
    }

    @GetMapping(path = "monthly/{month}/linestring/top")
    public List<String> getMonthlyDataByTaxiIdLineStringForTop(@PathVariable("month") int month) throws IOException {
        List<TaxiDataCountDto> topTaxis = taxiDataService.getMonthTopTaxis(month);
        List<String> lineString = new ArrayList<>();
        topTaxis.parallelStream().forEach(taxi -> {
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
            LineStringWriter lineStringWriter = new LineStringWriter(landmarks, closestLandmark);
            lineString.add(lineStringWriter.write());
        });
        return lineString;
    }

    @GetMapping(path = "/{id}/monthly/{month}/arff")
    public String getMonthlyDataByTaxiIdArff(@PathVariable("id") Long id, @PathVariable("month") int month) {
        System.out.println("fetching data from db...");
        List<TaxiData> taxiData = taxiDataService.getMonthlyDataByTaxiIdAndMonth(id, month);

        System.out.println("parsing linestrings...");
        List<String> taxiDataPositions = taxiData.stream().flatMap(p -> Stream.of(p.getPosition())).collect(Collectors.toList());
        LineStringParser lineStringParser = new LineStringParser(taxiDataPositions);
        lineStringParser.parse();

        System.out.println("transforming latitudes/longitudes to x,y coordinates...");
        List<Landmark> landmarks = CoordinateTrasformator.transformCoordinates(lineStringParser.getLandmarks());

        System.out.println("writing weka results...");
        WekaWriter wekaWriter = new WekaWriter(landmarks);
        return wekaWriter.write();
    }

}
