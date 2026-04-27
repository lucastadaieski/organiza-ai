document.addEventListener('DOMContentLoaded', () => {
    const nomeSalvo = localStorage.getItem('nome_usuario');
    if (nomeSalvo) {
        document.getElementById('nav-nome-usuario').innerText = nomeSalvo;
    }
    // Dispara a carga de eventos assim que a página abre
    carregarEventos();
});

async function fazerLogout() {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
        await fetch('/auth/logout', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
    } catch (e) {
        console.error("Erro ao fazer logout", e);
    } finally {
        localStorage.removeItem('token');
        localStorage.removeItem('nome_usuario');
        window.location.href = "/login";
    }
}

async function carregarEventos() {
    const token = localStorage.getItem('token');
    const container = document.getElementById('lista-eventos');

    try {
        const response = await fetch('/eventos/meus', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        // Verificação de segurança
        if (!response.ok) {
            console.error("Erro na busca de eventos:", response.status);
            return;
        }

        const eventos = await response.json();

        // DEBUG: Aqui você consegue ver no console o que o Java está enviando!
        console.log("Eventos vindos do Java:", eventos);

        container.innerHTML = '';

        if (eventos.length === 0) {
            container.innerHTML = '<p class="text-slate-500">Você ainda não criou nenhum evento.</p>';
            return;
        }

        eventos.forEach(evento => {
            // DICA: Verifique se no console aparece 'titulo' ou 'nome' e ajuste abaixo
            const tituloEvento = evento.titulo || evento.nome || 'Sem título';

            container.innerHTML += `
                <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 hover:shadow-md transition-all cursor-pointer" 
                     onclick="irParaEvento(${evento.id})">
                    <h3 class="text-xl font-bold text-slate-800">${tituloEvento}</h3>
                    <p class="text-slate-500 text-sm mt-2">${evento.descricao || 'Sem descrição'}</p>
                    <div class="mt-4 flex justify-between items-center">
                        <span class="text-xs font-bold px-2 py-1 bg-indigo-50 text-indigo-600 rounded">Doodle</span>
                        <span class="text-indigo-600 font-bold">Ver detalhes →</span>
                    </div>
                </div>
            `;
        });
    } catch (error) {
        console.error("Erro ao carregar eventos:", error);
    }
}

function irParaEvento(id) {
    window.location.href = `/painel?id=${id}`;
}