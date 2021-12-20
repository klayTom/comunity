package com.kaly.controller;

import com.kaly.dto.PageinationDto;
import com.kaly.mapper.UserMapper;
import com.kaly.model.User;
import com.kaly.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {
    @Autowired
    UserMapper userMapper;
    @Autowired
    QuestionService questionService;


    @GetMapping("/profile/{action}")
    public String profile (HttpServletRequest request,
                           @PathVariable("action") String action,
                           Model model,
                           @RequestParam(value = "page",defaultValue = "1") Integer page,
                           @RequestParam(value = "size",defaultValue = "5") Integer size) {

        User user =(User) request.getSession().getAttribute("user");

        if (user==null) {
            return "redirect:/";
        }

        if ("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName","我的问题");
        }else if ("replies".equals(action)) {
            model.addAttribute("section","replies");
            model.addAttribute("sectionName","最新回复");
        }
        PageinationDto pageinationDto = questionService.list(user.getId(), page, size);
        model.addAttribute("pageination",pageinationDto);
        return "profile";
    }
}
