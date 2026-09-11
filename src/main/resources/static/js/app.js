const API_BASE = "https://app-58f13233-93ab-41e4-8cfa-d7aa4e33885f.cleverapps.io";

let listaIngredientesGlobal = [];
let usuarioLogado = null;
let modoCadastro = false;

document.addEventListener("DOMContentLoaded", () => {
    verificarSessao();
    iniciarEfeitoFluidoMouse();

    document.getElementById("formAuth").addEventListener("submit", processarAuth);
    document.getElementById("formRedefinir").addEventListener("submit", processarRedefinicao);

    const formIngrediente = document.getElementById("formIngrediente");
    if (formIngrediente) formIngrediente.addEventListener("submit", salvarIngrediente);

    const formBebida = document.getElementById("formBebida");
    if (formBebida) formBebida.addEventListener("submit", salvarBebida);

    const formItemAvulso = document.getElementById("formItemAvulso");
    if (formItemAvulso) formItemAvulso.addEventListener("submit", salvarItemAvulso);

    const formProduto = document.getElementById("formProduto");
    if (formProduto) formProduto.addEventListener("submit", salvarProduto);
});

// Função auxiliar segura para converter JSON sem travar
async function parseRes(res) {
    const text = await res.text();
    try {
        return text ? JSON.parse(text) : {};
    } catch (e) {
        return { erro: text || `Erro HTTP ${res.status}` };
    }
}

function iniciarEfeitoFluidoMouse() {
    document.addEventListener("mousemove", (e) => {
        const cards = document.querySelectorAll(".card, .glass-panel");
        cards.forEach((card) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            card.style.setProperty("--mouse-x", `${x}px`);
            card.style.setProperty("--mouse-y", `${y}px`);
        });
    });
}

function verificarSessao() {
    const usuarioSalvo = localStorage.getItem("usuario_gestor");
    if (usuarioSalvo) {
        usuarioLogado = JSON.parse(usuarioSalvo);
        document.getElementById("authOverlay").style.display = "none";
        document.getElementById("userInfo").style.display = "flex";
        document.getElementById("userNomeDisplay").innerText = usuarioLogado.nome;
        carregarIngredientesSelect();
    } else {
        document.getElementById("authOverlay").style.display = "flex";
        document.getElementById("userInfo").style.display = "none";
    }
}

function abrirPerfil() {
    if (!usuarioLogado) return;
    document.getElementById("authOverlay").style.display = "flex";
    document.getElementById("formAuth").style.display = "none";
    document.getElementById("formRedefinir").style.display = "block";
    document.getElementById("redEmail").value = usuarioLogado.email;
    document.getElementById("authTitle").innerText = "Minha Conta / Alterar Senha";
    document.getElementById("btnToggleAuth").style.display = "none";
    document.getElementById("btnEsqueciSenha").style.display = "none";
    document.getElementById("btnVoltarLogin").style.display = "inline-block";
    document.getElementById("btnVoltarLogin").innerText = "← Voltar ao Sistema";
}

function fecharPerfilOuVoltar() {
    if (usuarioLogado) {
        document.getElementById("authOverlay").style.display = "none";
    } else {
        voltarParaLogin();
    }
}

function alternarModoAuth() {
    modoCadastro = !modoCadastro;
    document.getElementById("groupNome").style.display = modoCadastro ? "block" : "none";
    document.getElementById("groupPin").style.display = modoCadastro ? "block" : "none";
    document.getElementById("authTitle").innerText = modoCadastro ? "Criar Nova Conta" : "Entrar na Conta";
    document.getElementById("btnAuthSubmit").innerText = modoCadastro ? "Cadastrar" : "Entrar";
    document.getElementById("btnToggleAuth").innerText = modoCadastro ? "Já tem conta? Faça Login" : "Não tem conta? Cadastre-se";
}

function mostrarRedefinir() {
    document.getElementById("formAuth").style.display = "none";
    document.getElementById("formRedefinir").style.display = "block";
    document.getElementById("authTitle").innerText = "Redefinir Senha";
    document.getElementById("btnToggleAuth").style.display = "none";
    document.getElementById("btnEsqueciSenha").style.display = "none";
    document.getElementById("btnVoltarLogin").style.display = "inline-block";
    document.getElementById("btnVoltarLogin").innerText = "← Voltar ao Login";
}

