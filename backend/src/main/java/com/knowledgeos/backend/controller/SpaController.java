package com.knowledgeos.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/topics",
        "/topics/{id:\\d+}",
        "/revisions"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
