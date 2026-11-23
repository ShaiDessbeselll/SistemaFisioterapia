package dao;

import model.Funcionario;
import model.Pessoa;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Apoio dos outros DAOs (visível só dentro do pacote dao).
 * Como Paciente, Fisioterapeuta e Secretaria herdam de Pessoa, os dados comuns
 * ficam nas tabelas "pessoa" e "funcionario". Esta classe evita repetir esse código.
 */
class PessoaDAO {

    /** Grava na tabela pessoa e devolve o id gerado pelo banco (também colocado no objeto). */
    static int inserirPessoa(Connection conn, Pessoa pessoa) throws SQLException {
        String sql = "INSERT INTO pessoa (nome, cpf, telefone) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pessoa.getNome());
            ps.setString(2, pessoa.getCpf());
            ps.setString(3, pessoa.getTelefone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                int id = rs.getInt(1);
                pessoa.setId(id);
                return id;
            }
        }
    }

    /** Grava nas tabelas pessoa e funcionario. */
    static void inserirFuncionario(Connection conn, Funcionario funcionario) throws SQLException {
        inserirPessoa(conn, funcionario);
        String sql = "INSERT INTO funcionario (id_pessoa, matricula, data_admissao) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, funcionario.getId());
            ps.setString(2, funcionario.getMatricula());
            ps.setDate(3, paraSql(funcionario.getDataAdmissao()));
            ps.executeUpdate();
        }
    }

    /** Atualiza nome, cpf e telefone na tabela pessoa. */
    static void atualizarPessoa(Connection conn, Pessoa pessoa) throws SQLException {
        String sql = "UPDATE pessoa SET nome = ?, cpf = ?, telefone = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pessoa.getNome());
            ps.setString(2, pessoa.getCpf());
            ps.setString(3, pessoa.getTelefone());
            ps.setInt(4, pessoa.getId());
            ps.executeUpdate();
        }
    }

    /** Converte LocalDate (Java) em Date (SQL), aceitando valor nulo. */
    static Date paraSql(LocalDate data) {
        return data == null ? null : Date.valueOf(data);
    }

    /** Converte Date (SQL) em LocalDate (Java), aceitando valor nulo. */
    static LocalDate paraLocalDate(Date data) {
        return data == null ? null : data.toLocalDate();
    }
}
