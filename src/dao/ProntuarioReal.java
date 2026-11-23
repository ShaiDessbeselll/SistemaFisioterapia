package dao;

import model.Evolucao;
import model.Prontuario;

import java.util.List;

/**
 * PROXY (objeto real): faz o acesso de verdade ao prontuário, sem nenhuma verificação.
 * Por isso ele nunca deve ser usado diretamente pelas telas: o acesso passa pelo proxy.
 */
public class ProntuarioReal implements ProntuarioAcesso {

    private final Prontuario prontuario;

    public ProntuarioReal(Prontuario prontuario) {
        this.prontuario = prontuario;
    }

    @Override
    public String verDadosClinicos() {
        return "Anamnese: " + prontuario.getAnamnese() + "\n"
                + "Diagnóstico clínico: " + prontuario.getDiagnosticoClinico() + "\n"
                + "Diagnóstico fisioterapêutico: " + prontuario.getDiagnosticoFisioterapeutico() + "\n"
                + "Exame físico: " + prontuario.getExameFisico() + "\n"
                + "Objetivos: " + prontuario.getObjetivos() + "\n"
                + "Conduta: " + prontuario.getConduta();
    }

    @Override
    public List<Evolucao> listarEvolucoes() {
        return prontuario.listarEvolucoes();
    }

    @Override
    public void adicionarEvolucao(Evolucao evolucao) {
        prontuario.adicionarEvolucao(evolucao);
    }
}
