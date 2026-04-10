package com.skillbridge.skillbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({"/signup", "/login", "/dashboard", "/profile-setup"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
