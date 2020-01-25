package com.tr.taxidata.editor.model;

public class TaxiDataCountDto {

    private Long taxiId;
    private Long logCount;

    public TaxiDataCountDto(Long taxiId, Long logCount) {
        this.taxiId = taxiId;
        this.logCount = logCount;
    }

    public Long getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(Long taxiId) {
        this.taxiId = taxiId;
    }

    public Long getLogCount() {
        return logCount;
    }

    public void setLogCount(Long logCount) {
        this.logCount = logCount;
    }
}
