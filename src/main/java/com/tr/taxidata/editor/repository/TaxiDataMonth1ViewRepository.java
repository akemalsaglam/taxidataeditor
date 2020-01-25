package com.tr.taxidata.editor.repository;


import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaxiDataMonth1ViewRepository extends JpaRepository<TaxiData, Long> {

    Optional<TaxiData> getById(Long id);

    @Query(value = "select * from public.taxi_data_month1_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth1DataByTaxiId(Long id);

    @Query(value = "select btd.taxi_id as taxiId,count(*) as logCount from public.taxi_data_month1_view btd group by btd.taxi_id order by logCount desc limit 10", nativeQuery = true)
    List<TaxiDataCountDto> getMonth1TopTaxis();

}