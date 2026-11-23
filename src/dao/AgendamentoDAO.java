package dao;

import model.Agendamento;
import model.Fisioterapeuta;
import model.Paciente;
import model.Secretaria;
import model.StatusAgendamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO do Agendamento. Ao carregar um agendamento, ele usa os outros DAOs
 * para montar também o paciente, o fisioterapeuta e a secretária.
 */
public class AgendamentoDAO {

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final FisioterapeutaDAO fisioterapeutaDAO = new FisioterapeutaDAO();
    private final SecretariaDAO secretariaDAO = new SecretariaDAO();

    /** Grava o agendamento e coloca no objeto o id gerado pelo banco. */
    public void inserir(Agendamento agendamento) {
        String sql = "INSERT INTO agendamento (data, horario, status, id_paciente, id_fisioterapeuta, id_secretaria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, PessoaDAO.paraSql(agendamento.getData()));
            ps.setTime(2, Time.valueOf(agendamento.getHorario()));
            ps.setString(3, agendamento.getStatus().name());
            ps.setInt(4, agendamento.getPaciente().getId());
            ps.setInt(5, agendamento.getFisioterapeuta().getId());
            ps.setInt(6, agendamento.getSecretaria().getId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    agendamento.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir agendamento: " + e.getMessage(), e);
        }
    }

    /**
     * Salva no banco a data, o horário, o status, o paciente e o fisioterapeuta atuais
     * (depois de confirmar, cancelar, remarcar ou editar).
     */
    public void atualizar(Agendamento agendamento) {
        String sql = "UPDATE agendamento SET data = ?, horario = ?, status = ?, "
                + "id_paciente = ?, id_fisioterapeuta = ? WHERE id = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, PessoaDAO.paraSql(agendamento.getData()));
            ps.setTime(2, Time.valueOf(agendamento.getHorario()));
            ps.setString(3, agendamento.getStatus().name());
            ps.setInt(4, agendamento.getPaciente().getId());
            ps.setInt(5, agendamento.getFisioterapeuta().getId());
            ps.setInt(6, agendamento.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar agendamento: " + e.getMessage(), e);
        }
    }

    /** Busca pelo id. Devolve null se não existir. */
    public Agendamento buscarPorId(int id) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM agendamento WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar agendamento: " + e.getMessage(), e);
        }
    }

    /** Lista todos os agendamentos, por data e horário. */
    public List<Agendamento> listar() {
        List<Agendamento> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM agendamento ORDER BY data, horario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos: " + e.getMessage(), e);
        }
        return lista;
    }

    public void excluir(int id) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM agendamento WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir agendamento: " + e.getMessage(), e);
        }
    }

    private Agendamento montar(ResultSet rs) throws SQLException {
        Paciente paciente = pacienteDAO.buscarPorId(rs.getInt("id_paciente"));
        Fisioterapeuta fisioterapeuta = fisioterapeutaDAO.buscarPorId(rs.getInt("id_fisioterapeuta"));
        Secretaria secretaria = secretariaDAO.buscarPorId(rs.getInt("id_secretaria"));

        Agendamento agendamento = new Agendamento(
                rs.getInt("id"),
                PessoaDAO.paraLocalDate(rs.getDate("data")),
                rs.getTime("horario").toLocalTime(),
                paciente,
                fisioterapeuta,
                secretaria);
        agendamento.setStatus(StatusAgendamento.valueOf(rs.getString("status")));
        return agendamento;
    }
}
