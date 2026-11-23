package dao;

import model.Evolucao;

import java.util.List;

/**
 * PROXY (interface): o contrato de acesso ao prontuário.
 * Tanto o objeto real quanto o proxy (o "porteiro") implementam a mesma interface,
 * então quem usa não precisa saber com qual dos dois está falando.
 */
public interface ProntuarioAcesso {

    String verDadosClinicos();

    List<Evolucao> listarEvolucoes();

    void adicionarEvolucao(Evolucao evolucao);
}
