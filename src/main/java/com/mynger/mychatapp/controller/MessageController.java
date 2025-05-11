package com.mynger.mychatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.mynger.mychatapp.dto.MessageDTO;
import com.mynger.mychatapp.service.MessageService;
import com.mynger.mychatapp.service.ServerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;
    @SuppressWarnings("unused")
    @Autowired
    private ServerService serverService;

    @GetMapping
    public ModelAndView getAllMessages() {
        ModelAndView modelAndView = new ModelAndView("messages");
        modelAndView.addObject("messages", messageService.getAll());
        return modelAndView;
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("message", new MessageDTO());
        return "messages/add";
    }

    @PostMapping("/add")
    public String handleAdd(@ModelAttribute("message") MessageDTO message) {
        messageService.createMessage(message);
        return "redirect:/messages";
    }

}
