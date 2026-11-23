package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Secretaria: funcionária responsável pelos agendamentos da clínica.
 */
public class Secretaria extends Funcionario {

    private String turno;

    public Secretaria() {
    }

    public Secretaria(int id, String nome, String cpf, String telefone,
                      String matricula, LocalDate dataAdmissao, String turno) {
        super(id, nome, cpf, telefone, matricula, dataAdmissao);
        this.turno = turno;
    }

    /** Cria um novo agendamento (status AGENDADO) registrado por esta secretária. */
    public Agendamento criarAgendamento(Paciente paciente, Fisioterapeuta fisioterapeuta,
                                        LocalDate data, LocalTime horario) {
        return new Agendamento(0, data, horario, paciente, fisioterapeuta, this);
    }

    public void confirmarAgendamento(Agendamento agendamento) {
        agendamento.confirmar();
    }

    public void cancelarAgendamento(Agendamento agendamento) {
        agendamento.cancelar();
    }

    public void remarcarAgendamento(Agendamento agendamento, LocalDate novaData, LocalTime novoHorario) {
        agendamento.remarcar(novaData, novoHorario);
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }
}
