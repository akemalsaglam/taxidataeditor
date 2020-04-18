package com.tr.taxidata.editor.util;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

public class ResponseEntityHelper {

    public static ResponseEntity<ByteArrayResource> getZipResponeEntity(Map<String, byte[]> topTaxisLineStrings, int month, long limit) throws IOException {
        byte[] data = ZipHelper.zipBytes(topTaxisLineStrings);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=taxi_lineStrings_month" + month + "_top" + limit + ".zip")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .contentLength(data.length)
                .body(resource);
    }

}
