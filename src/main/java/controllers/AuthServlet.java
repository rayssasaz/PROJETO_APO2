package controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.sql.Connection;
import daos.UsuarioDAO;
import database.DBConnection;
import models.NivelAcesso;
import models.Usuario;
import utils.Criptografia;

/**
 * Servlet implementation class AuthServlet
 */
@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AuthServlet() {
      //  super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		
		if ("logout".equals(action)) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate(); // Destrói os dados do usuário logado
			}
			// Manda de volta para a landing page deslogado
			response.sendRedirect(request.getContextPath() + "/index.jsp");
		} else {
			response.getWriter().append("Served at: ").append(request.getContextPath());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String action = request.getParameter("action");
	    
	    // 1. Abre a conexão na hora que o formulário chega
	    DBConnection db = new DBConnection(); // criar a conexão pra usar na instância da DAO
	    Connection conn = null;
	    
	    try {
	        // Use a lógica de conexão que vocês criaram e que está funcionando
	        conn = db.getConnection(); 
	        
	        // 2. Instancia o DAO injetando a conexão ativa
	        UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
	        
	        if ("login".equals(action)) {
	            String email = request.getParameter("email");
	            String senhaDigitada = request.getParameter("senha");
	            
	            // Se já ativou a criptografia, lembre-se de converter a senhaDigitada para SHA-256 aqui
	            String senhaHasheada = Criptografia.converterParaSHA256(senhaDigitada);
	            
	            Usuario usuario = usuarioDAO.autenticar(email, senhaHasheada);
	            
	            if (usuario != null) {
	                request.getSession().setAttribute("usuarioAutenticado", usuario);
	                response.sendRedirect(request.getContextPath() + "/index.jsp");
	            } else {
	                request.setAttribute("erro", "E-mail ou senha incorretos.");
	                request.getRequestDispatcher("login.jsp").forward(request, response);
	            }
	        } else if ("cadastrar".equals(action)) {
	            // Lógica de cadastro usando o mesmo usuarioDAO...
	        	String nome = request.getParameter("nome");
	        	String email = request.getParameter("email");
	        	String senhaDigitada = request.getParameter("senha");
	        	
	        	// Criptografa a senha antes de enviar para o banco
	        	String senhaHasheada = Criptografia.converterParaSHA256(senhaDigitada);
	        	
	        	Usuario novoUsuario = new Usuario();
	        	novoUsuario.setNome(nome);
	        	novoUsuario.setEmail(email);
	        	novoUsuario.setSenha(senhaHasheada);
	        	novoUsuario.setAcesso(NivelAcesso.CLIENTE); // Todo cadastro público inicia como CLIENTE
	        	
	        	// Chama o método que você ajustou na UsuarioDAO
	        	boolean sucessoInsercao = usuarioDAO.inserir(novoUsuario);
	        	
	        	if (sucessoInsercao) {
	        		// Se cadastrou, já deixa o usuário logado na sessão automaticamente
	        		request.getSession().setAttribute("usuarioAutenticado", novoUsuario);
	                response.sendRedirect(request.getContextPath() + "/index.jsp");
	        	} else {
	        		request.setAttribute("erro", "Erro ao cadastrar. O e-mail informado já pode estar em uso.");
	                request.getRequestDispatcher("cadastro.jsp").forward(request, response);
	        	}
	        } else if ("recuperar".equals(action)) {   // ... Bloco do else if da Recuperação de senha ...
	        	String email = request.getParameter("email");
	        	
	        	if (email != null && !email.trim().isEmpty()) {
	        		// Aqui futuramente vocês disparam o serviço do Mailtrap
	        		request.setAttribute("sucesso", "As instruções de recuperação foram enviadas para o e-mail: " + email);
	        	} else {
	        		request.setAttribute("erro", "Por favor, informe um e-mail válido.");
	        	}
	        	request.getRequestDispatcher("recuperarSenha.jsp").forward(request, response);
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        // 3O Servlet terminou tudo, processou a JSP e agora fecha a conexão com segurança.
	        try { if (conn != null) conn.close(); } catch (SQLException e) {}
	    }
	}
	    
	  
}


