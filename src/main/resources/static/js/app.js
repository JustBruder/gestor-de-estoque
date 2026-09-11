const API_URL = "https://gestor-estoque-production.up.railway.app/api"; // Ajuste a URL base da sua API se necessário

document.addEventListener("DOMContentLoaded", () => {
    carregarInsumos();
    carregarBebidas();
    carregarItensAvulsos();
    carregarProdutos();
});

function getAuthHeader() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}

// ==========================================
// CADASTRO E RENDERIZAÇÃO DE INSUMOS
// ==========================================
async function carregarInsumos() {
    try {
        const res = await fetch(`${API_URL}/ingredientes`, { headers: getAuthHeader() });
        if (!res.ok) return;
        const ingredientes = await res.json();
        
        const tabela = document.getElementById("tabelaInsumos");
        if (tabela) {
            tabela.innerHTML = ingredientes.map(ing => `
                <tr>
                    <td>${ing.id}</td>
                    <td>${ing.nome}</td>
                    <td>${ing.quantidadeEstoque || 0}</td>
                    <td>${ing.unidadeMedida || 'UN'}</td>
                    <td><button class="btn-excluir" onclick="excluirIngrediente(${ing.id})">Excluir</button></td>
                </tr>
            `).join('');
        }
        atualizarSelectsIngredientes(ingredientes);
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
            body: JSON.stringify({ nome, quantidadeEstoque: parseFloat(quantidadeEstoque || 0), unidadeMedida })
        });
        if (res.ok) {
            alert("Insumo salvo!");
            carregarInsumos();
        }
    } catch (e) {
        console.error(e);
    }
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
// GERENCIADOR DYNAMIC DE INGREDIENTES NO LANCHE
// ==========================================
let listaIngredientesDisponiveis = [];

function atualizarSelectsIngredientes(ingredientes) {
    listaIngredientesDisponiveis = ingredientes;
}

function adicionarLinhaIngrediente() {
    const container = document.getElementById("containerIngredientesLanche");
    if (!container) return;

    const div = document.createElement("div");
    div.className = "linha-ingrediente";
    div.style.display = "flex";
    div.style.gap = "10px";
    div.style.marginTop = "5px";

    let options = listaIngredientesDisponiveis.map(ing => `<option value="${ing.id}">${ing.nome} (${ing.unidadeMedida})</option>`).join('');

    div.innerHTML = `
        <select class="select-ingrediente-id" style="flex: 2; padding: 5px;">
            <option value="">Selecione o Insumo</option>
            ${options}
        </select>
        <input type="number" class="input-ingrediente-qtd" placeholder="Qtd" value="1" step="0.1" style="flex: 1; padding: 5px;">
        <button type="button" onclick="this.parentElement.remove()" style="background: red; color: white; border: none; padding: 5px 10px;">X</button>
    `;
    container.appendChild(div);
}

// ==========================================
// CADASTRO E RENDERIZAÇÃO DE LANCHES (PRODUTOS)
// ==========================================
async function cadastrarLanche(e) {
    if (e) e.preventDefault();
    
    const nome = document.getElementById("nomeLanche")?.value;
    const preco = document.getElementById("precoLanche")?.value;

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
            document.getElementById("nomeLanche").value = "";
            document.getElementById("precoLanche").value = "";
            document.getElementById("containerIngredientesLanche").innerHTML = "";
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

        const container = document.getElementById("containerLanchesCadastrados");
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
                <div class="card-item">
                    <h3>${prod.nome}</h3>
                    <p>R$ ${Number(prod.preco).toFixed(2)}</p>
                    <p><strong>Ingredientes:</strong> ${textoReceita}</p>
                    <button class="btn-excluir" onclick="excluirProduto(${prod.id})">Excluir</button>
                    <button class="btn-vender" onclick="venderProduto(${prod.id})">Vender Lanche</button>
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
            alert("Venda realizada e estoque atualizado!");
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
// OUTRAS SEÇÕES (BEBIDAS E ITENS AVULSOS)
// ==========================================
async function carregarBebidas() {
    try {
        const res = await fetch(`${API_URL}/bebidas`, { headers: getAuthHeader() });
        if (!res.ok) return;
        const bebidas = await res.json();
        const container = document.getElementById("containerBebidas");
        if (container) {
            container.innerHTML = bebidas.map(b => `
                <div class="card-item">
                    <h3>${b.nome}</h3>
                    <p>R$ ${Number(b.preco).toFixed(2)} | Estoque: ${b.quantidadeEstoque || 0}</p>
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
        const container = document.getElementById("containerItensAvulsos");
        if (container) {
            container.innerHTML = itens.map(i => `
                <div class="card-item">
                    <h3>${i.nome}</h3>
                    <p>R$ ${Number(i.preco).toFixed(2)} | Estoque: ${i.quantidadeEstoque || 0}</p>
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
