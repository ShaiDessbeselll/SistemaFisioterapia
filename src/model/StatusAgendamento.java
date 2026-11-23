package model;

/**
 * Possíveis estados de um agendamento.
 * Os nomes são iguais aos valores do ENUM da coluna "status" no banco (banco.sql).
 */
public enum StatusAgendamento {
    AGENDADO,
    CONFIRMADO,
    CANCELADO,
    REMARCADO,
    REALIZADO
}
