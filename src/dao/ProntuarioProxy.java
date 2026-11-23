package dao;

import model.Evolucao;
import model.Fisioterapeuta;
import model.Funcionario;

import java.util.List;

/**
 * PROXY (o "porteiro"): fica na frente do ProntuarioReal e só deixa passar
 * quem tem permissão. Regra atual: apenas o fisioterapeuta acessa o prontuário.
 * A secretária cuida da agenda e não vê os dados clínicos.
 */
public class ProntuarioProxy implements ProntuarioAcesso {

    private final ProntuarioReal real;
    private final Funcionario usuario;

    public ProntuarioProxy(ProntuarioReal real, Funcionario usuario) {
        this.real = real;
        this.usuario = usuario;
    }

    @Override
    public String verDadosClinicos() {
        verificarPermissao();
        return real.verDadosClinicos(); // delega ao objeto real
    }

    @Override
    public List<Evolucao> listarEvolucoes() {
        verificarPermissao();
        return real.listarEvolucoes();
    }

    @Override
    public void adicionarEvolucao(Evolucao evolucao) {
        verificarPermissao();
        real.adicionarEvolucao(evolucao);
    }

    /** Só o fisioterapeuta tem permissão. */
    private boolean temPermissao() {
        return usuario instanceof Fisioterapeuta;
    }

    private void verificarPermissao() {
        if (!temPermissao()) {
            throw new SecurityException("Acesso negado: " + usuario.getNome()
                    + " não tem permissão para acessar o prontuário.");
        }
    }
}
