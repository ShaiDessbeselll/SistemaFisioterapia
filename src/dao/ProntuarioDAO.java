package dao;

import model.Evolucao;
import model.Fisioterapeuta;
import model.Paciente;
import model.Prontuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DAO do Prontuario e das suas Evolucoes (que pertencem ao prontuário).
 * Ao carregar um prontuário do banco, ele é remontado com o padrão Builder.
 */
public class ProntuarioDAO {

    private final FisioterapeutaDAO fisioterapeutaDAO = new FisioterapeutaDAO();

    /** Grava o prontuário e coloca no objeto o id gerado pelo banco. */
    public void inserir(Prontuario prontuario) {
        String sql = "INSERT INTO prontuario (data_abertura, anamnese, diagnostico_clinico, "
                + "diagnostico_fisioterapeutico, exame_fisico, objetivos, conduta, id_paciente) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, PessoaDAO.paraSql(prontuario.getDataAbertura()));
            ps.setString(2, prontuario.getAnamnese());
            ps.setString(3, prontuario.getDiagnosticoClinico());
            ps.setString(4, prontuario.getDiagnosticoFisioterapeutico());
            ps.setString(5, prontuario.getExameFisico());
            ps.setString(6, prontuario.getObjetivos());
            ps.setString(7, prontuario.getConduta());
            ps.setInt(8, prontuario.getPaciente().getId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    prontuario.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir prontuário: " + e.getMessage(), e);
        }
    }

    /** Atualiza no banco os campos clínicos do prontuário (o id precisa estar preenchido). */
    public void atualizar(Prontuario prontuario) {
        String sql = "UPDATE prontuario SET anamnese = ?, diagnostico_clinico = ?, "
                + "diagnostico_fisioterapeutico = ?, exame_fisico = ?, objetivos = ?, conduta = ? WHERE id = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prontuario.getAnamnese());
            ps.setString(2, prontuario.getDiagnosticoClinico());
            ps.setString(3, prontuario.getDiagnosticoFisioterapeutico());
            ps.setString(4, prontuario.getExameFisico());
            ps.setString(5, prontuario.getObjetivos());
            ps.setString(6, prontuario.getConduta());
            ps.setInt(7, prontuario.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar prontuário: " + e.getMessage(), e);
        }
    }

    /**
     * Busca o prontuário de um paciente (cada paciente tem no máximo um),
     * já com a lista de evoluções. Devolve null se o paciente ainda não tem prontuário.
     */
    public Prontuario buscarPorPaciente(Paciente paciente) {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM prontuario WHERE id_paciente = ?")) {
            ps.setInt(1, paciente.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                // Remonta o objeto usando o Builder
                Prontuario prontuario = new Prontuario.Builder(paciente)
                        .anamnese(rs.getString("anamnese"))
                        .diagnosticoClinico(rs.getString("diagnostico_clinico"))
                        .diagnosticoFisioterapeutico(rs.getString("diagnostico_fisioterapeutico"))
                        .exameFisico(rs.getString("exame_fisico"))
                        .objetivos(rs.getString("objetivos"))
                        .conduta(rs.getString("conduta"))
                        .build();
                prontuario.setId(rs.getInt("id"));
                prontuario.setDataAbertura(PessoaDAO.paraLocalDate(rs.getDate("data_abertura")));
                carregarEvolucoes(prontuario);
                paciente.setProntuario(prontuario);
                return prontuario;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar prontuário: " + e.getMessage(), e);
        }
    }

    /**
     * Grava uma evolução no banco, ligada ao prontuário (que já precisa ter sido gravado).
     * O id gerado é colocado na própria evolução.
     */
    public void inserirEvolucao(Prontuario prontuario, Evolucao evolucao) {
        String sql = "INSERT INTO evolucao (data, descricao, id_prontuario, id_fisioterapeuta) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, PessoaDAO.paraSql(evolucao.getData()));
            ps.setString(2, evolucao.getDescricao());
            ps.setInt(3, prontuario.getId());
            ps.setInt(4, evolucao.getFisioterapeuta().getId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    evolucao.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir evolução: " + e.getMessage(), e);
        }
    }

    /** Grava no banco o texto e a situação (ativa/inativa) da evolução. */
    public void atualizarEvolucao(Evolucao evolucao) {
        String sql = "UPDATE evolucao SET descricao = ?, ativa = ? WHERE id = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evolucao.getDescricao());
            ps.setBoolean(2, evolucao.isAtiva());
            ps.setInt(3, evolucao.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar evolução: " + e.getMessage(), e);
        }
    }

    /** Lê as evoluções do prontuário no banco e as adiciona ao objeto, da mais antiga para a mais nova. */
    private void carregarEvolucoes(Prontuario prontuario) throws SQLException {
        String sql = "SELECT * FROM evolucao WHERE id_prontuario = ? ORDER BY data, id";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prontuario.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Fisioterapeuta fisio = fisioterapeutaDAO.buscarPorId(rs.getInt("id_fisioterapeuta"));
                    Evolucao evolucao = new Evolucao(
                            rs.getInt("id"),
                            PessoaDAO.paraLocalDate(rs.getDate("data")),
                            rs.getString("descricao"),
                            fisio);
                    evolucao.setAtiva(rs.getBoolean("ativa"));
                    prontuario.adicionarEvolucao(evolucao);
                }
            }
        }
    }
}
