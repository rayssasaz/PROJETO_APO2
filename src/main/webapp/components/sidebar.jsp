<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="models.Usuario" %>
<%@ page import="models.NivelAcesso" %>
<%
    Usuario userSidebar = (Usuario) session.getAttribute("usuarioAutenticado");
    
    String papel = "";
    if (userSidebar != null && userSidebar.getAcesso() != null) {
        papel = userSidebar.getAcesso().name(); // Pega o nome do Enum (CLIENTE, SUPORTE, ADMIN)
    }
    
    // CASO QUEIRA TESTAR O VISUAL SEM ESTAR LOGADO, DESCOMENTE UMA DAS LINHAS ABAIXO:
    // papel = "CLIENTE";
    // papel = "SUPORTE";
    // papel = "ADMIN";
%>

<div class="bg-dark text-white p-3 flex-shrink-0 d-flex flex-column shadow" style="width: 240px; min-height: calc(100vh - 56px);">
    <div class="text-muted text-uppercase small fw-bold mb-3 px-2">Navegação</div>
    <ul class="nav nav-pills flex-column mb-auto">
        
        <% if ("CLIENTE".equals(papel)) { %>
            <li class="nav-item mb-2">
                <a href="${pageContext.request.contextPath}/cliente/dashboard.jsp" class="nav-link text-white active">
                    <i class="fa-solid fa-chart-pie me-2"></i>Meu Painel
                </a>
            </li>
            <li class="nav-item mb-2">
                <a href="${pageContext.request.contextPath}/cliente/abrir-chamado.jsp" class="nav-link text-white">
                    <i class="fa-solid fa-plus-circle me-2"></i>Abrir Chamado
                </a>
            </li>
        <% } %>

        <% if ("SUPORTE".equals(papel)) { %>
            <li class="nav-item mb-2">
                <a href="${pageContext.request.contextPath}/suporte/fila-chamados.jsp" class="nav-link text-dark bg-warning fw-bold">
                    <i class="fa-solid fa-list-check me-2"></i>Fila de Chamados
                </a>
            </li>
        <% } %>

        <% if ("ADMIN".equals(papel)) { %>
            <li class="nav-item mb-2">
                <a href="${pageContext.request.contextPath}/admin/dashboard-global.jsp" class="nav-link text-white active bg-danger">
                    <i class="fa-solid fa-gauge me-2"></i>Painel Geral
                </a>
            </li>
            <li class="nav-item mb-2">
                <a href="${pageContext.request.contextPath}/admin/gerenciar-usuarios.jsp" class="nav-link text-white">
                    <i class="fa-solid fa-users me-2"></i>Controle de Usuários
                </a>
            </li>
            <li class="nav-item mb-2">
                <a href="${pageContext.request.contextPath}/admin/configuracoes.jsp" class="nav-link text-white">
                    <i class="fa-solid fa-sliders me-2"></i>Categorias e Sistema
                </a>
            </li>
        <% } %>
        
    </ul>
    
    <% if (!"".equals(papel)) { 
        String corBadge = "bg-primary";
        if("ADMIN".equals(papel)) corBadge = "bg-danger";
        if("SUPORTE".equals(papel)) corBadge = "bg-warning text-dark";
    %>
        <div class="border-top border-secondary pt-3 px-2">
            <span class="badge <%= corBadge %> w-100 p-2">
                <%= papel %>
            </span>
        </div>
    <% } %>
</div>