package com.tr.taxidata.editor.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(path = "/dummy")
public class DummyController {

    @PostMapping(path = "/")
    public List<String> getDummy(@RequestBody String clusters) {
        return Collections.emptyList();
    }
}
