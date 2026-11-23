package controller;

import dao.AgendamentoDAO;
import dao.FisioterapeutaDAO;
import dao.PacienteDAO;
import dao.SecretariaDAO;
import model.Agendamento;
import model.AgendamentoObserver;
import model.Fisioterapeuta;
import model.NotificadorFisioterapeuta;
import model.NotificadorPaciente;
import model.Paciente;
import model.Secretaria;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * CONTROLLER dos agendamentos: valida o que foi digitado, confere conflitos de horário
 * e chama o model e o DAO.
 *
 * OBSERVER: ao confirmar, remarcar ou cancelar, os notificadores (do paciente e do
 * fisioterapeuta) são ligados ao agendamento. Os avisos que eles geram são devolvidos
 * à tela, que os exibe.
 */
public class AgendamentoController {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_HORARIO =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final FisioterapeutaDAO fisioterapeutaDAO = new FisioterapeutaDAO();
    private final SecretariaDAO secretariaDAO = new SecretariaDAO();

    /** Mensagens geradas pelos observers durante a última ação. */
    private final List<String> avisos = new ArrayList<>();

    public List<Agendamento> listar() {
        return agendamentoDAO.listar();
    }

    public List<Paciente> listarPacientes() {
        return pacienteDAO.listar();
    }

    public List<Fisioterapeuta> listarFisioterapeutas() {
        return fisioterapeutaDAO.listar();
    }

    /** Cria um agendamento novo (a secretária o registra) e grava no banco. */
    public void agendar(Paciente paciente, Fisioterapeuta fisioterapeuta, String data, String horario) {
        if (paciente == null) {
            throw new IllegalArgumentException("Escolha o paciente.");
        }
        if (fisioterapeuta == null) {
            throw new IllegalArgumentException("Escolha o fisioterapeuta.");
        }
        LocalDate dia = converterData(data);
        LocalTime hora = converterHorario(horario);
        verificarConflito(fisioterapeuta, dia, hora, 0);

        Secretaria secretaria = secretariaAtual();
        Agendamento agendamento = secretaria.criarAgendamento(paciente, fisioterapeuta, dia, hora);
        try {
            agendamentoDAO.inserir(agendamento);
        } catch (RuntimeException e) {
            throw traduzir(e);
        }
    }

