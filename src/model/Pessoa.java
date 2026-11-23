package model;

/**
 * Classe ABSTRATA: reúne os dados comuns a todas as pessoas do sistema.
 * Não pode ser instanciada diretamente (não existe "só uma pessoa"):
 * ela só existe como Paciente ou como Funcionario.
 */
public abstract class Pessoa {

    private int id;
    private String nome;
    private String cpf;
    private String telefone;

    public Pessoa() {
    }

    public Pessoa(int id, String nome, String cpf, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public String toString() {
        return nome + " (CPF: " + cpf + ")";
    }
}
