package com.medscan.app_med.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HomeController {

    // Ruta para la página de Inicio
    @GetMapping("/")
    public String index() {
        return "home"; // Busca templates/home.html
    }

    // Ruta para la Vista Principal del Usuario
    @GetMapping("/dashboard")
    public String dashboard() {
        return "user"; // Busca templates/user.html
    }
}

