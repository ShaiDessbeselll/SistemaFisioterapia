package model;

import java.time.LocalDate;

/**
 * Evolução: registro feito pelo fisioterapeuta a cada atendimento,
 * descrevendo como o paciente está respondendo ao tratamento.
 */
public class Evolucao {

    private int id;
    private LocalDate data;
    private String descricao;
    private Fisioterapeuta fisioterapeuta;
    private boolean ativa = true; // evolução inativada não é apagada: só fica marcada como inativa

    public Evolucao() {
    }

    public Evolucao(int id, LocalDate data, String descricao, Fisioterapeuta fisioterapeuta) {
        this.id = id;
        this.data = data;
        this.descricao = descricao;
        this.fisioterapeuta = fisioterapeuta;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public Fisioterapeuta getFisioterapeuta() {
        return fisioterapeuta;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    @Override
    public String toString() {
        return data + " - " + descricao + " (" + fisioterapeuta.getNome() + ")";
    }
}
