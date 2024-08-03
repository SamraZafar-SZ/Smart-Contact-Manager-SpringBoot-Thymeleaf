package com.smart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PreflightController {

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity handlePreflight() {
        return ResponseEntity.ok().build();
    }
}
