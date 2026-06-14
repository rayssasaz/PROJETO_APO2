package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import daos.CategoriaDAO;
import daos.ChamadoDAO;
import database.DBConnection;
import models.CategoriaChamado;
import models.Chamado;
import models.NivelAcesso;
import models.Usuario;

@WebServlet("/suporte/dashboard")
public class SuporteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Usuario tecnicoLogado = (Usuario) request.getSession().getAttribute("usuarioAutenticado");
        
        if (tecnicoLogado == null || !NivelAcesso.SUPORTE.equals(tecnicoLogado.getAcesso())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        DBConnection db = new DBConnection();
        Connection conn = null;

        try {
            conn = db.getConnection();
            ChamadoDAO chamadoDAO = new ChamadoDAO(conn);
            CategoriaDAO categoriaDAO = new CategoriaDAO(conn); // DAO de categorias adicionada aqui
            
            // 1. Busca os chamados da fila baseados nas especialidades
            List<Chamado> filaEspecializada = chamadoDAO.listarFilaDoTecnico(tecnicoLogado.getIdUsuario());
            
            // 2. Busca as especialidades do próprio técnico para exibir no topo da tela
            List<CategoriaChamado> minhasEspecialidades = categoriaDAO.listarPorTecnico(tecnicoLogado.getIdUsuario());
            
            // Pendura os dois objetos na requisição
            request.setAttribute("listaChamados", filaEspecializada);
            request.setAttribute("minhasEspecialidades", minhasEspecialidades);
            
            request.getRequestDispatcher("/suporte/dashboardSuporte.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
}