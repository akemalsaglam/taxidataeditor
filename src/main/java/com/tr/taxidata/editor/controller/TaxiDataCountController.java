package com.tr.taxidata.editor.controller;

import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import com.tr.taxidata.editor.parser.*;
import com.tr.taxidata.editor.service.TaxiDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/taxidatacount")
public class TaxiDataCountController {

    private TaxiDataService taxiDataService;

    public TaxiDataCountController(TaxiDataService taxiDataService) {
        this.taxiDataService = taxiDataService;
    }

    @GetMapping(path = "/month/{month}/top")
    public List<TaxiDataCountDto> getMonthTopTaxis(@PathVariable("month") int month) throws IOException {
        return taxiDataService.getMonthTopTaxis(month);
    }

}
