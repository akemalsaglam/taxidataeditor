package com.tr.taxidata.editor.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

    public static byte[] zipBytes(Map<String, byte[]> fileNameAndContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        fileNameAndContent.entrySet().forEach(lineStringEntry -> {
            ZipEntry entry = new ZipEntry(lineStringEntry.getKey());
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
