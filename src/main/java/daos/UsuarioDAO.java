package daos;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.Usuario;
import models.NivelAcesso;

public class UsuarioDAO {
	
	private Connection conn;
	
	public UsuarioDAO(Connection conn) {
		this.conn = conn;
	}
	
	public boolean inserir(Usuario usuario) {
	    PreparedStatement stmt = null;
	  //  Connection conn = null; // Certifique-se de obter a conexão aqui dentro ou passar por parâmetro
	    
	    String sql = "INSERT INTO tb_usuario (nome_usuario, email, senha, papel) values (?, ?, ?, ?)";
	    
	    try {
	        // 1. Obtém a conexão com o banco
	      //  conn = ConnectionFactory.getConnection();
	        
	        // 2. Prepara o statement
	        stmt = conn.prepareStatement(sql);
	        stmt.setString(1, usuario.getNome());
	        stmt.setString(2, usuario.getEmail());
	        stmt.setString(3, usuario.getSenha());
	        
	        // A MÁGICA ESTÁ AQUI: O .name() transforma o Enum ADMIN em uma String "ADMIN"
	        stmt.setString(4, usuario.getAcesso().name());
	        
	        // 3. Executa a inserção no banco
	        int linhasAfetadas = stmt.executeUpdate();
	        
	        // Se inseriu pelo menos 1 linha, retorna true
	        return linhasAfetadas > 0;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false; // Se der erro (ex: e-mail duplicado), retorna false
	    } finally {
	        // 4. Sempre feche os recursos para evitar travamento do banco
	        try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
	       // try { if (conn != null) conn.close(); } catch (SQLException e) {}
	    }
	}
	
	

    public Usuario autenticar(String email, String senha) { //SENHA HASHEADA
     //   Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Usuario usuario = null;

        try {
            // 1. Obtém a conexão com o banco de dados
        //    conn = ConnectionFactory.getConnection(); 
        	

            // 2. Prepara a query SQL (Segura contra SQL Injection)
            String sql = "SELECT id_usuario, nome_usuario, email, papel FROM tb_usuario WHERE email = ? AND senha = ?";
            stmt = conn.prepareStatement(sql);
            // Nota: A senha ja está hasheada (o AuthServlet passa a senha hasheada como parâmetro na chamada do método)
            stmt.setString(1, email);
            stmt.setString(2, senha); // O banco vai comparar HASH com HASH
            
            // 3. Executa a query
            rs = stmt.executeQuery();
            
            

            // 4. Se encontrou o usuário, preenche o objeto
            if (rs.next()) {
                usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNome(rs.getString("nome_usuario"));
                usuario.setEmail(rs.getString("email"));
                
                // Converte a String do ENUM do MySQL para o Enum do Java
                String papelBanco = rs.getString("papel");
                usuario.setAcesso(NivelAcesso.valueOf(papelBanco));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fecha os recursos do banco (Boa prática obrigatória)
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
           // try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        return usuario; // Retorna o usuário preenchido ou null se falhar
    }
    
    // OUTROS MÉTODOS
    
 // Para o Administrador mudar o nível de acesso de um usuário (RF16)
    public boolean alterarPapel(int idUsuario, NivelAcesso novoAcesso) {
        PreparedStatement stmt = null;
        String sql = "UPDATE tb_usuario SET papel = ? WHERE id_usuario = ?";
        
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, novoAcesso.name());
            stmt.setInt(2, idUsuario);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    // Para o Administrador listar todos os usuários no painel de controle
    public java.util.List<Usuario> listarTodos() {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        java.util.ArrayList<Usuario> lista = new java.util.ArrayList<>();
        String sql = "SELECT id_usuario, nome_usuario, email, papel FROM tb_usuario";
        
        try {
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNome(rs.getString("nome_usuario"));
                u.setEmail(rs.getString("email"));
                u.setAcesso(NivelAcesso.valueOf(rs.getString("papel")));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return lista;
    }
    
    
    
}