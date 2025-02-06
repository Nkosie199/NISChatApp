package com.mynger.mychatapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CommandController {

    // Serve the main UI
    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("title", "MyChatApp");
        return "index";
    }

    @GetMapping("/server")
    public String serverPage() {
        return "server";
    }

    @GetMapping("/client")
    public String clientPage(Model model) {
        return "client";
    }

}
