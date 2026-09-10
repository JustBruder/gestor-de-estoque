<div align="center">

  # ⚜️ Gestor de Estoque & PDV ⚜️

  <p align="center">
    <strong>Sistema de Gestão de Estoque, Ficha Técnica e Ponto de Venda em Nuvem</strong>
  </p>

  <p align="center">
    <a href="https://seu-projeto.vercel.app">
      <img src="https://img.shields.io/badge/Acessar_Aplicação-Live_Demo-FF8C00?style=for-the-badge&logo=vercel&logoColor=white" alt="Live Demo" />
    </a>
  </p>

  <!-- Badges em tons de Preto, Laranja e Dourado -->
  <p align="center">
    <img src="https://img.shields.io/badge/Java-17+-FF8C00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
    <img src="https://img.shields.io/badge/Spring_Boot-3.x-111111?style=for-the-badge&logo=springboot&logoColor=FF8C00" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/Security-JWT-DAA520?style=for-the-badge&logo=jsonwebtokens&logoColor=black" alt="JWT" />
    <img src="https://img.shields.io/badge/PostgreSQL-Neon-FFD700?style=for-the-badge&logo=postgresql&logoColor=black" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/Clever_Cloud-Backend-FF8C00?style=for-the-badge&logo=clevercloud&logoColor=white" alt="Clever Cloud" />
    <img src="https://img.shields.io/badge/Vercel-Frontend-000000?style=for-the-badge&logo=vercel&logoColor=DAA520" alt="Vercel" />
  </p>

</div>

<hr />

## Sobre o Projeto

O **Gestor de Estoque & PDV** é uma solução *fullstack* desenvolvida para automatizar e otimizar o controle de insumos, bebidas e produtos finais em estabelecimentos alimentícios. 

O sistema realiza o cálculo automático de fichas técnicas de produtos, abatendo proporcionalmente os insumos do estoque a cada venda realizada no Ponto de Venda (PDV), garantindo precisão operacional, controle financeiro e segurança total dos dados.

---

## 🍔🧡 Principais Funcionalidades

### **Autenticação & Perfis**
* **JWT Stateless:** Sessões seguras sem armazenamento de estado no servidor.
* **Recuperação de Acesso:** Fluxo de redefinição de senha via PIN de Segurança único por usuário.
* **Isolamento de Dados:** Cada conta possui acesso restrito exclusivamente ao seu próprio estoque.

### **Gestão de Estoque & Insumos**
* **Insumos Base:** Cadastro e controle de quantidades por gramas, litros, unidades, etc.
* **Itens & Bebidas Avulsas:** Gerenciamento independente com atualização automática de saldo.
* **Baixa de Acréscimos:** Registros rápidos para abates avulsos no estoque.

### **Ficha Técnica & Receitas**
* **Composição de Lanches:** Associação dinâmica de múltiplos insumos a um único produto.
* **Validação de Estoque:** Checagem preventiva de disponibilidade de matéria-prima antes do cadastro da receita.

### **Ponto de Venda (PDV Inteligente)**
* **Venda em 1-Clique:** Interface ágil para registro de vendas.
* **Baixa Proporcional Automática:** Ao vender um produto, todos os ingredientes de sua ficha técnica são debitados do estoque instantaneamente.

---

## Arquitetura & Tecnologias

A aplicação segue o modelo de arquitetura desacoplada (**Decoupled SPA + REST API**), garantindo alta escalabilidade e baixa latência:

> 📱 **Frontend (Vercel)**
> `Vanilla JS` • `HTML5 / CSS3`
>
> 🔻 *Requisições REST / HTTPS (JWT)*
>
> ⚙️ **Backend (Clever Cloud)**
> `Java 17` • `Spring Boot 3` • `Spring Security`
>
> 🔻 *Conexão JDBC / SSL*
>
> 🗄️ **Database (Neon Tech)**
> `PostgreSQL Serverless`

---

## ✨ **[Clique para testar o site!](https://conferindoestoque.vercel.app/)** ✨

---

<div align="center">

### 💻 Contate-me / Let's Connect 💖
<div align="center">

<a href="https://www.linkedin.com/in/ingrid-bruder/" target="_blank">
  <img src="https://img.shields.io/badge/LinkedIn-FF69B4?style=for-the-badge&logo=linkedin&logoColor=white"/>
</a>
<a href="mailto:ibruder83@gmail.com" target="_blank">
  <img src="https://img.shields.io/badge/Email-FF69B4?style=for-the-badge&logo=gmail&logoColor=white"/>
</a>

</div>
  <sub>Desenvolvido por JustBruder 🌷✨</sub>
</div>
