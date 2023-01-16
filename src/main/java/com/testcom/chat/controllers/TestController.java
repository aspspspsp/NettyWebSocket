package com.testcom.chat.controllers;

import com.testcom.chat.repositories.UserGroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Controller
public class TestController {
    @Value("${netty.ws}")
    private String ws;

    @Resource
    private UserGroupRepository userGroupRepository;

    /**
     * 登入頁面
     */
    @RequestMapping("/login")
    public String login() {
        return "test/login";
    }

    /**
     * 登入後跳轉到測試主頁
     */
    @PostMapping("/login.do")
    public String login(@RequestParam Integer userId, HttpSession session, Model model) {
        model.addAttribute("ws", ws);
        session.setAttribute("userId", userId);
        model.addAttribute("groupList", userGroupRepository.findGroupIdByUserId(userId));

        return "test/index";
    }
}
