package com.mynger.mychatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.mynger.mychatapp.model.Client;
import com.mynger.mychatapp.service.ClientService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/client")
public class ClientController {
    @Autowired
    private ClientService clientService;

    // Start the client
    @PostMapping("/start")
    @ResponseBody
    public ModelAndView startClient(@RequestParam("host") String host, @RequestParam("port") int port) {
        clientService.runClient(host, port); // Start server and get log output
        ModelAndView modelAndView = new ModelAndView("client-running"); // Points to server.html
        modelAndView.addObject("port", port);
        modelAndView.addObject("logOutput", clientService.getLogMessages());
        return modelAndView;
    }
}
