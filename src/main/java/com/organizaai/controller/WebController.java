package com.organizaai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping({"/", "/home"})
    public String abrirHome() {
        // O Spring vai procurar o arquivo "home.html" na pasta templates
        return "home";
    }

    @GetMapping("/dashboard")
    public String abrirDashboard() { return "dashboard"; }

    // Quando o usuário digitar /painel no navegador, o Spring entrega o HTML
    @GetMapping("/painel")
    public String abrirPainel() {
        return "painel";
    }

    @GetMapping("/registrar")
    public String abrirRegistro() {
        return "registrar"; // Vai buscar registrar.html em templates
    }

    @GetMapping("/login")
    public String abrirLogin() {
        return "login"; // Procura login.html em templates
    }

    @GetMapping("/mfa-setup")
    public String abrirSetupMfa() {
        return "mfa-setup"; // Busca o mfa-setup.html
    }

    @GetMapping("/novo-evento")
    public String abrirNovoEvento() {
        return "novo-evento";
    }

    @GetMapping("/esqueci-senha")
    public String abrirEsqueciSenha() {
        return "esqueci-senha";
    }

    @GetMapping("/redefinir-senha")
    public String abrirRedefinirSenha() {
        return "redefinir-senha";
    }
}
