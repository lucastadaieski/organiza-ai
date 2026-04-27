// Memória para saber se estamos pedindo a senha (1) ou o código MFA (2)
let etapaAtual = 1;

document.getElementById('form-login').addEventListener('submit', async (e) => {
    e.preventDefault(); // Impede a página de recarregar

    const email = document.getElementById('email').value;
    const msgErro = document.getElementById('msg-erro');
    msgErro.classList.add('hidden'); // Esconde o erro sempre que tenta de novo

    // ==========================================
    // ETAPA 1: VALIDAR SENHA
    // ==========================================
    if (etapaAtual === 1) {
        const password = document.getElementById('senha').value;

        try {
            const resp = await fetch('/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }) // Usando password para bater com seu DTO
            });

            const data = await resp.json();

            if (resp.ok) {
                if (!data.mfaAtivo) {
                    // Nunca configurou: Manda para a tela do QR Code
                    window.location.href = `/mfa-setup?email=${email}`;
                } else {
                    // Já configurou: Prepara a tela para receber o código MFA

                    // 1. Esconde o campo de senha
                    document.getElementById('campo-senha').classList.add('hidden');

                    // 2. Mostra o campo do MFA
                    document.getElementById('campo-mfa').classList.remove('hidden');

                    // 3. Muda o texto do botão
                    document.querySelector('button[type="submit"]').innerText = "Verificar Código";

                    // 4. Avança a etapa para o próximo clique!
                    etapaAtual = 2;
                }
            } else {
                msgErro.innerText = data.erro || "E-mail ou senha incorretos.";
                msgErro.classList.remove('hidden');
            }
        } catch (error) {
            msgErro.innerText = "Erro ao conectar com o servidor.";
            msgErro.classList.remove('hidden');
        }
    }
        // ==========================================
        // ETAPA 2: VALIDAR CÓDIGO MFA E PEGAR O TOKEN
    // ==========================================
    else if (etapaAtual === 2) {
        // Pega o valor que o usuário digitou no novo campo
        const code = document.getElementById('mfa-code').value;

        try {
            // Note que aqui chamamos o /verify passando via URL (Query Params)
            const resp = await fetch(`/auth/login/verify?email=${email}&code=${code}`, {
                method: 'POST'
            });

            if (resp.ok) {
                const data = await resp.json();

                // O GRANDE MOMENTO: Salva o JWT no navegador!
                localStorage.setItem('token', data.token);
                localStorage.setItem('nome_usuario', data.nome);

                // Vai para a glória (Painel)
                window.location.href = "/dashboard";
            } else {
                msgErro.innerText = "Código MFA inválido. Tente novamente.";
                msgErro.classList.remove('hidden');
            }
        } catch (error) {
            msgErro.innerText = "Erro ao validar código MFA.";
            msgErro.classList.remove('hidden');
        }
    }
});