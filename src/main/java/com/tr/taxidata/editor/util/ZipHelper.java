package com.tr.taxidata.editor.util;

import com.tr.taxidata.editor.model.TaxiDataCountDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

    public static byte[] zipBytes(Map<String, byte[]> topTaxisLineStrings) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        topTaxisLineStrings.entrySet().forEach(lineStringEntry -> {
            ZipEntry entry = new ZipEntry("taxi-" + lineStringEntry.getKey() + ".wkt");
            entry.setSize(lineStringEntry.getValue().length);
            try {
                zos.putNextEntry(entry);
                zos.write(lineStringEntry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }

}
