package com.tr.taxidata.editor.util;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

public class ResponseEntityHelper {

    public static ResponseEntity<ByteArrayResource> getZipResponeEntity(Map<String, byte[]> fileNameAndContent, int month, long limit, String zipName) throws IOException {
        byte[] data = ZipHelper.zipBytes(fileNameAndContent);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipName)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .contentLength(data.length)
                .body(resource);
    }

}
