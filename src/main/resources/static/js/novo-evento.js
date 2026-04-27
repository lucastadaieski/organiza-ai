// 1. Função para adicionar novos campos de data dinamicamente
function adicionarCampoData() {
    const container = document.getElementById('container-datas');

    const novaDiv = document.createElement('div');
    novaDiv.className = 'flex gap-2 data-input-group animate-fade-in';

    novaDiv.innerHTML = `
        <input type="date" required class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-indigo-500 outline-none transition-all">
        <button type="button" onclick="this.parentElement.remove()" class="px-4 py-3 bg-red-50 text-red-600 rounded-xl hover:bg-red-100 font-bold">
            X
        </button>
    `;

    container.appendChild(novaDiv);
}

// 2. Intercepta o envio do formulário
document.getElementById('form-evento').addEventListener('submit', async (e) => {
    e.preventDefault();

    const token = localStorage.getItem('token');
    if (!token) {
        alert("Sua sessão expirou.");
        window.location.href = "/login";
        return;
    }

    const btnSubmit = document.getElementById('btn-submit');
    const msgErro = document.getElementById('msg-erro');
    btnSubmit.innerText = "Criando...";
    msgErro.classList.add('hidden');

    // Monta os dados
    const titulo = document.getElementById('titulo').value;
    const descricao = document.getElementById('descricao').value;

    // Captura todas as datas digitadas transformando em um array
    const inputsData = document.querySelectorAll('.data-input-group input[type="date"]');
    const datasSugeridas = Array.from(inputsData).map(input => input.value);

    const payload = {
        titulo: titulo,
        descricao: descricao,
        datas: datasSugeridas // O seu Java (DTO) precisa esperar uma List<String> ou List<LocalDate> chamada "datas"
    };

    try {
        const response = await fetch('/eventos', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("Evento criado com sucesso!");
            window.location.href = "/dashboard"; // Volta pro Dashboard para ver o novo evento!
        } else {
            const erroData = await response.json();
            msgErro.innerText = erroData.mensagem || "Erro ao criar evento. Verifique os dados.";
            msgErro.classList.remove('hidden');
            btnSubmit.innerText = "Criar e Gerar Link";
        }
    } catch (error) {
        msgErro.innerText = "Erro ao conectar com o servidor.";
        msgErro.classList.remove('hidden');
        btnSubmit.innerText = "Criar e Gerar Link";
    }
});