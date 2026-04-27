// static/js/doodle.js

// Lógica para pegar o ID da URL de forma dinâmica
const urlParams = new URLSearchParams(window.location.search);
const EVENTO_ID = urlParams.get('id');

async function carregarDoodle() {
    if (!EVENTO_ID) {
        alert("Evento não especificado!");
        window.location.href = "/dashboard";
        return;
    }

    const token = localStorage.getItem('token');
    // ... resto do código igual, usando a variável EVENTO_ID dinâmica ...
    const response = await fetch(`/eventos/${EVENTO_ID}/doodle`, { ... });
}

async function carregarDoodle() {
    // 1. Pega o token dinâmico que o login.js salvou
    const token = localStorage.getItem('token');

    // 2. Trava de segurança: Se não tem token, joga pro login
    if (!token) {
        alert("Sua sessão expirou ou você não está logado.");
        window.location.href = "/login";
        return;
    }

    try {
        const response = await fetch(`/eventos/${EVENTO_ID}/doodle`, {
            method: 'GET',
            headers: {
                // 3. Injeta o token dinâmico no cabeçalho
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        // 4. Se o Java der erro 401 ou 403 (Token expirado ou inválido)
        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('token'); // Limpa a sujeira
            window.location.href = "/login";
            return;
        }

        if (!response.ok) throw new Error("Erro ao buscar os dados do evento.");

        const doodleData = await response.json();
        desenharTela(doodleData);

    } catch (error) {
        console.error("Erro na requisição:", error);
        document.getElementById('container-datas').innerHTML =
            `<p class="text-red-500 font-bold">Erro ao carregar os dados. O servidor pode estar offline.</p>`;
    }
}

function desenharTela(doodle) {
    const badge = document.getElementById('status-badge');
    badge.innerText = doodle.statusDoodle;
    badge.className = doodle.statusDoodle === 'ABERTO'
        ? 'bg-green-100 text-green-800 text-xs font-bold px-2 py-1 rounded'
        : 'bg-red-100 text-red-800 text-xs font-bold px-2 py-1 rounded';

    const container = document.getElementById('container-datas');
    container.innerHTML = '';

    doodle.opcoes.forEach(opcao => {
        const nomesConfirmados = opcao.votosUsuarios
            .filter(voto => voto.disponivel)
            .map(voto => voto.nome)
            .join(', ');

        const cartaoHTML = `
            <div class="border rounded-xl p-5 flex flex-col justify-between hover:shadow-lg transition-shadow bg-white">
                <div>
                    <p class="text-2xl font-black text-slate-800">${formatarData(opcao.data)}</p>
                    <p class="text-sm text-slate-500 mt-2">
                        <span class="font-bold text-indigo-600">${opcao.totalVotosSim}</span> votos (Sim)
                    </p>
                    <p class="text-sm text-slate-400 mt-2 min-h-[2rem]">${nomesConfirmados ? '👍 ' + nomesConfirmados : 'Nenhum voto ainda'}</p>
                </div>
                <button onclick="votar(${opcao.id})" class="mt-4 w-full bg-indigo-50 hover:bg-indigo-600 hover:text-white text-indigo-700 font-bold py-2 px-4 border border-indigo-200 hover:border-indigo-600 rounded-lg transition-all active:scale-95">
                    Votar neste dia
                </button>
            </div>
        `;
        container.innerHTML += cartaoHTML;
    });
}

function formatarData(dataIso) {
    const partes = dataIso.split('-');
    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

// Para evitar erro no console caso o botão seja clicado antes de criarmos a função
function votar(opcaoId) {
    console.log("Clicou para votar na opção ID:", opcaoId);
    alert("Função de votar será implementada em breve!");
}

// Inicia a busca assim que o arquivo JS é carregado
carregarDoodle();