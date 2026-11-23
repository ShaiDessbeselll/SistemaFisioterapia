package model;

/**
 * MEMENTO: uma "foto" imutável dos campos clínicos do Prontuario em um momento.
 * Só o Prontuario cria esta foto (construtor visível apenas dentro do pacote model),
 * e ninguém consegue alterá-la depois: ela só guarda o estado.
 */
public class ProntuarioMemento {

    private final String anamnese;
    private final String diagnosticoClinico;
    private final String diagnosticoFisioterapeutico;
    private final String exameFisico;
    private final String objetivos;
    private final String conduta;

    ProntuarioMemento(String anamnese, String diagnosticoClinico, String diagnosticoFisioterapeutico,
                      String exameFisico, String objetivos, String conduta) {
        this.anamnese = anamnese;
        this.diagnosticoClinico = diagnosticoClinico;
        this.diagnosticoFisioterapeutico = diagnosticoFisioterapeutico;
        this.exameFisico = exameFisico;
        this.objetivos = objetivos;
        this.conduta = conduta;
    }

    public String getAnamnese() {
        return anamnese;
    }

    public String getDiagnosticoClinico() {
        return diagnosticoClinico;
    }

    public String getDiagnosticoFisioterapeutico() {
        return diagnosticoFisioterapeutico;
    }

    public String getExameFisico() {
        return exameFisico;
    }

    public String getObjetivos() {
        return objetivos;
    }

    public String getConduta() {
        return conduta;
    }
}
