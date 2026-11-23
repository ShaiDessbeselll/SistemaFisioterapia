package model;

import java.time.LocalDate;

/**
 * Classe ABSTRATA: reúne o que os profissionais da clínica têm em comum.
 * Herda de Pessoa. Fisioterapeuta e Secretaria herdam desta classe.
 */
public abstract class Funcionario extends Pessoa {

    private String matricula;
    private LocalDate dataAdmissao;

    public Funcionario() {
    }

    public Funcionario(int id, String nome, String cpf, String telefone,
                       String matricula, LocalDate dataAdmissao) {
        super(id, nome, cpf, telefone); // chama o construtor de Pessoa
        this.matricula = matricula;
        this.dataAdmissao = dataAdmissao;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }
}