function voltarParaLogin() {
    document.getElementById("formAuth").style.display = "block";
    document.getElementById("formRedefinir").style.display = "none";
    document.getElementById("authTitle").innerText = "Entrar na Conta";
    document.getElementById("btnToggleAuth").style.display = "inline-block";
    document.getElementById("btnEsqueciSenha").style.display = "inline-block";
    document.getElementById("btnVoltarLogin").style.display = "none";
}

async function processarAuth(e) {
    e.preventDefault();
    const endpoint = modoCadastro ? `${API_BASE}/api/auth/cadastrar` : `${API_BASE}/api/auth/login`;
    
    const payload = {
        email: document.getElementById("authEmail").value,
        senha: document.getElementById("authSenha").value
    };

    if (modoCadastro) {
        payload.nome = document.getElementById("authNome").value;
        payload.pinSeguranca = document.getElementById("authPin").value;
    }

    try {
        const res = await fetch(endpoint, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await parseRes(res);

        if (res.ok) {
            localStorage.setItem("usuario_gestor", JSON.stringify(data));
            verificarSessao();
        } else {
            alert("❌ " + (data.erro || data.message || "Falha na autenticação."));
        }
    } catch (err) {
        alert("❌ Erro de conexão com o servidor.");
    }
}

async function processarRedefinicao(e) {
    e.preventDefault();

    const payload = {
        email: document.getElementById("redEmail").value,
        pinSeguranca: document.getElementById("redPin").value,
        novaSenha: document.getElementById("redNovaSenha").value
    };

    try {
        const res = await fetch(`${API_BASE}/api/auth/redefinir-senha`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Senha alterada com sucesso!"));
            fecharPerfilOuVoltar();
        } else {
            alert("❌ " + (data.erro || data.message || "Falha ao redefinir senha."));
        }
    } catch (err) {
        alert("❌ Erro de conexão com o servidor.");
    }
}

function fazerLogout() {
    localStorage.removeItem("usuario_gestor");
    usuarioLogado = null;
    location.reload();
}

function getHeaders() {
    const token = usuarioLogado ? usuarioLogado.token : "";
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}

async function carregarIngredientesSelect() {
    if (!usuarioLogado) return;
    try {
        const res = await fetch(`${API_BASE}/api/ingredientes`, { headers: getHeaders() });
        const data = await parseRes(res);
        if (res.ok && Array.isArray(data)) {
            listaIngredientesGlobal = data;
        } else if (res.status === 401 || res.status === 403) {
            alert("⚠️ Sua sessão expirou. Por favor, faça login novamente.");
            fazerLogout();
        }
    } catch (err) {
        console.error("Erro ao carregar ingredientes:", err);
    }
}

function adicionarLinhaIngrediente() {
    const container = document.getElementById("listaItensReceita");
    if (!container) return;

    if (!Array.isArray(listaIngredientesGlobal) || listaIngredientesGlobal.length === 0) {
        carregarIngredientesSelect();
        alert("⚠️ Cadastre pelo menos um ingrediente antes de montar a receita!");
        return;
    }

    const div = document.createElement("div");
    div.className = "row g-1 mb-2 item-receita-linha align-items-center";

    let options = listaIngredientesGlobal
        .map(i => `<option value="${i.id}">${i.nome} (${i.unidadeMedida})</option>`)
        .join('');

    div.innerHTML = `
        <div class="col-6">
            <select class="form-select ing-select" required>
                <option value="">Selecione...</option>
                ${options}
            </select>
        </div>
        <div class="col-4">
            <input type="number" step="0.01" min="0.01" class="form-control ing-qtd" placeholder="Qtd" required>
        </div>
        <div class="col-2">
            <button type="button" class="btn btn-neon-danger btn-sm w-100 p-1" onclick="this.parentElement.parentElement.remove()">X</button>
        </div>
    `;
    container.appendChild(div);
}

async function salvarIngrediente(e) {
    e.preventDefault();
    try {
        const payload = {
            nome: document.getElementById("ingNome").value.trim(),
            quantidadeEstoque: parseFloat(document.getElementById("ingQtd").value),
            unidadeMedida: document.getElementById("ingUnidade").value
        };

        const res = await fetch(`${API_BASE}/api/ingredientes`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(payload)
        });

        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ Ingrediente cadastrado!");
            document.getElementById("formIngrediente").reset();
            await carregarIngredientesSelect();
        } else if (res.status === 401 || res.status === 403) {
            alert("⚠️ Sessão expirada! Faça login novamente.");
            fazerLogout();
        } else {
            alert("❌ " + (data.erro || data.message || `Erro ${res.status} ao salvar ingrediente.`));
        }
    } catch (err) {
        alert("❌ Erro de conexão: " + err.message);
    }
}

