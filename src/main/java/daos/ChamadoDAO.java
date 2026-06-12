package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import models.Chamado;

public class ChamadoDAO {
    
    private Connection conn;

    public ChamadoDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Insere um novo chamado no banco de dados (Ação do Cliente)
     */
    public boolean inserirChamado(Chamado chamado) {
        PreparedStatement stmt = null;
        
        // Repare que id_tecnico e observacoes_tecnico não entram no INSERT,
        // pois o chamado nasce sem técnico (NULL no banco) e sem observações.
        String sql = "INSERT INTO tb_chamado (id_cliente, id_categoria, titulo, descricao, status, data_abertura, data_atualizacao) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            stmt = conn.prepareStatement(sql);
            
            // 1. Chaves estrangeiras (Pega os IDs de dentro dos objetos Cliente e Categoria)
            stmt.setInt(1, chamado.getCliente().getIdUsuario());
            stmt.setInt(2, chamado.getCategoria().getIdCategoria());
            
            // 2. Textos
            stmt.setString(3, chamado.getTitulo());
            stmt.setString(4, chamado.getDescricao());
            
            // 3. Status (Transforma o Enum do Java em String para o MySQL)
            stmt.setString(5, chamado.getStatus().name());
            
            // 4. Datas (Converte o LocalDateTime do Java 8 para o java.sql.Timestamp exigido pelo JDBC)
            stmt.setTimestamp(6, Timestamp.valueOf(chamado.getDataAbertura()));
            stmt.setTimestamp(7, Timestamp.valueOf(chamado.getDataAtualizacao()));
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
        
    }
    
    /**
     * Retorna o histórico de chamados de um cliente específico
     */
    public java.util.List<Chamado> listarPorCliente(int idCliente) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        java.util.List<Chamado> lista = new java.util.ArrayList<>();

        // JOIN cuidadoso para buscar a Categoria e o Técnico (LEFT JOIN porque pode ser nulo)
        String sql = "SELECT ch.id_chamado, ch.titulo, ch.status, ch.data_abertura, " +
                     "cat.nome_categoria, tec.nome_usuario AS nome_tecnico " +
                     "FROM tb_chamado ch " +
                     "INNER JOIN tb_categoria cat ON ch.id_categoria = cat.id_categoria " +
                     "LEFT JOIN tb_usuario tec ON ch.id_tecnico = tec.id_usuario " +
                     "WHERE ch.id_cliente = ? ORDER BY ch.data_abertura DESC";

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCliente);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Chamado c = new Chamado();
                c.setIdChamado(rs.getInt("id_chamado"));
                c.setTitulo(rs.getString("titulo"));
                c.setStatus(models.StatusChamado.valueOf(rs.getString("status")));
                
                // Converte Timestamp do banco de volta para o seu LocalDateTime
                c.setDataAbertura(rs.getTimestamp("data_abertura").toLocalDateTime());

                // Monta o objeto Categoria com o que veio do JOIN
                models.CategoriaChamado cat = new models.CategoriaChamado();
                cat.setNome(rs.getString("nome_categoria"));
                c.setCategoria(cat);

                // Monta o objeto Técnico apenas se alguém já tiver assumido o chamado
                String nomeTecnico = rs.getString("nome_tecnico");
                if (nomeTecnico != null) {
                    models.Usuario tecnico = new models.Usuario();
                    tecnico.setNome(nomeTecnico);
                    c.setTecnico(tecnico);
                }

                lista.add(c);
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