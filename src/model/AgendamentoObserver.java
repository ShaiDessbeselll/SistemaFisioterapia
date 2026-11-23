package model;

/**
 * OBSERVER (interface): quem quiser ser avisado quando um agendamento mudar
 * implementa este método.
 */
public interface AgendamentoObserver {

    void atualizar(Agendamento agendamento);
}
