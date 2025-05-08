package com.mynger.mychatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.mynger.mychatapp.service.MessageService;
import com.mynger.mychatapp.service.ServerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;
    @SuppressWarnings("unused")
    @Autowired
    private ServerService serverService;

    @GetMapping
    public ModelAndView startServer() {
        ModelAndView modelAndView = new ModelAndView("messages");
        modelAndView.addObject("messages", messageService.getAll());
        return modelAndView;
    }
}
