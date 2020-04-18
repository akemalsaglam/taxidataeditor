package com.tr.taxidata.editor.util;

import java.util.List;

public class ArffHelper {

    public static StringBuilder getArffStringBuilder(List<StringBuilder> arffStringWithoutHeader) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@RELATION taxi");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("@ATTRIBUTE xValue REAL");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("@ATTRIBUTE yValue REAL");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("@DATA");
        stringBuilder.append(System.getProperty("line.separator"));
        arffStringWithoutHeader.forEach(stringBuilder::append);
        return stringBuilder;
    }

}
