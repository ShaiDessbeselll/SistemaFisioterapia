package dao;

import model.Fisioterapeuta;
import model.Funcionario;
import util.Senha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO do login: confere usuário e senha na tabela funcionario.
 * A senha digitada nunca é comparada diretamente: compara-se o hash (veja a classe Senha).
 */
public class UsuarioDAO {

    private final FisioterapeutaDAO fisioterapeutaDAO = new FisioterapeutaDAO();
    private final SecretariaDAO secretariaDAO = new SecretariaDAO();

    /**
     * Devolve o funcionário (Fisioterapeuta ou Secretaria) se o login e a senha estiverem certos;
     * senão devolve null.
     */
    public Funcionario autenticar(String login, String senha) {
        String sql = "SELECT id_pessoa, senha_salt, senha_hash FROM funcionario WHERE login = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null; // não existe esse login
                }
                String salt = rs.getString("senha_salt");
                String hash = rs.getString("senha_hash");
                if (salt == null || hash == null || !Senha.confere(senha, salt, hash)) {
                    return null; // senha errada
                }
                return carregar(rs.getInt("id_pessoa"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao fazer login: " + e.getMessage(), e);
        }
    }

    /** Descobre se o funcionário é fisioterapeuta ou secretária e o carrega completo. */
    private Funcionario carregar(int id) {
        Fisioterapeuta fisioterapeuta = fisioterapeutaDAO.buscarPorId(id);
        if (fisioterapeuta != null) {
            return fisioterapeuta;
        }
        return secretariaDAO.buscarPorId(id);
    }
}
