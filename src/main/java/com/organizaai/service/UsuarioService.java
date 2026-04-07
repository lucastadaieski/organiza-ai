package com.organizaai.service;

import com.organizaai.enums.Role;
import com.organizaai.model.Usuario;
import com.organizaai.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public Usuario salvarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public boolean validarSenha(String email, String senhaPlana) {
        // Busca o usuário no banco pelo e-mail
        return usuarioRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(senhaPlana, user.getPassword()))
                .orElse(false);
    }

    public void cadastrar(Usuario usuario) {
        // 1. Criptografa a senha (Requisito 1.2)
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 2. Define o Enum corretamente
        if (usuario.getRole() == null) {
            usuario.setRole(Role.USER);
        }

        usuarioRepository.save(usuario);
    }
}
