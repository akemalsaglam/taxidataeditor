package com.tr.taxidata.editor.service;

import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import com.tr.taxidata.editor.repository.TaxiDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TaxiDataService {

    @Autowired
    private TaxiDataRepository taxiDataRepository;

    public Optional<TaxiData> getById(Long id) {
        return taxiDataRepository.getById(id);
    }

    public List<TaxiData> findAll() {
        return taxiDataRepository.findAll();
    }

    public List<TaxiData> getMonthlyDataByTaxiIdAndMonth(Long id, int month) {
        List<TaxiData> taxiData = new ArrayList<>();
        switch (month) {
            case 1:
                taxiData = taxiDataRepository.getMonth1DataByTaxiId(id);
                break;
            case 2:
                taxiData = taxiDataRepository.getMonth2DataByTaxiId(id);
                break;
            case 3:
                taxiData = taxiDataRepository.getMonth3DataByTaxiId(id);
                break;
            case 4:
                taxiData = taxiDataRepository.getMonth4DataByTaxiId(id);
                break;
            case 5:
                taxiData = taxiDataRepository.getMonth5DataByTaxiId(id);
                break;
            case 6:
                taxiData = taxiDataRepository.getMonth6DataByTaxiId(id);
                break;
            case 7:
                taxiData = taxiDataRepository.getMonth7DataByTaxiId(id);
                break;
            case 8:
                taxiData = taxiDataRepository.getMonth8DataByTaxiId(id);
                break;
            case 9:
                taxiData = taxiDataRepository.getMonth9DataByTaxiId(id);
                break;
            case 10:
                taxiData = taxiDataRepository.getMonth10DataByTaxiId(id);
                break;
            case 11:
                taxiData = taxiDataRepository.getMonth11DataByTaxiId(id);
                break;
            case 12:
                taxiData = taxiDataRepository.getMonth12DataByTaxiId(id);
                break;
        }
        return taxiData;
    }

    public List<List<TaxiData>> getMonthlyWeek1DataByTaxiIdAndMonth(Long id, int month) throws ParseException {
        List<List<TaxiData>> taxiData = new ArrayList<>();
        switch (month) {
            case 1:
                for (int i = 1; i < 8; i++) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


                    String startDateString = "2019-01-0" + i + " 00:00:00";
                    String endDateString = "2019-01-0" + (i + 1) + " 00:00:00";

                    Date startDate = dateFormat.parse(startDateString);
                    Date endDate = dateFormat.parse(endDateString);

                    Timestamp startDateTimestamp = new java.sql.Timestamp(startDate.getTime());
                    Timestamp endDateTimestamp = new java.sql.Timestamp(endDate.getTime());

                    taxiData.add(taxiDataRepository.getMonth1Week1DataByTaxiId(id, startDateTimestamp, endDateTimestamp));
                }
                break;
            /*case 2:
                taxiData = taxiDataRepository.getMonth2DataByTaxiId(id);
                break;
            case 3:
                taxiData = taxiDataRepository.getMonth3DataByTaxiId(id);
                break;
            case 4:
                taxiData = taxiDataRepository.getMonth4DataByTaxiId(id);
                break;
            case 5:
                taxiData = taxiDataRepository.getMonth5DataByTaxiId(id);
                break;
            case 6:
                taxiData = taxiDataRepository.getMonth6DataByTaxiId(id);
                break;
            case 7:
                taxiData = taxiDataRepository.getMonth7DataByTaxiId(id);
                break;
            case 8:
                taxiData = taxiDataRepository.getMonth8DataByTaxiId(id);
                break;
            case 9:
                taxiData = taxiDataRepository.getMonth9DataByTaxiId(id);
                break;
            case 10:
                taxiData = taxiDataRepository.getMonth10DataByTaxiId(id);
                break;
            case 11:
                taxiData = taxiDataRepository.getMonth11DataByTaxiId(id);
                break;
            case 12:
                taxiData = taxiDataRepository.getMonth12DataByTaxiId(id);
                break;*/
        }
        return taxiData;
    }

    public List<TaxiDataCountDto> getMonthTopTaxisByLimit(int month, long limit) {
        List<Object[]> taxiData = new ArrayList<>();
        switch (month) {
            case 1:
                taxiData = taxiDataRepository.getMonth1TopTaxis(limit);
                break;
            /*case 2:
                taxiData = taxiDataRepository.getMonth2DataByTaxiId(id);
                break;
            case 3:
                taxiData = taxiDataRepository.getMonth3DataByTaxiId(id);
                break;
            case 4:
                taxiData = taxiDataRepository.getMonth4DataByTaxiId(id);
                break;
            case 5:
                taxiData = taxiDataRepository.getMonth5DataByTaxiId(id);
                break;
            case 6:
                taxiData = taxiDataRepository.getMonth6DataByTaxiId(id);
                break;
            case 7:
                taxiData = taxiDataRepository.getMonth7DataByTaxiId(id);
                break;
            case 8:
                taxiData = taxiDataRepository.getMonth8DataByTaxiId(id);
                break;
            case 9:
                taxiData = taxiDataRepository.getMonth9DataByTaxiId(id);
                break;
            case 10:
                taxiData = taxiDataRepository.getMonth10DataByTaxiId(id);
                break;
            case 11:
                taxiData = taxiDataRepository.getMonth11DataByTaxiId(id);
                break;
            case 12:
                taxiData = taxiDataRepository.getMonth12DataByTaxiId(id);
                break;*/
        }
        return taxiData.stream()
                .map(data -> new TaxiDataCountDto(Long.parseLong(data[0].toString()), Long.parseLong(data[1].toString())))
                .collect(Collectors.toList());
    }
}
