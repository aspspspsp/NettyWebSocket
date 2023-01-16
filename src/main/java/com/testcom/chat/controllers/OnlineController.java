package com.testcom.chat.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OnlineController {
    @Value("${netty.ws}")
    private String ws;

    /**
     * 客服界面
     */
    @GetMapping(value = {"/index", "/customer","/"})
    public String index(Model model) {
        model.addAttribute("ws", ws);
        return "customer";
    }


    /**
     * 游客页面
     */
    @GetMapping("/tourist")
    public String tourist(Model model) {
        model.addAttribute("ws", ws);
        return "tourist";
    }

}
