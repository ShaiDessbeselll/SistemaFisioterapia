package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Responsável por abrir a conexão com o banco MySQL "clinica_fisioterapia".
 * Requer o conector do MySQL (arquivo .jar) na pasta lib/ e no classpath do projeto.
 */
public class Conexao {

    private static final String URL =
            "jdbc:mysql://localhost:3306/clinica_fisioterapia"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo";
    private static final String USUARIO = "root";
    private static final String SENHA = "15122007"; // troque pela sua senha do MySQL

    /** Abre e devolve uma nova conexão. Quem chamar deve fechá-la depois. */
    public static Connection getConexao() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar no banco: " + e.getMessage(), e);
        }
    }
}
