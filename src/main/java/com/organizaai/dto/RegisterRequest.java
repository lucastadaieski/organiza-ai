package com.organizaai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Nome é obrigatório")
		@Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
		String nome,

		@NotBlank(message = "Email é obrigatório")
		@Email(message = "Email deve ser válido")
		String email,

		@NotBlank(message = "Senha é obrigatória")
		@Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
		String senha,

		@NotNull Boolean consentimentoAceito
) {}
