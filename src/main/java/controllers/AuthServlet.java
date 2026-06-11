package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

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
import services.MailtrapRestService;
import utils.Criptografia;
import utils.GerenciadorTokens;

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
		} else if ("verificarEmail".equals(action)) {
		    String email = request.getParameter("email");
		    
		    DBConnection db = new DBConnection();
		    try (Connection conn = db.getConnection()) {
		        UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
		        Usuario usuario = usuarioDAO.buscarPorEmail(email);
		        
		        // Configura o cabeçalho HTTP para responder JSON puro
		        response.setContentType("application/json");
		        response.setCharacterEncoding("UTF-8");
		        
		        // Se encontrar o usuário, significa que o e-mail JÁ EXISTE (não está disponível)
		        boolean disponivel = (usuario == null);
		        
		        // Constrói o formato JSON manualmente: {"disponivel": true} ou {"disponivel": false}
		        String json = "{\"disponivel\": " + disponivel + "}";
		        
		        response.getWriter().write(json);
		        return; // Interrompe o método para não executar nenhum forward ou redirect!
		        
		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        response.getWriter().write("{\"erro\": \"Erro no servidor ao validar e-mail.\"}");
		        return;
		    }
		} else if("validar".equals(action)){
			String tokenRecebido = request.getParameter("token");
	        
	        // 1. Verifica no mapa da memória se o token existe
	        String email = GerenciadorTokens.obterEmailPorTokenValidacao(tokenRecebido);
	        
	        if (email != null) {
	            // Token válido! 
	            // Agora sim você acessa o banco para mudar o status do usuário.
	            // Exemplo: usuarioDAO.ativarContaUsuario(email);
	            
	            // 2. Destrói o token da memória para não ser usado duas vezes
	            GerenciadorTokens.removerTokenValidacao(tokenRecebido);
	            
	            request.setAttribute("sucesso", "Conta ativada com sucesso! Você já pode fazer login.");	
	        }else {
	            request.setAttribute("erro", "Link de validação inválido ou expirado.");
	        }
	        request.getRequestDispatcher("login.jsp").forward(request, response);		
		} 		
		else {
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
	    
	 // Monta a URL base dinamicamente (ex: http://localhost:8080/SeuProjeto)
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	    
	    try {
	        //cria a conexão
	        conn = db.getConnection(); 
	        
	        // 2. Instancia o DAO injetando a conexão ativa
	        UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
	        
	        if ("login".equals(action)) {
	            String email = request.getParameter("email");
	            String senhaDigitada = request.getParameter("senha");
	            

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
	        		
	        		// 1. Gera o token
	                String tokenValidacao = UUID.randomUUID().toString();
	                
	                // 2. Salva APENAS na memória RAM do servidor
	                GerenciadorTokens.adicionarTokenValidacao(tokenValidacao, email);
	                
	                // 3. Monta o link apontando para uma action de validação no próprio Servlet
	                String linkConfirmacao = baseUrl + "/auth?action=validar&token=" + tokenValidacao;
	                
	                // 4. Dispara o e-mail
	                MailtrapRestService.enviarEmailValidacao(nome, email, linkConfirmacao);
	                
	                request.setAttribute("sucesso", "Cadastro realizado! Verifique seu e-mail para ativar a conta.");
	                request.getRequestDispatcher("login.jsp").forward(request, response);
	        		
	        		
	        		// Se cadastrou, já deixa o usuário logado na sessão automaticamente
	        		//request.getSession().setAttribute("usuarioAutenticado", novoUsuario);
	                //response.sendRedirect(request.getContextPath() + "/index.jsp");
	        	} else {
	        		request.setAttribute("erro", "Erro ao cadastrar. O e-mail informado já pode estar em uso.");
	                request.getRequestDispatcher("cadastro.jsp").forward(request, response);
	        	}
	        } else if ("recuperar".equals(action)) {   // ... Bloco do else if da Recuperação de senha ...
	        	String email = request.getParameter("email");
	        	
	        	if (email != null && !email.trim().isEmpty()) {
	        		
	        		String tokenRecuperacao = UUID.randomUUID().toString();
	                
	                // Salva na memória
	                GerenciadorTokens.adicionarTokenRecuperacao(tokenRecuperacao, email);
	                
	                // Dispara o e-mail
	                String linkRecuperacao = baseUrl + "/redefinirSenha.jsp?token=" + tokenRecuperacao;
	                MailtrapRestService.enviarEmailRecuperacao(email, linkRecuperacao);
	                
	                request.setAttribute("sucesso", "Se o e-mail constar em nossa base, as instruções foram enviadas para o e-mail: " + email);
	        	} else {
	        		request.setAttribute("erro", "Por favor, informe um e-mail válido.");
	        	}
	        	request.getRequestDispatcher("recuperarSenha.jsp").forward(request, response);
	        }  else if ("salvarNovaSenha".equals(action)) {
                String tokenRecebido = request.getParameter("token");
                String novaSenhaDigitada = request.getParameter("novaSenha");
                
                // 1. Verifica no mapa da memória se o token existe e pega o e-mail atrelado a ele
                String email = GerenciadorTokens.obterEmailPorTokenRecuperacao(tokenRecebido);
                
                if (email != null) {
                    // 2. Token válido! Criptografa a nova senha
                    String senhaHasheada = Criptografia.converterParaSHA256(novaSenhaDigitada);
                    
                    
                    boolean sucesso = usuarioDAO.atualizarSenha(email, senhaHasheada);
                    
                    if (sucesso) {
                        // 4. Limpa o token da memória para que o link expire e não possa ser usado novamente
                        GerenciadorTokens.removerTokenRecuperacao(tokenRecebido);
                        
                        request.setAttribute("sucesso", "Senha alterada com sucesso! Você já pode fazer login.");
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                    } else {
                        request.setAttribute("erro", "Ocorreu um erro ao atualizar a senha no banco. Tente novamente.");
                        // Se falhou no banco, devolve para a mesma tela, repassando o token na URL para não perder a referência
                        request.getRequestDispatcher("redefinirSenha.jsp?token=" + tokenRecebido).forward(request, response);
                    }
                    
                } else {
                    // Se o 'email' voltou nulo, o token não existe no mapa (foi inventado, já foi usado ou o servidor reiniciou)
                    request.setAttribute("erro", "O link de recuperação é inválido ou expirou. Por favor, solicite um novo.");
                    request.getRequestDispatcher("recuperarSenha.jsp").forward(request, response);
                }
            }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
            
            // Devolve uma mensagem amigável para a tela
            request.setAttribute("erro", "Ocorreu um erro de processamento: " + e.getMessage());
            
            // Tenta descobrir de onde o usuário veio, ou manda para uma página padrão
            if ("cadastrar".equals(action)) {
                request.getRequestDispatcher("cadastro.jsp").forward(request, response);
            } else if ("recuperar".equals(action)) {
                request.getRequestDispatcher("recuperarSenha.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
	    } finally {
	        // 3O Servlet terminou tudo, processou a JSP e agora fecha a conexão com segurança.
	        try { if (conn != null) conn.close(); } catch (SQLException e) {}
	    }
	}
	    
	  
}


