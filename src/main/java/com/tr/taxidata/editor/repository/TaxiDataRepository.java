package com.tr.taxidata.editor.repository;


import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaxiDataRepository extends JpaRepository<TaxiData, Long> {

    Optional<TaxiData> getById(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month1 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00'", nativeQuery = true)
    List<TaxiData> getMonth1DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month2_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth2DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month3_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth3DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month4_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth4DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month5_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth5DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month6_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth6DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month7_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth7DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month8_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth8DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month9_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth9DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month10_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth10DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month11_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth11DataByTaxiId(Long id);

    @Query(value = "select * from public.taxi_data_month12_view tdmv where tdmv.taxi_id=?1", nativeQuery = true)
    List<TaxiData> getMonth12DataByTaxiId(Long id);

    @Query(value = "select btd.taxi_id as taxiId,count(*) as logCount from public.bursa_taxi_data_month1 btd group by btd.taxi_id order by logCount desc limit 10", nativeQuery = true)
    List<Object[]> getMonth1TopTaxis();
}