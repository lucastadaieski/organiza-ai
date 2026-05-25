// static/js/registrar.js

document.getElementById('form-registro').addEventListener('submit', async (e) => {
    e.preventDefault(); // Impede a página de recarregar

    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;
    const confirmarSenha = document.getElementById('confirmar-senha').value;
    const msgErro = document.getElementById('mensagem-erro');

    // Captura o estado do checkbox de consentimento LGPD
    const consentimento = document.getElementById('consentimento').checked;

    if (senha !== confirmarSenha) {
        msgErro.innerText = "As senhas não coincidem. Tente novamente.";
        msgErro.classList.remove('hidden');
        return;
    }

    // Validação extra de segurança (caso o usuário burle o HTML)
    if (!consentimento) {
        msgErro.innerText = "Você precisa aceitar os Termos de Uso e Política de Privacidade.";
        msgErro.classList.remove('hidden');
        return;
    }

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
                senha: senha,
                // Envia a prova do consentimento para o Java salvar no banco (Itens 4.6 e 4.7)
                consentimentoAceito: consentimento
            })
        });

        if (response.ok) {
            // Sucesso! Redireciona para o login
            alert("Conta criada com sucesso! Agora faça seu login.");
            window.location.href = "/login";
        } else {
            // Se der erro (ex: e-mail já existe)
            // Tenta ler o JSON, mas se vier texto puro (Spring padrão), trata o erro
            const contentType = response.headers.get("content-type");
            let erroMsg = "Erro ao registrar. Verifique os dados.";

            if (contentType && contentType.indexOf("application/json") !== -1) {
                const data = await response.json();
                erroMsg = data.mensagem || erroMsg;
            } else {
                erroMsg = await response.text() || erroMsg;
            }

            msgErro.innerText = erroMsg;
            msgErro.classList.remove('hidden');
        }

    } catch (error) {
        console.error("Erro na requisição:", error);
        msgErro.innerText = "Servidor offline ou erro de rede.";
        msgErro.classList.remove('hidden');
    }
});