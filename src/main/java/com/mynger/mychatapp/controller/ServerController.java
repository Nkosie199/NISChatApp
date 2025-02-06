package com.mynger.mychatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.mynger.mychatapp.service.ServerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/server")
public class ServerController {

    @Autowired
    private ServerService serverService;

    @PostMapping("/start")
    public ModelAndView startServer(@RequestParam(defaultValue = "4444") String port) {
        int portNumber = Integer.parseInt(port);
        log.info(serverService.startServer(portNumber)); // Start server and get log output
        serverService.runServer(portNumber); // This method will set serverService.getLogMessages()
        ModelAndView modelAndView = new ModelAndView("server-running"); // Points to server-running.html
        modelAndView.addObject("port", port);
        modelAndView.addObject("logOutput", serverService.getLogMessages());
        return modelAndView;
    }

    @PostMapping("/stop")
    public String stopServer() {
        return serverService.stopServer();
    }
}