async function salvarBebida(e) {
    e.preventDefault();
    try {
        const payload = {
            nome: document.getElementById("bebNome").value.trim(),
            preco: parseFloat(document.getElementById("bebPreco").value),
            quantidadeEstoque: parseFloat(document.getElementById("bebQtd").value)
        };

        const res = await fetch(`${API_BASE}/api/bebidas`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(payload)
        });

        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ Bebida cadastrada com sucesso!");
            document.getElementById("formBebida").reset();
        } else if (res.status === 401 || res.status === 403) {
            alert("⚠️ Sessão expirada! Faça login novamente.");
            fazerLogout();
        } else {
            alert("❌ " + (data.erro || data.message || `Erro ${res.status} ao salvar bebida.`));
        }
    } catch (err) {
        alert("❌ Erro de conexão: " + err.message);
    }
}

async function salvarItemAvulso(e) {
    e.preventDefault();
    try {
        const payload = {
            nome: document.getElementById("itemNome").value.trim(),
            preco: parseFloat(document.getElementById("itemPreco").value),
            quantidadeEstoque: parseFloat(document.getElementById("itemQtd").value)
        };

        const res = await fetch(`${API_BASE}/api/itens`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(payload)
        });

        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ Item cadastrado com sucesso!");
            document.getElementById("formItemAvulso").reset();
        } else if (res.status === 401 || res.status === 403) {
            alert("⚠️ Sessão expirada! Faça login novamente.");
            fazerLogout();
        } else {
            const msgErro = data.erro || data.error || data.message || `Erro ${res.status} no servidor.`;
            alert("❌ Erro ao salvar item: " + msgErro);
        }
    } catch (err) {
        alert("❌ Erro de conexão com o servidor: " + err.message);
    }
}

async function salvarProduto(e) {
    e.preventDefault();

    try {
        const nomeInput = document.getElementById("prodNome").value.trim();
        const precoInput = parseFloat(document.getElementById("prodPreco").value);

        if (!nomeInput || isNaN(precoInput) || precoInput <= 0) {
            alert("⚠️ Preencha o nome e preço do lanche!");
            return;
        }

        const linhas = document.querySelectorAll(".item-receita-linha");
        const itensReceita = [];

        linhas.forEach(linha => {
            const ingSelect = linha.querySelector(".ing-select");
            const qtdInput = linha.querySelector(".ing-qtd");

            if (ingSelect && qtdInput) {
                const ingId = parseInt(ingSelect.value);
                const qtd = parseFloat(qtdInput.value);

                if (!isNaN(ingId) && ingId > 0 && !isNaN(qtd) && qtd > 0) {
                    itensReceita.push({
                        ingredienteId: ingId,
                        quantidadeNecessaria: qtd
                    });
                }
            }
        });

        if (itensReceita.length === 0) {
            alert("⚠️ Selecione pelo menos 1 ingrediente na receita!");
            return;
        }

        const payload = {
            nome: nomeInput,
            preco: precoInput,
            itensReceita: itensReceita
        };

        const res = await fetch(`${API_BASE}/api/produtos`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(payload)
        });

        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ Lanche cadastrado com sucesso!");
            document.getElementById("formProduto").reset();
            document.getElementById("listaItensReceita").innerHTML = "";
            await carregarIngredientesSelect();
        } else if (res.status === 401 || res.status === 403) {
            alert("⚠️ Sessão expirada! Faça login novamente.");
            fazerLogout();
        } else {
            alert("❌ " + (data.erro || data.message || `Erro ${res.status} ao salvar lanche.`));
        }
    } catch (error) {
        alert("❌ Erro de conexão: " + error.message);
    }
}

