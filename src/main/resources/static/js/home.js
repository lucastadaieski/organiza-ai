// static/js/home.js

document.addEventListener("DOMContentLoaded", () => {
    // 1. Tenta recuperar o token (que você salvaria após o login)
    // Por enquanto, você pode testar mudando isso manualmente para null ou para um texto
    const token = localStorage.getItem("token");

    const btnLogin = document.getElementById("btn-login");
    const btnRegistrar = document.getElementById("btn-registrar");
    const btnPainel = document.getElementById("btn-painel");
    const heroBtn = document.getElementById("hero-main-btn");

    if (token) {
        // Usuário logado: mostra o link pro painel
        btnPainel.classList.remove("hidden");
        heroBtn.innerText = "Ir para meus Eventos";
        heroBtn.href = "/painel";
    } else {
        // Usuário visitante: mostra login e registro
        btnLogin.classList.remove("hidden");
        btnRegistrar.classList.remove("hidden");
    }
});