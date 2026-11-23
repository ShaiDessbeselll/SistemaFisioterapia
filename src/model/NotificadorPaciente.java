package model;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * OBSERVER (concreto): avisa o paciente quando o agendamento muda.
 * A mensagem vai para onde for indicado em "saida" (por padrão, o terminal;
 * na interface gráfica, pode ser uma janela de aviso).
 */
public class NotificadorPaciente implements AgendamentoObserver {

    private final Paciente paciente;
    private final Consumer<String> saida;

    public NotificadorPaciente(Paciente paciente) {
        this(paciente, System.out::println);
    }

    public NotificadorPaciente(Paciente paciente, Consumer<String> saida) {
        this.paciente = paciente;
        this.saida = saida;
    }

    @Override
    public void atualizar(Agendamento agendamento) {
        saida.accept("Aviso para o paciente " + paciente.getNome() + ": sua consulta de "
                + agendamento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " às " + agendamento.getHorario() + " está " + agendamento.getStatus() + ".");
    }
}
