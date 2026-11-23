package model;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * MEMENTO (zelador): guarda as "fotos" (ProntuarioMemento) em uma pilha.
 * A última foto guardada é a primeira a voltar ao desfazer.
 * Ele não sabe o que há dentro das fotos: só as guarda e as devolve.
 */
public class HistoricoProntuario {

    private final Deque<ProntuarioMemento> versoes = new ArrayDeque<>();

    /** Guarda uma nova versão no topo da pilha. */
    public void guardar(ProntuarioMemento memento) {
        versoes.push(memento);
    }

    /** Tira e devolve a última versão guardada (ou null, se não houver nenhuma). */
    public ProntuarioMemento desfazer() {
        return versoes.isEmpty() ? null : versoes.pop();
    }

    public boolean temVersoes() {
        return !versoes.isEmpty();
    }
}
