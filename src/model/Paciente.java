package model;

import java.time.LocalDate;

/**
 * Paciente da clínica. Herda os dados básicos de Pessoa.
 * Cada paciente tem um único prontuário (relação 1 para 1 do diagrama).
 */
public class Paciente extends Pessoa {

    private LocalDate dataNascimento;
    private String queixa;
    private Prontuario prontuario;

    public Paciente() {
    }

    public Paciente(int id, String nome, String cpf, String telefone,
                    LocalDate dataNascimento, String queixa) {
        super(id, nome, cpf, telefone);
        this.dataNascimento = dataNascimento;
        this.queixa = queixa;
    }

    /** Devolve o prontuário do paciente (ou null, se ainda não foi aberto). */
    public Prontuario consultarProntuario() {
        return prontuario;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getQueixa() {
        return queixa;
    }

    public void setQueixa(String queixa) {
        this.queixa = queixa;
    }

    public void setProntuario(Prontuario prontuario) {
        this.prontuario = prontuario;
    }
}
