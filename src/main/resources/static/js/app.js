document.addEventListener("DOMContentLoaded", () => {
    // Carrega os dados iniciais se houver token salvo
    if (localStorage.getItem("token")) {
        carregarInsumos();
        carregarBebidas();
        carregarItensAvulsos();
        carregarProdutos();
    }

    // ==========================================
    // CONECTANDO OS BOTÕES DA TELA DE LOGIN
    // ==========================================
    // Substitua os IDs abaixo pelos IDs reais que estão no seu HTML!
    const btnEntrar = document.getElementById("btnEntrar");
    const btnCadastrar = document.getElementById("btnCadastrar");
    const btnEsqueceu = document.getElementById("btnEsqueceu");

    // Adiciona o evento de clique no botão Entrar
    if (btnEntrar) {
        btnEntrar.addEventListener("click", fazerLogin);
    }

    // Adiciona o evento de clique no botão de Cadastro
    if (btnCadastrar) {
        btnCadastrar.addEventListener("click", (e) => {
            e.preventDefault();
            // Como a IA não fez a função, coloquei um aviso. 
            // Se você tiver uma tela separada, mude para: window.location.href = "cadastro.html";
            alert("A função de cadastro ainda precisa ser implementada para comunicar com a API!");
        });
    }

    // Adiciona o evento de clique no botão de Esqueci a Senha
    if (btnEsqueceu) {
        btnEsqueceu.addEventListener("click", (e) => {
            e.preventDefault();
            alert("A tela ou função de recuperação de senha precisa ser criada!");
        });
    }
});
