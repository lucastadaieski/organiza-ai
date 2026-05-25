package com.organizaai.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
public class AesEncryptor implements AttributeConverter<String, String> {

    // Chave de 32 caracteres (256 bits) para o algoritmo AES
    private static final String SECRET = "Meus32CaracteresSecretosParaAES!";
    private final SecretKeySpec keySpec;

    public AesEncryptor() {
        // Inicializa a chave de criptografia
        this.keySpec = new SecretKeySpec(SECRET.getBytes(), "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            // Retorna o texto embaralhado em Base64 para salvar no banco
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar dado em repouso", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            // Retorna o texto original quando o Java for ler do banco
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dado do banco", e);
        }
    }
}