package com.organizaai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void enviarEmailRecuperacao(String destinatario, String token) {
        String linkDeRecuperacao = baseUrl + "/redefinir-senha?token=" + token;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom("suporte@organizaai.com.br");
        mensagem.setTo(destinatario);
        mensagem.setSubject("OrganizaAI - Recuperação de Senha");
        mensagem.setText("Olá!\n\n" +
                "Recebemos uma solicitação para redefinir sua senha no OrganizaAI.\n" +
                "Clique no link abaixo para criar uma nova senha:\n\n" +
                linkDeRecuperacao + "\n\n" +
                "Se você não solicitou isso, pode ignorar este e-mail em segurança.\n\n" +
                "Abraços,\nEquipe OrganizaAI");

        try {
            mailSender.send(mensagem);
            log.info("Email de recuperação enviado para: {}", destinatario);
        } catch (Exception e) {
            log.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }
}