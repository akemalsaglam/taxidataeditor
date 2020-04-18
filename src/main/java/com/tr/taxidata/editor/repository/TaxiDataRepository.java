package com.tr.taxidata.editor.repository;


import com.tr.taxidata.editor.model.TaxiData;
import com.tr.taxidata.editor.model.TaxiDataCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface TaxiDataRepository extends JpaRepository<TaxiData, Long> {

    Optional<TaxiData> getById(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month1 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth1Day1DataByTaxiId(Long id);

    /*@Query(value = "select * from public.bursa_taxi_data_month2 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth2DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month3 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth3DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month4 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth4DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month5 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth5DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month6 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth6DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month7 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth7DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month8 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth8DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month9 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth9DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month10 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth10DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month11 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth11DataByTaxiId(Long id);

    @Query(value = "select * from public.bursa_taxi_data_month12 tdmv where tdmv.taxi_id=?1 and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-02 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth12DataByTaxiId(Long id);*/

    @Query(value = "select btd.taxi_id as taxiId,count(*) as logCount from public.bursa_taxi_data_month1 btd group by btd.taxi_id order by logCount desc limit ?1", nativeQuery = true)
    List<Object[]> getMonth1TopTaxis(Long limit);

    @Query(value = "select * from public.bursa_taxi_data_month1 tdmv " +
            "where tdmv.taxi_id=?1 " +
            "and tdmv.date>'2019-01-01 00:00:00' and tdmv.date<'2019-01-08 00:00:00' order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth1Week1DataByTaxiId(Long taxiId);

    @Query(value = "select * from public.bursa_taxi_data_month1 tdmv " +
            "where tdmv.taxi_id=?1 " +
            "and tdmv.date > ?2 and tdmv.date < ?3 order by \"date\" asc", nativeQuery = true)
    List<TaxiData> getMonth1Week1DataByTaxiId(Long taxiId, Timestamp startDate, Timestamp endDate);
}