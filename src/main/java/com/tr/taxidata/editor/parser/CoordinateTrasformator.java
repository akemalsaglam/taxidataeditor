package com.tr.taxidata.editor.parser;

import java.math.BigDecimal;
import java.util.List;

public class CoordinateTrasformator {

    private static int precisonFloating = 3; // use 3 decimals after comma for rounding

    public static List<Landmark> transformCoordinates(List<Landmark> landmarks) {
        double latMin, lonMin;
        latMin = 39.9407938;
        lonMin = 28.6256705;

        for (Landmark l : landmarks) {
            l.x = geoDistance(l.latitude, l.longitude, l.latitude, lonMin);
            l.y = geoDistance(l.latitude, l.longitude, latMin, l.longitude);
        }

        return landmarks;
    }

    private static double geoDistance(double lat1, double lon1, double lat2, double lon2) {
        // return distance between two gps fixes in meters
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = (R * c * 1000.0d);
        distance = round(distance, precisonFloating);
        return distance;
    }

    private static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