async function carregarEstoque() {
    try {
        const resIng = await fetch(`${API_BASE}/api/ingredientes`, { headers: getHeaders() });
        const ingredientes = await parseRes(resIng);
        const tbodyIng = document.getElementById("tabelaEstoque");
        const tbodyBaixo = document.getElementById("tabelaEstoqueBaixo");
        const painelBaixo = document.getElementById("painelEstoqueBaixoContainer");

        let itensBaixos = [];

        if (tbodyIng && Array.isArray(ingredientes)) {
            tbodyIng.innerHTML = "";
            if (ingredientes.length === 0) {
                tbodyIng.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Nenhum ingrediente.</td></tr>`;
            } else {
                ingredientes.forEach(item => {
                    // Regra de Estoque Baixo: <= 20 para UN/FATIA, <= 200 para GRAMAS/ML/KG
                    const unidade = (item.unidadeMedida || "").toUpperCase();
                    const limite = (unidade.includes("GRAMA") || unidade.includes("ML") || unidade.includes("KG")) ? 200 : 20;

                    if (item.quantidadeEstoque <= limite) {
                        itensBaixos.push(item);
                    }

                    tbodyIng.innerHTML += `
                        <tr>
                            <td>${item.id}</td>
                            <td class="fw-bold">${item.nome}</td>
                            <td>${item.quantidadeEstoque}</td>
                            <td>${item.unidadeMedida}</td>
                            <td>
                                <button class="btn btn-neon-danger btn-sm" onclick="excluirIngrediente(${item.id}, '${item.nome}')">Excluir</button>
                            </td>
                        </tr>
                    `;
                });
            }

            // Renderiza o Alerta de Estoque Baixo Automaticamente
            if (tbodyBaixo && painelBaixo) {
                tbodyBaixo.innerHTML = "";
                if (itensBaixos.length > 0) {
                    painelBaixo.style.display = "block";
                    itensBaixos.forEach(item => {
                        tbodyBaixo.innerHTML += `
                            <tr>
                                <td>${item.id}</td>
                                <td class="fw-bold text-danger">${item.nome}</td>
                                <td class="fw-bold text-warning">${item.quantidadeEstoque}</td>
                                <td>${item.unidadeMedida}</td>
                            </tr>
                        `;
                    });
                } else {
                    painelBaixo.style.display = "none";
                }
            }
        }

        const resBeb = await fetch(`${API_BASE}/api/bebidas`, { headers: getHeaders() });
        const bebidas = await parseRes(resBeb);
        const tbodyBeb = document.getElementById("tabelaBebidasEstoque");
        if (tbodyBeb && Array.isArray(bebidas)) {
            tbodyBeb.innerHTML = "";
            if (bebidas.length === 0) {
                tbodyBeb.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Nenhuma bebida.</td></tr>`;
            } else {
                bebidas.forEach(item => {
                    tbodyBeb.innerHTML += `
                        <tr>
                            <td class="fw-bold">${item.nome}</td>
                            <td class="text-success">R$ ${item.preco.toFixed(2)}</td>
                            <td>${item.quantidadeEstoque} UN</td>
                            <td>
                                <button class="btn btn-neon-danger btn-sm" onclick="excluirBebida(${item.id}, '${item.nome}')">Excluir</button>
                            </td>
                        </tr>
                    `;
                });
            }
        }

        const resItens = await fetch(`${API_BASE}/api/itens`, { headers: getHeaders() });
        const itens = await parseRes(resItens);
        const tbodyItens = document.getElementById("tabelaItensEstoque");
        if (tbodyItens && Array.isArray(itens)) {
            tbodyItens.innerHTML = "";
            if (itens.length === 0) {
                tbodyItens.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Nenhum item cadastrado.</td></tr>`;
            } else {
                itens.forEach(item => {
                    tbodyItens.innerHTML += `
                        <tr>
                            <td class="fw-bold">${item.nome}</td>
                            <td class="text-success">R$ ${item.preco.toFixed(2)}</td>
                            <td>${item.quantidadeEstoque} UN</td>
                            <td>
                                <button class="btn btn-neon-danger btn-sm" onclick="excluirItemAvulso(${item.id}, '${item.nome}')">Excluir</button>
                            </td>
                        </tr>
                    `;
                });
            }
        }
    } catch (err) {
        console.error("Erro ao carregar estoque:", err);
    }
}

async function excluirIngrediente(id, nome) {
    if (!confirm(`Tem certeza que deseja excluir o ingrediente: ${nome}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/api/ingredientes/${id}`, { method: 'DELETE', headers: getHeaders() });
        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Ingrediente excluído!"));
            await carregarEstoque();
            await carregarIngredientesSelect();
        } else {
            alert("❌ " + (data.erro || data.message || "Erro ao excluir."));
        }
    } catch (err) {
        alert("❌ Erro ao conectar ao servidor.");
    }
}

async function excluirBebida(id, nome) {
    if (!confirm(`Tem certeza que deseja excluir a bebida: ${nome}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/api/bebidas/${id}`, { method: 'DELETE', headers: getHeaders() });
        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Bebida excluída!"));
            await carregarEstoque();
        } else {
            alert("❌ " + (data.erro || data.message || "Erro ao excluir."));
        }
    } catch (err) {
        alert("❌ Erro ao conectar ao servidor.");
    }
}

async function excluirItemAvulso(id, nome) {
    if (!confirm(`Tem certeza que deseja excluir o item: ${nome}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/api/itens/${id}`, { method: 'DELETE', headers: getHeaders() });
        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Item excluído!"));
            await carregarEstoque();
        } else {
            alert("❌ " + (data.erro || data.message || "Erro ao excluir."));
        }
    } catch (err) {
        alert("❌ Erro de conexão.");
    }
}

async function carregarAcrescimos() {
    try {
        const res = await fetch(`${API_BASE}/api/ingredientes`, { headers: getHeaders() });
        const ingredientes = await parseRes(res);
        const grid = document.getElementById("gridAcrescimos");
        if (!grid) return;

        grid.innerHTML = "";

        if (!Array.isArray(ingredientes) || ingredientes.length === 0) {
            grid.innerHTML = `<div class="col-12 text-center text-muted"><p>Nenhum ingrediente disponível para acréscimo.</p></div>`;
            return;
        }

        ingredientes.forEach(ing => {
            grid.innerHTML += `
                <div class="col-md-3">
                    <div class="card p-3 text-center">
                        <h5 class="text-gradient">${ing.nome}</h5>
                        <p class="mb-2 text-secondary">Estoque: <strong>${ing.quantidadeEstoque} ${ing.unidadeMedida}</strong></p>
                        <button class="btn btn-neon btn-sm w-100" onclick="darBaixaAcrescimo(${ing.id}, '${ing.nome}')">
                            Abater 1 ${ing.unidadeMedida}
                        </button>
                    </div>
                </div>
            `;
        });
    } catch (err) {
        console.error("Erro ao carregar acréscimos:", err);
    }
}

async function darBaixaAcrescimo(ingredienteId, nome) {
    try {
        const res = await fetch(`${API_BASE}/api/ingredientes/${ingredienteId}/acrescimo`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ quantidade: 1.0 })
        });
        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Baixa realizada!"));
            await carregarAcrescimos();
        } else {
            alert("❌ " + (data.erro || data.message || "Erro ao dar baixa."));
        }
    } catch (err) {
        alert("❌ Erro de conexão.");
    }
}

async function carregarPDV() {
    try {
        const resProds = await fetch(`${API_BASE}/api/produtos`, { headers: getHeaders() });
        const produtos = await parseRes(resProds);
        const gridProds = document.getElementById("gridProdutosVenda");
        
        if (gridProds) {
            gridProds.innerHTML = "";
            if (!Array.isArray(produtos) || produtos.length === 0) {
                gridProds.innerHTML = `<div class="col-12 text-muted"><p>Nenhum lanche cadastrado.</p></div>`;
            } else {
                produtos.forEach(prod => {
                    let itensTexto = (prod.receita && prod.receita.length > 0)
                        ? prod.receita.map(r => `${r.quantidadeNecessaria}x ${r.ingrediente ? r.ingrediente.nome : 'Item'}`).join(", ")
                        : "Sem receita";

                    gridProds.innerHTML += `
                        <div class="col-12">
                            <div class="card p-3 shadow-sm border-primary">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <h4 class="text-gradient m-0">${prod.nome}</h4>
                                    <button class="btn btn-neon-danger btn-sm" onclick="excluirLanche(${prod.id}, '${prod.nome}')">Excluir</button>
                                </div>
                                <h5 class="text-success">R$ ${prod.preco.toFixed(2)}</h5>
                                <p class="text-muted small mt-2"><strong>Ingredientes:</strong> ${itensTexto}</p>
                                <button class="btn btn-neon w-100 mt-2" onclick="realizarVenda(${prod.id}, '${prod.nome}')">Vender Lanche</button>
                            </div>
                        </div>
                    `;
                });
            }
        }

        const resBebs = await fetch(`${API_BASE}/api/bebidas`, { headers: getHeaders() });
        const bebidas = await parseRes(resBebs);
        const gridBebs = document.getElementById("gridBebidasVenda");

        if (gridBebs) {
            gridBebs.innerHTML = "";
            if (!Array.isArray(bebidas) || bebidas.length === 0) {
                gridBebs.innerHTML = `<div class="col-12 text-muted"><p>Nenhuma bebida cadastrada.</p></div>`;
            } else {
                bebidas.forEach(beb => {
                    gridBebs.innerHTML += `
                        <div class="col-12">
                            <div class="card p-3">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <h5 class="text-gradient m-0">${beb.nome}</h5>
                                        <span class="text-success fw-bold">R$ ${beb.preco.toFixed(2)}</span> | <span class="text-secondary small">Estoque: ${beb.quantidadeEstoque} UN</span>
                                    </div>
                                    <button class="btn btn-neon btn-sm" onclick="venderBebida(${beb.id}, '${beb.nome}')">Vender</button>
                                </div>
                            </div>
                        </div>
                    `;
                });
            }
        }

        const resItens = await fetch(`${API_BASE}/api/itens`, { headers: getHeaders() });
        const itens = await parseRes(resItens);
        const gridItens = document.getElementById("gridItensVenda");

        if (gridItens) {
            gridItens.innerHTML = "";
            if (!Array.isArray(itens) || itens.length === 0) {
                gridItens.innerHTML = `<div class="col-12 text-muted"><p>Nenhum item cadastrado.</p></div>`;
            } else {
                itens.forEach(item => {
                    gridItens.innerHTML += `
                        <div class="col-12">
                            <div class="card p-3">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <h5 class="text-gradient m-0">${item.nome}</h5>
                                        <span class="text-success fw-bold">R$ ${item.preco.toFixed(2)}</span> | <span class="text-secondary small">Estoque: ${item.quantidadeEstoque} UN</span>
                                    </div>
                                    <button class="btn btn-neon btn-sm" onclick="venderItemAvulso(${item.id}, '${item.nome}')">Vender</button>
                                </div>
                            </div>
                        </div>
                    `;
                });
            }
        }
    } catch (err) {
        console.error("Erro ao carregar PDV:", err);
    }
}

async function realizarVenda(produtoId, nomeProduto) {
    if (!confirm(`Confirmar venda de: ${nomeProduto}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/api/vendas/${produtoId}`, { method: 'POST', headers: getHeaders() });
        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Venda realizada!"));
        } else {
            alert("❌ " + (data.erro || data.message || "Erro ao registrar venda."));
        }
    } catch (err) {
        alert("❌ Erro ao conectar ao servidor.");
    }
}

async function venderBebida(id, nome) {
    try {
        const res = await fetch(`${API_BASE}/api/bebidas/${id}/venda`, { method: 'POST', headers: getHeaders() });
        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Venda realizada!"));
            await carregarPDV();
        } else {
            alert("❌ " + (data.erro || data.message || "Erro ao vender bebida."));
        }
    } catch (err) {
        alert("❌ Erro ao conectar ao servidor.");
    }
}

async function venderItemAvulso(id, nome) {
    try {
        const res = await fetch(`${API_BASE}/api/itens/${id}/venda`, { method: 'POST', headers: getHeaders() });
        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Venda realizada!"));
            await carregarPDV();
        } else {
            alert("❌ " + (data.erro || data.message || "Erro ao vender item."));
        }
    } catch (err) {
        alert("❌ Erro ao conectar ao servidor.");
    }
}

async function excluirLanche(id, nome) {
    if (!confirm(`Tem certeza que deseja excluir o lanche: ${nome}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/api/produtos/${id}`, { method: 'DELETE', headers: getHeaders() });
        const data = await parseRes(res);

        if (res.ok) {
            alert("✅ " + (data.mensagem || "Lanche excluído!"));
            await carregarPDV();
        } else {
            alert("❌ " + (data.erro || data.message || "Erro ao excluir lanche."));
        }
    } catch (err) {
        alert("❌ Erro de conexão.");
    }
}
