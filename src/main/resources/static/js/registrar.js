// static/js/registrar.js

document.getElementById('form-registro').addEventListener('submit', async (e) => {
    e.preventDefault(); // Impede a página de recarregar

    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;
    const confirmarSenha = document.getElementById('confirmar-senha').value;
    const msgErro = document.getElementById('mensagem-erro');

    if (senha !== confirmarSenha) {
        msgErro.innerText = "As senhas não coincidem. Tente novamente.";
        msgErro.classList.remove('hidden');
        return;
    }

// Se passou, segue com o fetch normal para o /auth/register...

    try {
        // Chamada para o seu @RestController de autenticação
        const response = await fetch('/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nome: nome,
                email: email,
                senha: senha
            })
        });

        if (response.ok) {
            // Sucesso! Redireciona para o login
            alert("Conta criada com sucesso! Agora faça seu login.");
            window.location.href = "/login";
        } else {
            // Se der erro (ex: e-mail já existe)
            const data = await response.json();
            msgErro.innerText = data.mensagem || "Erro ao registrar. Verifique os dados.";
            msgErro.classList.remove('hidden');
        }

    } catch (error) {
        console.error("Erro na requisição:", error);
        msgErro.innerText = "Servidor offline ou erro de rede.";
        msgErro.classList.remove('hidden');
    }
});