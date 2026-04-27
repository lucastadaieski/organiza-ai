package com.organizaai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarEmailRecuperacao(String destinatario, String token) {
        String linkDeRecuperacao = "http://localhost:8080/redefinir-senha?token=" + token;

        SimpleMailMessage mensagem = new SimpleMailMessage();

        // A SOLUÇÃO ESTÁ AQUI: Usando um e-mail com formato válido
        mensagem.setFrom("suporte@organizaai.com.br");

        mensagem.setTo(destinatario);
        mensagem.setSubject("OrganizaAI - Recuperação de Senha");
        mensagem.setText("Olá!\n\n" +
                "Recebemos uma solicitação para redefinir sua senha no OrganizaAI.\n" +
                "Clique no link abaixo para criar uma nova senha:\n\n" +
                linkDeRecuperacao + "\n\n" +
                "Se você não solicitou isso, pode ignorar este e-mail em segurança.\n\n" +
                "Abraços,\nEquipe OrganizaAI");

        // Dispara o e-mail!
        mailSender.send(mensagem);
    }
}