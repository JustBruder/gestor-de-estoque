// ==========================================
// CONFIGURAÇÃO GLOBAL DA API (CLEVER CLOUD)
// ==========================================
const API_URL = "https://app-443b811a-ae97-40b5-90ae-dbdcac27e57c.cleverapps.io/api";

document.addEventListener("DOMContentLoaded", () => {
    // Carrega os dados iniciais se houver token salvo
    if (localStorage.getItem("token")) {
        carregarInsumos();
        carregarBebidas();
        carregarItensAvulsos();
        carregarProdutos();
    }
});

function getAuthHeader() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}

// ==========================================
// AUTENTICAÇÃO (LOGIN & LOGOUT)
// ==========================================
async function fazerLogin(e) {
    if (e) e.preventDefault();
    const email = document.getElementById("loginEmail")?.value || document.getElementById("email")?.value;
    const senha = document.getElementById("loginSenha")?.value || document.getElementById("senha")?.value;

    if (!email || !senha) {
        alert("Preencha email e senha!");
        return;
    }

    try {
        const res = await fetch(`${API_URL}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, senha })
        });

        if (res.ok) {
            const data = await res.json();
            const token = data.token || data.tokenJWT || data.accessToken;
            if (token) {
                localStorage.setItem("token", token);
                window.location.reload();
            } else {
                alert("Erro ao obter token de acesso.");
            }
        } else {
            alert("Email ou senha inválidos.");
        }
    } catch (err) {
        console.error("Erro no login:", err);
        alert("Erro de conexão com o servidor.");
    }
}

function fazerLogout() {
    localStorage.removeItem("token");
    window.location.reload();
}

// ==========================================
// SEÇÃO: INSUMOS (ESTOQUE BASE)
// ==========================================
let listaIngredientesDisponiveis = [];

async function carregarInsumos() {
    try {
        const res = await fetch(`${API_URL}/ingredientes`, { headers: getAuthHeader() });
        if (!res.ok) return;
        const ingredientes = await res.json();
        
        listaIngredientesDisponiveis = ingredientes;

        // Renderiza na tabela de insumos (Visão do Estoque)
        const tabela = document.getElementById("tabelaInsumos") || document.getElementById("corpoTabelaInsumos");
        if (tabela) {
            tabela.innerHTML = ingredientes.map(ing => `
                <tr>
                    <td>${ing.id}</td>
                    <td>${ing.nome}</td>
                    <td>${ing.quantidadeEstoque !== undefined ? ing.quantidadeEstoque : (ing.quantidade || 0)}</td>
                    <td>${ing.unidadeMedida || 'UN'}</td>
                    <td><button class="btn-excluir" onclick="excluirIngrediente(${ing.id})">Excluir</button></td>
                </tr>
            `).join('');
        }
    } catch (e) {
        console.error("Erro ao carregar insumos:", e);
    }
}

async function salvarIngrediente(e) {
    if (e) e.preventDefault();
    const nome = document.getElementById("nomeInsumo")?.value;
    const quantidadeEstoque = document.getElementById("qtdInsumo")?.value;
    const unidadeMedida = document.getElementById("unidadeInsumo")?.value;

    if (!nome) return alert("Preencha o nome do insumo.");

    try {
        const res = await fetch(`${API_URL}/ingredientes`, {
            method: "POST",
            headers: getAuthHeader(),
            body: JSON.stringify({ 
                nome: nome, 
                quantidadeEstoque: parseFloat(quantidadeEstoque || 0), 
                unidadeMedida: unidadeMedida || 'UN' 
            })
        });

        if (res.ok) {
            alert("Insumo salvo com sucesso!");
            limparCamposInsumo();
            carregarInsumos();
        } else {
            alert("Erro ao salvar insumo.");
        }
    } catch (e) {
        console.error(e);
    }
}

function limparCamposInsumo() {
    if (document.getElementById("nomeInsumo")) document.getElementById("nomeInsumo").value = "";
    if (document.getElementById("qtdInsumo")) document.getElementById("qtdInsumo").value = "";
}

async function excluirIngrediente(id) {
    if (!confirm("Deseja excluir este insumo?")) return;
    try {
        const res = await fetch(`${API_URL}/ingredientes/${id}`, {
            method: "DELETE",
            headers: getAuthHeader()
        });
        if (res.ok) carregarInsumos();
    } catch (e) {
        console.error(e);
    }
}

// ==========================================
// SEÇÃO: MONTAGEM DE RECEITA NO LANCHE
// ==========================================
function adicionarLinhaIngrediente() {
    let container = document.getElementById("containerIngredientesLanche");
    if (!container) {
        // Se a div não existir no HTML, cria automaticamente antes do botão
        const btnCadastrar = document.querySelector('[onclick*="cadastrarLanche"]') || document.querySelector('button[type="submit"]');
        if (btnCadastrar && btnCadastrar.parentElement) {
            container = document.createElement("div");
            container.id = "containerIngredientesLanche";
            btnCadastrar.parentElement.insertBefore(container, btnCadastrar);
        } else {
            return;
        }
    }

    const div = document.createElement("div");
    div.className = "linha-ingrediente";
    div.style.display = "flex";
    div.style.gap = "8px";
    div.style.marginTop = "8px";

    let options = `<option value="">Selecione o Insumo</option>` + 
        listaIngredientesDisponiveis.map(ing => `<option value="${ing.id}">${ing.nome} (${ing.unidadeMedida || 'UN'})</option>`).join('');

    div.innerHTML = `
        <select class="select-ingrediente-id" style="flex: 2; padding: 6px; border-radius: 4px; background: #222; color: #fff; border: 1px solid #444;">
            ${options}
        </select>
        <input type="number" class="input-ingrediente-qtd" placeholder="Qtd" value="1" step="0.1" style="flex: 1; padding: 6px; border-radius: 4px; background: #222; color: #fff; border: 1px solid #444;">
        <button type="button" onclick="this.parentElement.remove()" style="background: #e74c3c; color: white; border: none; padding: 6px 10px; border-radius: 4px; cursor: pointer;">✕</button>
    `;
    container.appendChild(div);
}

// ==========================================
// SEÇÃO: PRODUTOS / LANCHES
// ==========================================
async function cadastrarLanche(e) {
    if (e) e.preventDefault();
    
    const nomeInput = document.getElementById("nomeLanche") || document.querySelector('input[placeholder*="X-Burguer"]');
    const precoInput = document.getElementById("precoLanche") || document.querySelector('input[placeholder*="25.00"]');

    const nome = nomeInput?.value;
    const preco = precoInput?.value;

    if (!nome || !preco) return alert("Preencha o nome e o preço do lanche.");

    const itensReceita = [];
    const linhas = document.querySelectorAll(".linha-ingrediente");

    linhas.forEach(linha => {
        const select = linha.querySelector(".select-ingrediente-id");
        const inputQtd = linha.querySelector(".input-ingrediente-qtd");

        if (select && select.value) {
            itensReceita.push({
                ingredienteId: parseInt(select.value),
                quantidadeNecessaria: parseFloat(inputQtd.value || 1)
            });
        }
    });

    const payload = {
        nome: nome,
        preco: parseFloat(preco),
        itensReceita: itensReceita
    };

    try {
        const res = await fetch(`${API_URL}/produtos`, {
            method: "POST",
            headers: getAuthHeader(),
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert("Lanche cadastrado com sucesso!");
            if (nomeInput) nomeInput.value = "";
            if (precoInput) precoInput.value = "";
            const container = document.getElementById("containerIngredientesLanche");
            if (container) container.innerHTML = "";
            carregarProdutos();
        } else {
            alert("Erro ao cadastrar lanche.");
        }
    } catch (e) {
        console.error("Erro ao cadastrar lanche:", e);
    }
}

async function carregarProdutos() {
    try {
        const res = await fetch(`${API_URL}/produtos`, { headers: getAuthHeader() });
        if (!res.ok) return;
        const produtos = await res.json();

        const container = document.getElementById("containerLanchesCadastrados") || document.getElementById("listaLanches");
        if (!container) return;

        container.innerHTML = produtos.map(prod => {
            const listaItens = prod.receita || prod.itensReceita || prod.ingredientes || [];
            let textoReceita = "Sem receita";

            if (prod.receitaTexto && prod.receitaTexto.trim() !== "") {
                textoReceita = prod.receitaTexto;
            } else if (listaItens.length > 0) {
                textoReceita = listaItens.map(item => {
                    const ingNome = item.ingrediente?.nome || item.nome || item.ingredienteNome || "Ingrediente";
                    const qtd = item.quantidadeNecessaria || item.quantidade || item.qtd || 1;
                    return `${qtd}x ${ingNome}`;
                }).join(", ");
            }

            return `
                <div class="card-item" style="margin-bottom: 15px; padding: 15px; border-radius: 8px; background: rgba(255,255,255,0.05);">
                    <h3>${prod.nome}</h3>
                    <p style="color: #ff9f43; font-weight: bold;">R$ ${Number(prod.preco).toFixed(2)}</p>
                    <p style="font-size: 0.9em; opacity: 0.8;"><strong>Ingredientes:</strong> ${textoReceita}</p>
                    <div style="margin-top: 10px; display: flex; gap: 8px;">
                        <button class="btn-excluir" onclick="excluirProduto(${prod.id})">Excluir</button>
                        <button class="btn-vender" onclick="venderProduto(${prod.id})" style="background: #28a745; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer;">Vender Lanche</button>
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        console.error("Erro ao carregar produtos:", e);
    }
}

async function excluirProduto(id) {
    if (!confirm("Deseja excluir este lanche?")) return;
    try {
        const res = await fetch(`${API_URL}/produtos/${id}`, {
            method: "DELETE",
            headers: getAuthHeader()
        });
        if (res.ok) carregarProdutos();
    } catch (e) {
        console.error(e);
    }
}

async function venderProduto(id) {
    try {
        const res = await fetch(`${API_URL}/vendas/produto/${id}`, {
            method: "POST",
            headers: getAuthHeader()
        });
        if (res.ok) {
            alert("Venda realizada com sucesso! Estoque atualizado.");
            carregarInsumos();
            carregarProdutos();
        } else {
            alert("Erro ao realizar venda ou estoque insuficiente.");
        }
    } catch (e) {
        console.error(e);
    }
}

// ==========================================
// SEÇÃO: BEBIDAS E ITENS AVULSOS
// ==========================================
async function carregarBebidas() {
    try {
        const res = await fetch(`${API_URL}/bebidas`, { headers: getAuthHeader() });
        if (!res.ok) return;
        const bebidas = await res.json();
        const container = document.getElementById("containerBebidas") || document.getElementById("listaBebidas");
        if (container) {
            container.innerHTML = bebidas.map(b => `
                <div class="card-item">
                    <h3>${b.nome}</h3>
                    <p>R$ ${Number(b.preco).toFixed(2)} | Estoque: ${b.quantidadeEstoque !== undefined ? b.quantidadeEstoque : (b.quantidade || 0)} UN</p>
                    <button class="btn-excluir" onclick="excluirBebida(${b.id})">Excluir</button>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error(e);
    }
}

async function excluirBebida(id) {
    if (!confirm("Deseja excluir esta bebida?")) return;
    try {
        const res = await fetch(`${API_URL}/bebidas/${id}`, { method: "DELETE", headers: getAuthHeader() });
        if (res.ok) carregarBebidas();
    } catch (e) {
        console.error(e);
    }
}

async function carregarItensAvulsos() {
    try {
        const res = await fetch(`${API_URL}/itens-avulsos`, { headers: getAuthHeader() });
        if (!res.ok) return;
        const itens = await res.json();
        const container = document.getElementById("containerItensAvulsos") || document.getElementById("listaOutrosItens");
        if (container) {
            container.innerHTML = itens.map(i => `
                <div class="card-item">
                    <h3>${i.nome}</h3>
                    <p>R$ ${Number(i.preco).toFixed(2)} | Estoque: ${i.quantidadeEstoque !== undefined ? i.quantidadeEstoque : (i.quantidade || 0)} UN</p>
                    <button class="btn-excluir" onclick="excluirItemAvulso(${i.id})">Excluir</button>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error(e);
    }
}

async function excluirItemAvulso(id) {
    if (!confirm("Deseja excluir este item?")) return;
    try {
        const res = await fetch(`${API_URL}/itens-avulsos/${id}`, { method: "DELETE", headers: getAuthHeader() });
        if (res.ok) carregarItensAvulsos();
    } catch (e) {
        console.error(e);
    }
}
