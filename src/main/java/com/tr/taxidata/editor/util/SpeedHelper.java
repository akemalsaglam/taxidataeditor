package com.tr.taxidata.editor.util;

public class SpeedHelper {

    private static double KILOMETER_TO_METER = 1000;
    private static double HOUR_TO_SECOND = 60 * 60;

    public static double changeSpeedToMeterDivideSecond(double speedKilometerDivideHour) {
        return Math.round((speedKilometerDivideHour * KILOMETER_TO_METER) / HOUR_TO_SECOND);
    }

}
