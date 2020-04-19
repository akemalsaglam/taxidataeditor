package com.tr.taxidata.editor.util;

import com.tr.taxidata.editor.model.TaxiDataCountDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TaxiDataHelper {

    public static void printSettingsByTaxi(List<TaxiDataCountDto> topTaxis) {
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

    public static String getSimulationSettingsForTaxi(List<TaxiDataCountDto> topTaxis, Map<String, Long> meanSpeedByTaxi) {
        AtomicInteger index = new AtomicInteger(1);
        List<String> settings = new ArrayList<>();
        topTaxis.forEach(taxi -> {
            double speedToMeterDivideSecond = SpeedHelper.changeSpeedToMeterDivideSecond(meanSpeedByTaxi.get(taxi.getTaxiId()));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Group").append(index.get()).append(".groupID = taxi").append(index.get()).append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".hostName = taxi-").append(taxi.getTaxiId()).append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".okMaps = 1").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".speed = ").append(speedToMeterDivideSecond-10).append(", ").append(speedToMeterDivideSecond+10).append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".nrofHosts = 1").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".movementModel = MapRouteMovement").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".routeFile = data/custom/taxidata/bursa-0101/taxi-").append(taxi.getTaxiId()).append(".wkt").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".routeType = 1").append(System.lineSeparator());
            stringBuilder.append("Group").append(index.get()).append(".routeFirstStop = 0").append(System.lineSeparator());
            stringBuilder.append(" ").append(System.lineSeparator());
            index.getAndIncrement();
            settings.add(stringBuilder.toString());
        });
        return settings.stream().collect(Collectors.joining(System.lineSeparator()));
    }
}
