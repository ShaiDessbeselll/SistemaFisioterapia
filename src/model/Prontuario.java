package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Prontuário clínico de um paciente.
 *
 * BUILDER: o construtor é privado, então o objeto só é criado pela classe interna
 * Prontuario.Builder, que monta o prontuário passo a passo.
 *
 * MEMENTO: este é o "originador". Ele sabe tirar uma foto do seu estado
 * (salvarEstado) e voltar a uma foto anterior (restaurarEstado).
 *
 * É uma COMPOSIÇÃO: as evoluções pertencem ao prontuário e não existem sem ele.
 */
public class Prontuario {

    private int id;
    private final Paciente paciente;
    private LocalDate dataAbertura;
    private String anamnese;
    private String diagnosticoClinico;
    private String diagnosticoFisioterapeutico;
    private String exameFisico;
    private String objetivos;
    private String conduta;
    private final List<Evolucao> evolucoes = new ArrayList<>();

    /** Construtor PRIVADO: só o Builder consegue chamá-lo. */
    private Prontuario(Builder builder) {
        this.paciente = builder.paciente;
        this.dataAbertura = LocalDate.now();
        this.anamnese = builder.anamnese;
        this.diagnosticoClinico = builder.diagnosticoClinico;
        this.diagnosticoFisioterapeutico = builder.diagnosticoFisioterapeutico;
        this.exameFisico = builder.exameFisico;
        this.objetivos = builder.objetivos;
        this.conduta = builder.conduta;
    }

    // ---------- MEMENTO ----------

    /** Tira uma "foto" dos campos clínicos neste momento. */
    public ProntuarioMemento salvarEstado() {
        return new ProntuarioMemento(anamnese, diagnosticoClinico, diagnosticoFisioterapeutico,
                exameFisico, objetivos, conduta);
    }

    /** Volta os campos clínicos para o que estava na foto. */
    public void restaurarEstado(ProntuarioMemento memento) {
        this.anamnese = memento.getAnamnese();
        this.diagnosticoClinico = memento.getDiagnosticoClinico();
        this.diagnosticoFisioterapeutico = memento.getDiagnosticoFisioterapeutico();
        this.exameFisico = memento.getExameFisico();
        this.objetivos = memento.getObjetivos();
        this.conduta = memento.getConduta();
    }

    // ---------- Evoluções ----------

    public void adicionarEvolucao(Evolucao evolucao) {
        evolucoes.add(evolucao);
    }

    /** Devolve uma cópia da lista, para ninguém alterar as evoluções por fora. */
    public List<Evolucao> listarEvolucoes() {
        return new ArrayList<>(evolucoes);
    }

    // ---------- Getters e setters ----------

    public int getId() {
        return id;
    }

    /** Usado pelo DAO depois de gravar no banco. */
    public void setId(int id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public LocalDate getDataAbertura() {
        return dataAbertura;
    }

    /** Usado pelo DAO ao carregar um prontuário já existente do banco. */
    public void setDataAbertura(LocalDate dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public String getAnamnese() {
        return anamnese;
    }

    public void setAnamnese(String anamnese) {
        this.anamnese = anamnese;
    }

    public String getDiagnosticoClinico() {
        return diagnosticoClinico;
    }

    public void setDiagnosticoClinico(String diagnosticoClinico) {
        this.diagnosticoClinico = diagnosticoClinico;
    }

    public String getDiagnosticoFisioterapeutico() {
        return diagnosticoFisioterapeutico;
    }

    public void setDiagnosticoFisioterapeutico(String diagnosticoFisioterapeutico) {
        this.diagnosticoFisioterapeutico = diagnosticoFisioterapeutico;
    }

    public String getExameFisico() {
        return exameFisico;
    }

    public void setExameFisico(String exameFisico) {
        this.exameFisico = exameFisico;
    }

    public String getObjetivos() {
        return objetivos;
    }

    public void setObjetivos(String objetivos) {
        this.objetivos = objetivos;
    }

    public String getConduta() {
        return conduta;
    }

    public void setConduta(String conduta) {
        this.conduta = conduta;
    }

    /**
     * BUILDER: monta o Prontuario passo a passo.
     * O paciente é obrigatório (vem no construtor); os demais campos são opcionais
     * e cada método devolve o próprio Builder, permitindo encadear as chamadas.
     */
    public static class Builder {

        private final Paciente paciente;
        private String anamnese = "";
        private String diagnosticoClinico = "";
        private String diagnosticoFisioterapeutico = "";
        private String exameFisico = "";
        private String objetivos = "";
        private String conduta = "";

        public Builder(Paciente paciente) {
            if (paciente == null) {
                throw new IllegalArgumentException("O prontuário precisa de um paciente.");
            }
            this.paciente = paciente;
        }

        public Builder anamnese(String valor) {
            this.anamnese = valor;
            return this;
        }

        public Builder diagnosticoClinico(String valor) {
            this.diagnosticoClinico = valor;
            return this;
        }

        public Builder diagnosticoFisioterapeutico(String valor) {
            this.diagnosticoFisioterapeutico = valor;
            return this;
        }

        public Builder exameFisico(String valor) {
            this.exameFisico = valor;
            return this;
        }

        public Builder objetivos(String valor) {
            this.objetivos = valor;
            return this;
        }

        public Builder conduta(String valor) {
            this.conduta = valor;
            return this;
        }

        /** Cria o Prontuario com tudo o que foi informado. */
        public Prontuario build() {
            return new Prontuario(this);
        }
    }
}
