package com.tr.taxidata.editor.controller;

import com.tr.taxidata.editor.model.TaxiDataCountDto;
import com.tr.taxidata.editor.service.TaxiDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/taxidatacount")
public class TaxiDataCountController {

    private TaxiDataService taxiDataService;

    public TaxiDataCountController(TaxiDataService taxiDataService) {
        this.taxiDataService = taxiDataService;
    }

  /*  @GetMapping(path = "/month/{month}/top")
    public List<TaxiDataCountDto> getMonthTopTaxis(@PathVariable("month") long month) throws IOException {
        return taxiDataService.getMonthTopTaxisByLimit(month);
    }*/

}
