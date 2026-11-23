package dao;

import model.AcaoAuditoria;
import model.Fisioterapeuta;
import model.Funcionario;
import model.RegistroAuditoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da auditoria do prontuário. Só tem DUAS operações de propósito: gravar um registro novo
 * e listar. Não existe alterar nem apagar, para o histórico não poder ser mexido pelo sistema.
 * A data e a hora vêm do relógio do banco de dados (CURRENT_TIMESTAMP), não do computador do usuário.
 */
public class AuditoriaDAO {

    public void registrar(int idProntuario, Funcionario usuario, AcaoAuditoria acao, String detalhes) {
        String sql = "INSERT INTO auditoria_prontuario "
                + "(id_prontuario, id_funcionario, nome_usuario, acao, detalhes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProntuario);
            ps.setInt(2, usuario.getId());
            ps.setString(3, nomeComCargo(usuario));
            ps.setString(4, acao.name());
            ps.setString(5, detalhes);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gravar a auditoria: " + e.getMessage(), e);
        }
    }

    /** Registros do prontuário, do mais novo para o mais antigo. */
    public List<RegistroAuditoria> listarPorProntuario(int idProntuario) {
        String sql = "SELECT * FROM auditoria_prontuario WHERE id_prontuario = ? ORDER BY data_hora DESC, id DESC";
        List<RegistroAuditoria> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProntuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp quando = rs.getTimestamp("data_hora");
                    lista.add(new RegistroAuditoria(
                            rs.getInt("id"),
                            quando == null ? null : quando.toLocalDateTime(),
                            rs.getString("nome_usuario"),
                            rs.getString("acao"),
                            rs.getString("detalhes")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao ler a auditoria: " + e.getMessage(), e);
        }
        return lista;
    }

    private static String nomeComCargo(Funcionario usuario) {
        String cargo = usuario instanceof Fisioterapeuta ? "Fisioterapeuta" : "Secretária";
        return usuario.getNome() + " (" + cargo + ")";
    }
}
