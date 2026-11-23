package model;

import java.time.LocalDate;

/**
 * Fisioterapeuta: funcionário que atende os pacientes e registra as evoluções.
 */
public class Fisioterapeuta extends Funcionario {

    private String crefito;
    private String especialidade;

    public Fisioterapeuta() {
    }

    public Fisioterapeuta(int id, String nome, String cpf, String telefone,
                          String matricula, LocalDate dataAdmissao,
                          String crefito, String especialidade) {
        super(id, nome, cpf, telefone, matricula, dataAdmissao);
        this.crefito = crefito;
        this.especialidade = especialidade;
    }

    /**
     * Abre um prontuário (em branco) para o paciente, usando o Builder,
     * e o associa ao paciente. Os campos clínicos podem ser preenchidos
     * montando o prontuário diretamente com Prontuario.Builder.
     */
    public Prontuario abrirProntuario(Paciente paciente) {
        Prontuario prontuario = new Prontuario.Builder(paciente).build();
        paciente.setProntuario(prontuario);
        return prontuario;
    }

    /**
     * Registra uma evolução no prontuário do paciente.
     * Cria a Evolucao (com a data de hoje e este fisioterapeuta) e a adiciona ao prontuário.
     */
    public void registrarEvolucao(Prontuario prontuario, String descricao) {
        Evolucao evolucao = new Evolucao(0, LocalDate.now(), descricao, this);
        prontuario.adicionarEvolucao(evolucao);
    }

    public String getCrefito() {
        return crefito;
    }

    public void setCrefito(String crefito) {
        this.crefito = crefito;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
}
