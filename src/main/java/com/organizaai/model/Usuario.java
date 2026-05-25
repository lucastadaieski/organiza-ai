package com.organizaai.model;

import com.organizaai.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import com.organizaai.util.AesEncryptor;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private int tentativasLogin = 0;
    private LocalDateTime bloqueadoAte;
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    @Convert(converter = AesEncryptor.class)
    private String mfaSecret;

    private boolean ativo = true;

    @Column(name = "consentimento_aceito")
    private Boolean consentimentoAceito;

    @Column(name = "consentimento_data")
    private LocalDateTime consentimentoData;

    @Column(name = "consentimento_versao")
    private String consentimentoVersao;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public boolean isBloqueado() {
        if (bloqueadoAte == null) return false;
        return bloqueadoAte.isAfter(LocalDateTime.now());
    }
}
