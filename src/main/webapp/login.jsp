<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Help Desk - Login</title>
    <%@ include file="components/header.jsp" %>
</head>
<body class="bg-dark">
    <div class="container d-flex justify-content-center align-items-center vh-100">
        <div class="card p-4 shadow" style="width: 24rem;">
            <div class="text-center mb-4">
                <i class="fa-solid fa-headset fa-3x text-primary"></i>
                <h3 class="mt-2">Fazer Login</h3>
            </div>
            
            <form action="auth" method="POST">
                <div class="mb-3">
                    <label class="form-label">E-mail</label>
                    <input type="email" name="email" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">Senha</label>
                    <input type="password" name="senha" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary w-100">Entrar</button>
            </form>
            
            <div class="mt-3 text-center">
                <a href="cadastro.jsp" class="text-decoration-none">Criar uma conta</a> | 
                <a href="recuperarSenha.jsp" class="text-decoration-none">Esqueci a senha</a>
            </div >
        </div>
    </div>
    <%@ include file="components/scripts.jsp" %>
</body>
</html>