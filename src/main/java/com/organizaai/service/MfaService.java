package com.organizaai.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // 1. Gera uma nova chave secreta para o usuário
    public String gerarSecret() {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey(); // Em versões recentes, use getKey() em vez de getSecret()
    }

    // 2. Gera a URL do QR Code
    public String gerarQrCodeUrl(String secret, String email) {
        // Criamos um objeto de chave a partir do secret salvo
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret).build();

        // O primeiro parâmetro é o nome da sua aplicação que aparecerá no celular do usuário
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL("OrganizaAI", email, key);
    }

    // 3. Valida se o código de 6 dígitos que o usuário digitou bate com o segredo dele
    public boolean validarCodigo(String secret, int codigo) {
        return gAuth.authorize(secret, codigo);
    }

    public boolean verificarCodigo(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        // Converte o código String para Integer se a biblioteca exigir
        try {
            int codeInt = Integer.parseInt(code);
            return gAuth.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
