package com.mynger.mychatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.mynger.mychatapp.service.ServerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/server")
public class ServerController {

    @Autowired
    private ServerService serverService;

    @GetMapping
    public ModelAndView startServer() {
        ModelAndView modelAndView = new ModelAndView("server-logs");
        modelAndView.addObject("logOutput", serverService.getMessages());
        return modelAndView;
    }
}
