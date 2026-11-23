package model;

import java.time.LocalDateTime;

/**
 * Uma linha da auditoria do prontuário: QUEM fez, QUANDO fez e O QUE fez.
 * É somente leitura: depois de gravado, o sistema não altera nem apaga um registro.
 */
public class RegistroAuditoria {

    private final int id;
    private final LocalDateTime dataHora;
    private final String usuario;   // nome de quem fez, como estava na hora (ex.: "Ana Paula (Fisioterapeuta)")
    private final String acao;      // nome da AcaoAuditoria
    private final String detalhes;  // o que mudou (valor antigo → valor novo)

    public RegistroAuditoria(int id, LocalDateTime dataHora, String usuario, String acao, String detalhes) {
        this.id = id;
        this.dataHora = dataHora;
        this.usuario = usuario;
        this.acao = acao;
        this.detalhes = detalhes;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAcao() {
        return acao;
    }

    public String getDetalhes() {
        return detalhes == null ? "" : detalhes;
    }

    /** Nome da ação para mostrar na tela (se for um tipo desconhecido, mostra o que está no banco). */
    public String getAcaoLegivel() {
        try {
            return AcaoAuditoria.valueOf(acao).getRotulo();
        } catch (IllegalArgumentException e) {
            return acao;
        }
    }
}
