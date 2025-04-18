package com.kousen.cert.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Simple controller to handle the root path
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:index.html";
    }
}