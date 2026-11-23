package dao;

import model.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) do Paciente: concentra todo o acesso ao banco
 * relacionado a pacientes. Assim o resto do sistema não precisa conhecer SQL.
 * O paciente fica em duas tabelas: pessoa (dados comuns) e paciente (dados próprios).
 */
public class PacienteDAO {

    private static final String SELECT_BASE =
            "SELECT p.id, p.nome, p.cpf, p.telefone, pa.data_nascimento, pa.queixa "
            + "FROM pessoa p JOIN paciente pa ON pa.id_pessoa = p.id ";

    /** Grava o paciente. As duas tabelas são gravadas juntas (transação): ou grava tudo, ou nada. */
    public void inserir(Paciente paciente) {
        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false);
            try {
                PessoaDAO.inserirPessoa(conn, paciente);
                String sql = "INSERT INTO paciente (id_pessoa, data_nascimento, queixa) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, paciente.getId());
                    ps.setDate(2, PessoaDAO.paraSql(paciente.getDataNascimento()));
                    ps.setString(3, paciente.getQueixa());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir paciente: " + e.getMessage(), e);
        }
    }

    /** Busca um paciente pelo id. Devolve null se não existir. */
    public Paciente buscarPorId(int id) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar paciente: " + e.getMessage(), e);
        }
    }

    /** Lista todos os pacientes, em ordem alfabética. */
    public List<Paciente> listar() {
        List<Paciente> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY p.nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pacientes: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Atualiza os dados do paciente (o id precisa estar preenchido). */
    public void atualizar(Paciente paciente) {
        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false);
            try {
                PessoaDAO.atualizarPessoa(conn, paciente);
                String sql = "UPDATE paciente SET data_nascimento = ?, queixa = ? WHERE id_pessoa = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDate(1, PessoaDAO.paraSql(paciente.getDataNascimento()));
                    ps.setString(2, paciente.getQueixa());
                    ps.setInt(3, paciente.getId());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar paciente: " + e.getMessage(), e);
        }
    }

    /**
     * Exclui o paciente. Se ele já tiver agendamento ou prontuário, o banco recusa
     * (para não perder histórico clínico) e é lançado um erro.
     */
    public void excluir(int id) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM pessoa WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir paciente: " + e.getMessage(), e);
        }
    }

    /** Transforma uma linha do resultado em um objeto Paciente. */
    private Paciente montar(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("telefone"),
                PessoaDAO.paraLocalDate(rs.getDate("data_nascimento")),
                rs.getString("queixa"));
    }
}
