package model;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * OBSERVER (concreto): avisa o fisioterapeuta quando um agendamento dele muda.
 */
public class NotificadorFisioterapeuta implements AgendamentoObserver {

    private final Fisioterapeuta fisioterapeuta;
    private final Consumer<String> saida;

    public NotificadorFisioterapeuta(Fisioterapeuta fisioterapeuta) {
        this(fisioterapeuta, System.out::println);
    }

    public NotificadorFisioterapeuta(Fisioterapeuta fisioterapeuta, Consumer<String> saida) {
        this.fisioterapeuta = fisioterapeuta;
        this.saida = saida;
    }

    @Override
    public void atualizar(Agendamento agendamento) {
        saida.accept("Aviso para " + fisioterapeuta.getNome() + ": a consulta de "
                + agendamento.getPaciente().getNome() + " em "
                + agendamento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " às " + agendamento.getHorario() + " está " + agendamento.getStatus() + ".");
    }
}
