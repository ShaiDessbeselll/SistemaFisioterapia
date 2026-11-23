package model;

/**
 * Tipos de ação que ficam registrados na auditoria do prontuário.
 * O nome (VISUALIZOU, ALTEROU...) é o que vai para o banco; o rótulo é o que aparece na tela.
 */
public enum AcaoAuditoria {
    VISUALIZOU("Visualizou o prontuário"),
    ACESSO_NEGADO("Tentativa de acesso negada"),
    CRIOU("Criou o prontuário"),
    ALTEROU("Alterou o prontuário"),
    DESFEZ("Desfez uma alteração"),
    EVOLUCAO_REGISTRADA("Registrou uma evolução"),
    EVOLUCAO_EDITADA("Editou uma evolução"),
    EVOLUCAO_INATIVADA("Inativou uma evolução"),
    EVOLUCAO_REATIVADA("Reativou uma evolução");

    private final String rotulo;

    AcaoAuditoria(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