    /**
     * Corrige um agendamento já existente (paciente, fisioterapeuta, data e horário)
     * e grava no banco. Não muda o status nem envia avisos.
     */
    public void editar(Agendamento agendamento, Paciente paciente, Fisioterapeuta fisioterapeuta,
                       String data, String horario) {
        if (paciente == null) {
            throw new IllegalArgumentException("Escolha o paciente.");
        }
        if (fisioterapeuta == null) {
            throw new IllegalArgumentException("Escolha o fisioterapeuta.");
        }
        LocalDate dia = converterDataEdicao(data, agendamento.getData());
        LocalTime hora = converterHorario(horario);
        verificarConflito(fisioterapeuta, dia, hora, agendamento.getId());
        try {
            agendamento.editar(paciente, fisioterapeuta, dia, hora); // regra do model
            agendamentoDAO.atualizar(agendamento);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (RuntimeException e) {
            throw traduzir(e);
        }
    }

    /** Exclui o agendamento de vez (some do banco). */
    public void excluir(Agendamento agendamento) {
        agendamentoDAO.excluir(agendamento.getId());
    }

    /** Confirma o agendamento e devolve os avisos enviados aos envolvidos. */
    public List<String> confirmar(Agendamento agendamento) {
        Secretaria secretaria = secretariaAtual();
        return executar(agendamento, () -> secretaria.confirmarAgendamento(agendamento));
    }

    /** Cancela o agendamento e devolve os avisos enviados aos envolvidos. */
    public List<String> cancelar(Agendamento agendamento) {
        Secretaria secretaria = secretariaAtual();
        return executar(agendamento, () -> secretaria.cancelarAgendamento(agendamento));
    }

    /** Remarca para nova data e horário e devolve os avisos enviados aos envolvidos. */
    public List<String> remarcar(Agendamento agendamento, String data, String horario) {
        LocalDate dia = converterData(data);
        LocalTime hora = converterHorario(horario);
        verificarConflito(agendamento.getFisioterapeuta(), dia, hora, agendamento.getId());

        Secretaria secretaria = secretariaAtual();
        return executar(agendamento, () -> secretaria.remarcarAgendamento(agendamento, dia, hora));
    }

    // ---------------------------------------------------------------- apoio

    /**
     * Liga os observers ao agendamento, executa a ação (que muda o status e notifica),
     * grava no banco e desliga os observers. Devolve as mensagens geradas.
     */
    private List<String> executar(Agendamento agendamento, Runnable acao) {
        avisos.clear();
        AgendamentoObserver avisoPaciente = new NotificadorPaciente(agendamento.getPaciente(), avisos::add);
        AgendamentoObserver avisoFisio =
                new NotificadorFisioterapeuta(agendamento.getFisioterapeuta(), avisos::add);
        agendamento.adicionarObservador(avisoPaciente);
        agendamento.adicionarObservador(avisoFisio);
        try {
            acao.run();
            agendamentoDAO.atualizar(agendamento);
        } catch (IllegalStateException e) {
            // regra do model (por exemplo, confirmar um agendamento cancelado)
            throw new IllegalArgumentException(e.getMessage());
        } catch (RuntimeException e) {
            throw traduzir(e);
        } finally {
            agendamento.removerObservador(avisoPaciente);
            agendamento.removerObservador(avisoFisio);
        }
        return new ArrayList<>(avisos);
    }

    /** Uma secretária só pode agendar; sem login ainda, usa a primeira cadastrada no banco. */
    private Secretaria secretariaAtual() {
        List<Secretaria> secretarias = secretariaDAO.listar();
        if (secretarias.isEmpty()) {
            throw new IllegalArgumentException("Não há secretária cadastrada no sistema.");
        }
        return secretarias.get(0);
    }

    /** Impede dois agendamentos do mesmo fisioterapeuta no mesmo dia e horário. */
    private void verificarConflito(Fisioterapeuta fisioterapeuta, LocalDate dia, LocalTime hora, int idIgnorado) {
        for (Agendamento a : agendamentoDAO.listar()) {
            if (a.getId() != idIgnorado
                    && a.getFisioterapeuta().getId() == fisioterapeuta.getId()
                    && a.getData().equals(dia)
                    && a.getHorario().equals(hora)) {
                throw new IllegalArgumentException(
                        "Este fisioterapeuta já tem uma consulta nesse dia e horário.");
            }
        }
    }

    private RuntimeException traduzir(RuntimeException e) {
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
            return new IllegalArgumentException("Este fisioterapeuta já tem uma consulta nesse dia e horário.");
        }
        return e;
    }

    /** Converte "dd/mm/aaaa" em data, sem aceitar datas passadas. */
    static LocalDate converterData(String texto) {
        try {
            LocalDate data = LocalDate.parse(texto.trim(), FORMATO_DATA);
            if (data.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("A data não pode estar no passado.");
            }
            return data;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use o formato dd/mm/aaaa.");
        }
    }

    /**
     * Converte "dd/mm/aaaa" em data para a EDIÇÃO: só recusa data passada se ela for
     * diferente da data que o agendamento já tinha (assim dá para corrigir o paciente
     * de um agendamento antigo sem mexer na data).
     */
    static LocalDate converterDataEdicao(String texto, LocalDate dataAtual) {
        try {
            LocalDate data = LocalDate.parse(texto.trim(), FORMATO_DATA);
            if (data.isBefore(LocalDate.now()) && !data.equals(dataAtual)) {
                throw new IllegalArgumentException("A data não pode estar no passado.");
            }
            return data;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use o formato dd/mm/aaaa.");
        }
    }

    /** Converte "hh:mm" em horário (exemplo: 14:30). */
    static LocalTime converterHorario(String texto) {
        try {
            return LocalTime.parse(texto.trim(), FORMATO_HORARIO);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Horário inválido. Use o formato hh:mm (exemplo: 14:30).");
        }
    }
}
