package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Agendamento de uma consulta: liga um paciente a um fisioterapeuta
 * e é registrado por uma secretária.
 *
 * OBSERVER: este é o objeto "observado". Quem quiser ser avisado das mudanças
 * se cadastra com adicionarObservador(); ao confirmar, cancelar ou remarcar,
 * todos os cadastrados são avisados por notificar().
 */
public class Agendamento {

    private int id;
    private LocalDate data;
    private LocalTime horario;
    private StatusAgendamento status;
    private Paciente paciente;
    private Fisioterapeuta fisioterapeuta;
    private Secretaria secretaria;
    private final List<AgendamentoObserver> observadores = new ArrayList<>();

    public Agendamento() {
    }

    public Agendamento(int id, LocalDate data, LocalTime horario,
                       Paciente paciente, Fisioterapeuta fisioterapeuta, Secretaria secretaria) {
        this.id = id;
        this.data = data;
        this.horario = horario;
        this.paciente = paciente;
        this.fisioterapeuta = fisioterapeuta;
        this.secretaria = secretaria;
        this.status = StatusAgendamento.AGENDADO; // todo agendamento começa como AGENDADO
    }

    public void confirmar() {
        if (status == StatusAgendamento.CANCELADO) {
            throw new IllegalStateException("Não é possível confirmar um agendamento cancelado.");
        }
        status = StatusAgendamento.CONFIRMADO;
        notificar();
    }

    public void cancelar() {
        if (status == StatusAgendamento.REALIZADO) {
            throw new IllegalStateException("Não é possível cancelar um atendimento já realizado.");
        }
        status = StatusAgendamento.CANCELADO;
        notificar();
    }

    public void remarcar(LocalDate novaData, LocalTime novoHorario) {
        if (status == StatusAgendamento.CANCELADO || status == StatusAgendamento.REALIZADO) {
            throw new IllegalStateException("Este agendamento não pode ser remarcado.");
        }
        this.data = novaData;
        this.horario = novoHorario;
        this.status = StatusAgendamento.REMARCADO;
        notificar();
    }

    /**
     * Corrige os dados do agendamento (paciente, fisioterapeuta, data e horário).
     * Diferente de remarcar(), o status não muda e ninguém é avisado: é uma correção.
     * Agendamento cancelado ou já realizado não pode ser editado.
     */
    public void editar(Paciente novoPaciente, Fisioterapeuta novoFisioterapeuta,
                       LocalDate novaData, LocalTime novoHorario) {
        if (status == StatusAgendamento.CANCELADO || status == StatusAgendamento.REALIZADO) {
            throw new IllegalStateException("Este agendamento não pode ser editado.");
        }
        this.paciente = novoPaciente;
        this.fisioterapeuta = novoFisioterapeuta;
        this.data = novaData;
        this.horario = novoHorario;
    }

    // ---------- OBSERVER ----------

    public void adicionarObservador(AgendamentoObserver observador) {
        observadores.add(observador);
    }

    public void removerObservador(AgendamentoObserver observador) {
        observadores.remove(observador);
    }

    /** Avisa todos os observadores cadastrados de que este agendamento mudou. */
    private void notificar() {
        for (AgendamentoObserver observador : observadores) {
            observador.atualizar(this);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Fisioterapeuta getFisioterapeuta() {
        return fisioterapeuta;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    @Override
    public String toString() {
        return data + " " + horario + " | " + paciente.getNome()
                + " com " + fisioterapeuta.getNome() + " [" + status + "]";
    }
}
