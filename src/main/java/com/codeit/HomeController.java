package com.codeit; 

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showLoginPage() {
        System.out.println("inside / ");
        return "login";  // no `.html` needed, Thymeleaf will find templates/login.html
    }
}
