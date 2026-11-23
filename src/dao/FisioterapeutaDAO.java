package dao;

import model.Fisioterapeuta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO do Fisioterapeuta. Ele fica em três tabelas: pessoa, funcionario e fisioterapeuta
 * (o mesmo desenho da herança Pessoa, Funcionario, Fisioterapeuta no diagrama).
 */
public class FisioterapeutaDAO {

    private static final String SELECT_BASE =
            "SELECT p.id, p.nome, p.cpf, p.telefone, f.matricula, f.data_admissao, "
            + "fi.crefito, fi.especialidade "
            + "FROM pessoa p "
            + "JOIN funcionario f ON f.id_pessoa = p.id "
            + "JOIN fisioterapeuta fi ON fi.id_funcionario = f.id_pessoa ";

    public void inserir(Fisioterapeuta fisioterapeuta) {
        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false);
            try {
                PessoaDAO.inserirFuncionario(conn, fisioterapeuta);
                String sql = "INSERT INTO fisioterapeuta (id_funcionario, crefito, especialidade) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, fisioterapeuta.getId());
                    ps.setString(2, fisioterapeuta.getCrefito());
                    ps.setString(3, fisioterapeuta.getEspecialidade());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir fisioterapeuta: " + e.getMessage(), e);
        }
    }

    /** Busca pelo id. Devolve null se não existir. */
    public Fisioterapeuta buscarPorId(int id) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar fisioterapeuta: " + e.getMessage(), e);
        }
    }

    public List<Fisioterapeuta> listar() {
        List<Fisioterapeuta> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY p.nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar fisioterapeutas: " + e.getMessage(), e);
        }
        return lista;
    }

    private Fisioterapeuta montar(ResultSet rs) throws SQLException {
        return new Fisioterapeuta(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("telefone"),
                rs.getString("matricula"),
                PessoaDAO.paraLocalDate(rs.getDate("data_admissao")),
                rs.getString("crefito"),
                rs.getString("especialidade"));
    }
}
