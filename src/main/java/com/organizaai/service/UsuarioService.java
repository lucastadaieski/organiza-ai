package com.organizaai.service;

import com.organizaai.enums.Role;
import com.organizaai.model.Usuario;
import com.organizaai.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;


    public void cadastrar(Usuario usuario) {

        //Verifica se o e-mail já existe no banco para evitar duplicidade.
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Este e-mail já está em uso.");
        }

        //Criptografa a senha antes de salvar no banco.
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        //Define a role user como padrão.
        if (usuario.getRole() == null) {
            usuario.setRole(Role.USER);
        }

        usuarioRepository.save(usuario);
    }

    public Usuario atualizarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    //Verifica se a senha fornecida no login corresponde ao hash armazenado no banco.
    public boolean validarSenha(String email, String senhaPlana) {
        return usuarioRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(senhaPlana, user.getPassword()))
                .orElse(false);
    }
}
