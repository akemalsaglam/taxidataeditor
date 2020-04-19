package com.tr.taxidata.editor.util;

import java.math.BigDecimal;

public class SpeedHelper {

    private static double KILOMETER_TO_METER = 1000;
    private static double HOUR_TO_SECOND = 60 * 60;
    private static int PRECISION = 1;

    public static double changeSpeedToMeterDivideSecond(double speedKilometerDivideHour) {
        BigDecimal bd = new BigDecimal(Double.toString((speedKilometerDivideHour * KILOMETER_TO_METER) / HOUR_TO_SECOND));
        bd = bd.setScale(PRECISION, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

}
