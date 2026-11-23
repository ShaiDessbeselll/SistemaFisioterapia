package dao;

import model.Secretaria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da Secretaria. Ela fica em três tabelas: pessoa, funcionario e secretaria.
 */
public class SecretariaDAO {

    private static final String SELECT_BASE =
            "SELECT p.id, p.nome, p.cpf, p.telefone, f.matricula, f.data_admissao, s.turno "
            + "FROM pessoa p "
            + "JOIN funcionario f ON f.id_pessoa = p.id "
            + "JOIN secretaria s ON s.id_funcionario = f.id_pessoa ";

    public void inserir(Secretaria secretaria) {
        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false);
            try {
                PessoaDAO.inserirFuncionario(conn, secretaria);
                String sql = "INSERT INTO secretaria (id_funcionario, turno) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, secretaria.getId());
                    ps.setString(2, secretaria.getTurno());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir secretária: " + e.getMessage(), e);
        }
    }

    /** Busca pelo id. Devolve null se não existir. */
    public Secretaria buscarPorId(int id) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar secretária: " + e.getMessage(), e);
        }
    }

    public List<Secretaria> listar() {
        List<Secretaria> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY p.nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar secretárias: " + e.getMessage(), e);
        }
        return lista;
    }

    private Secretaria montar(ResultSet rs) throws SQLException {
        return new Secretaria(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("telefone"),
                rs.getString("matricula"),
                PessoaDAO.paraLocalDate(rs.getDate("data_admissao")),
                rs.getString("turno"));
    }
}
