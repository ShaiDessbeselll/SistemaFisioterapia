package controller;

import dao.AuditoriaDAO;
import dao.FisioterapeutaDAO;
import dao.PacienteDAO;
import dao.ProntuarioDAO;
import dao.ProntuarioProxy;
import dao.ProntuarioReal;
import dao.SecretariaDAO;
import model.AcaoAuditoria;
import model.Evolucao;
import model.Fisioterapeuta;
import model.Funcionario;
import model.HistoricoProntuario;
import model.Paciente;
import model.Prontuario;
import model.ProntuarioMemento;
import model.RegistroAuditoria;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CONTROLLER do prontuário. É aqui que os três padrões trabalham juntos:
 *
 *  - BUILDER:  criar() monta o prontuário novo com Prontuario.Builder.
 *  - MEMENTO:  salvarAlteracoes() guarda uma "foto" antes de alterar, e desfazer() volta a ela.
 *  - PROXY:    abrir(), listarEvolucoes() e registrarEvolucao() passam pelo ProntuarioProxy,
 *              que só deixa o fisioterapeuta entrar.
 *
 * AUDITORIA: tudo que alguém faz no prontuário (abrir, criar, alterar, desfazer e mexer nas
 * evoluções) é gravado com o nome de quem fez, a data/hora e o que mudou.
 */
public class ProntuarioController {

    private final ProntuarioDAO prontuarioDAO = new ProntuarioDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final FisioterapeutaDAO fisioterapeutaDAO = new FisioterapeutaDAO();
    private final SecretariaDAO secretariaDAO = new SecretariaDAO();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    /** MEMENTO: versões anteriores do prontuário aberto (valem enquanto ele estiver aberto). */
    private HistoricoProntuario historico = new HistoricoProntuario();

    public List<Paciente> listarPacientes() {
        return pacienteDAO.listar();
    }

    /** Quem pode estar "usando o sistema": fisioterapeutas e secretárias (até existir o login). */
    public List<Funcionario> listarUsuarios() {
        List<Funcionario> usuarios = new ArrayList<>();
        usuarios.addAll(fisioterapeutaDAO.listar());
        usuarios.addAll(secretariaDAO.listar());
        return usuarios;
    }

    /**
     * Abre o prontuário do paciente, passando pelo PROXY.
     * Devolve null se o paciente ainda não tem prontuário.
     * Lança SecurityException se o usuário não tiver permissão (e a tentativa fica na auditoria).
     */
    public Prontuario abrir(Paciente paciente, Funcionario usuario) {
        exigirUsuario(usuario);
        historico = new HistoricoProntuario(); // cada abertura começa um histórico novo

        Prontuario prontuario = prontuarioDAO.buscarPorPaciente(paciente);
        if (prontuario == null) {
            // Só o fisioterapeuta pode abrir (criar) um prontuário
            if (!(usuario instanceof Fisioterapeuta)) {
                throw acessoNegado(usuario);
            }
            return null;
        }
        try {
            proxy(prontuario, usuario).verDadosClinicos(); // o Proxy barra quem não tem permissão
        } catch (SecurityException e) {
            registrarSemFalhar(prontuario, usuario, AcaoAuditoria.ACESSO_NEGADO,
                    "Tentativa de abrir o prontuário de " + paciente.getNome() + " bloqueada.");
            throw e;
        }
        auditoriaDAO.registrar(prontuario.getId(), usuario, AcaoAuditoria.VISUALIZOU,
                "Abriu o prontuário de " + paciente.getNome() + ".");
        return prontuario;
    }

    /** BUILDER: monta e grava um prontuário novo. */
    public Prontuario criar(Paciente paciente, Funcionario usuario, String anamnese,
                            String diagnosticoClinico, String diagnosticoFisioterapeutico,
                            String exameFisico, String objetivos, String conduta) {
        exigirUsuario(usuario);
        if (!(usuario instanceof Fisioterapeuta)) {
            throw acessoNegado(usuario);
        }
        Prontuario prontuario = new Prontuario.Builder(paciente)
                .anamnese(anamnese.trim())
                .diagnosticoClinico(diagnosticoClinico.trim())
                .diagnosticoFisioterapeutico(diagnosticoFisioterapeutico.trim())
                .exameFisico(exameFisico.trim())
                .objetivos(objetivos.trim())
                .conduta(conduta.trim())
                .build();
        try {
            prontuarioDAO.inserir(prontuario);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IllegalArgumentException("Este paciente já tem um prontuário.");
            }
            throw e;
        }
        paciente.setProntuario(prontuario);
        historico = new HistoricoProntuario();
        auditoriaDAO.registrar(prontuario.getId(), usuario, AcaoAuditoria.CRIOU,
                "Criou o prontuário de " + paciente.getNome() + ".");
        return prontuario;
    }

    /** MEMENTO: guarda uma foto do estado atual, aplica as alterações e grava no banco. */
    public void salvarAlteracoes(Prontuario prontuario, Funcionario usuario, String anamnese,
                                 String diagnosticoClinico, String diagnosticoFisioterapeutico,
                                 String exameFisico, String objetivos, String conduta) {
        exigirAcesso(prontuario, usuario);
        if (t(prontuario.getAnamnese()).equals(t(anamnese))
                && t(prontuario.getDiagnosticoClinico()).equals(t(diagnosticoClinico))
                && t(prontuario.getDiagnosticoFisioterapeutico()).equals(t(diagnosticoFisioterapeutico))
                && t(prontuario.getExameFisico()).equals(t(exameFisico))
                && t(prontuario.getObjetivos()).equals(t(objetivos))
                && t(prontuario.getConduta()).equals(t(conduta))) {
            throw new IllegalArgumentException("Nenhuma alteração para salvar.");
        }

        ProntuarioMemento antes = prontuario.salvarEstado();
        historico.guardar(antes); // foto ANTES de alterar

        prontuario.setAnamnese(anamnese.trim());
        prontuario.setDiagnosticoClinico(diagnosticoClinico.trim());
        prontuario.setDiagnosticoFisioterapeutico(diagnosticoFisioterapeutico.trim());
        prontuario.setExameFisico(exameFisico.trim());
        prontuario.setObjetivos(objetivos.trim());
        prontuario.setConduta(conduta.trim());
        try {
            prontuarioDAO.atualizar(prontuario);
        } catch (RuntimeException e) {
            prontuario.restaurarEstado(historico.desfazer()); // se o banco falhar, volta ao que era
            throw e;
        }
        auditoriaDAO.registrar(prontuario.getId(), usuario, AcaoAuditoria.ALTEROU,
                diferencas(antes, prontuario.salvarEstado()));
    }

    /** MEMENTO: volta o prontuário para a última foto guardada e grava no banco. */
    public void desfazer(Prontuario prontuario, Funcionario usuario) {
        exigirAcesso(prontuario, usuario);
        if (!historico.temVersoes()) {
            throw new IllegalArgumentException("Não há alterações para desfazer.");
        }
        ProntuarioMemento antes = prontuario.salvarEstado();
        ProntuarioMemento anterior = historico.desfazer();
        prontuario.restaurarEstado(anterior);
        prontuarioDAO.atualizar(prontuario);
        auditoriaDAO.registrar(prontuario.getId(), usuario, AcaoAuditoria.DESFEZ,
                diferencas(antes, anterior));
    }

    /** PROXY: lista as evoluções, ativas e inativas (só quem tem permissão). */
    public List<Evolucao> listarEvolucoes(Prontuario prontuario, Funcionario usuario) {
        exigirUsuario(usuario);
        return proxy(prontuario, usuario).listarEvolucoes();
    }

    /** PROXY: registra uma evolução (só o fisioterapeuta) e grava no banco. */
    public void registrarEvolucao(Prontuario prontuario, Funcionario usuario, String descricao) {
        exigirUsuario(usuario);
        descricao = descricao.trim();
        if (descricao.isEmpty()) {
            throw new IllegalArgumentException("Escreva a evolução do atendimento.");
        }
        Fisioterapeuta autor = usuario instanceof Fisioterapeuta ? (Fisioterapeuta) usuario : null;
        Evolucao evolucao = new Evolucao(0, LocalDate.now(), descricao, autor);

        proxy(prontuario, usuario).adicionarEvolucao(evolucao); // o Proxy barra a secretária
        prontuarioDAO.inserirEvolucao(prontuario, evolucao);
        auditoriaDAO.registrar(prontuario.getId(), usuario, AcaoAuditoria.EVOLUCAO_REGISTRADA,
                "Evolução nº " + evolucao.getId() + ": \"" + resumo(descricao) + "\"");
    }

    /** Muda o texto de uma evolução ativa. O texto antigo e o novo ficam na auditoria. */
    public void editarEvolucao(Prontuario prontuario, Funcionario usuario, Evolucao evolucao, String novaDescricao) {
        exigirAcesso(prontuario, usuario);
        novaDescricao = novaDescricao.trim();
        if (novaDescricao.isEmpty()) {
            throw new IllegalArgumentException("Escreva o texto da evolução.");
        }
        if (!evolucao.isAtiva()) {
            throw new IllegalArgumentException("Reative a evolução antes de editar.");
        }
        String antiga = evolucao.getDescricao();
        if (t(antiga).equals(novaDescricao)) {
            throw new IllegalArgumentException("Nenhuma alteração para salvar.");
        }
        evolucao.setDescricao(novaDescricao);
        try {
            prontuarioDAO.atualizarEvolucao(evolucao);
        } catch (RuntimeException e) {
            evolucao.setDescricao(antiga); // se o banco falhar, volta ao que era
            throw e;
        }
        auditoriaDAO.registrar(prontuario.getId(), usuario, AcaoAuditoria.EVOLUCAO_EDITADA,
                "Evolução nº " + evolucao.getId() + " (" + PacienteController.formatarData(evolucao.getData()) + ")\n"
                        + "Antes: \"" + resumo(antiga) + "\"\n"
                        + "Depois: \"" + resumo(novaDescricao) + "\"");
    }

    /** Inativa a evolução (ela não é apagada, só fica marcada). O motivo é obrigatório e vai para a auditoria. */
    public void inativarEvolucao(Prontuario prontuario, Funcionario usuario, Evolucao evolucao, String motivo) {
        exigirAcesso(prontuario, usuario);
        motivo = motivo == null ? "" : motivo.trim();
        if (motivo.isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo da inativação.");
        }
        if (!evolucao.isAtiva()) {
            throw new IllegalArgumentException("Esta evolução já está inativa.");
        }
        mudarSituacao(evolucao, false);
        auditoriaDAO.registrar(prontuario.getId(), usuario, AcaoAuditoria.EVOLUCAO_INATIVADA,
                "Evolução nº " + evolucao.getId() + " (" + PacienteController.formatarData(evolucao.getData()) + ")\n"
                        + "Texto: \"" + resumo(evolucao.getDescricao()) + "\"\n"
                        + "Motivo: " + motivo);
    }

    /** Volta uma evolução inativa para ativa. */
    public void reativarEvolucao(Prontuario prontuario, Funcionario usuario, Evolucao evolucao) {
        exigirAcesso(prontuario, usuario);
        if (evolucao.isAtiva()) {
            throw new IllegalArgumentException("Esta evolução já está ativa.");
        }
        mudarSituacao(evolucao, true);
        auditoriaDAO.registrar(prontuario.getId(), usuario, AcaoAuditoria.EVOLUCAO_REATIVADA,
                "Evolução nº " + evolucao.getId() + " (" + PacienteController.formatarData(evolucao.getData()) + ")\n"
                        + "Texto: \"" + resumo(evolucao.getDescricao()) + "\"");
    }

    /** Histórico de auditoria do prontuário (só o fisioterapeuta vê). */
    public List<RegistroAuditoria> listarAuditoria(Prontuario prontuario, Funcionario usuario) {
        exigirAcesso(prontuario, usuario);
        return auditoriaDAO.listarPorProntuario(prontuario.getId());
    }

    // ---------------------------------------------------------------- apoio

    private void mudarSituacao(Evolucao evolucao, boolean ativa) {
        boolean anterior = evolucao.isAtiva();
        evolucao.setAtiva(ativa);
        try {
            prontuarioDAO.atualizarEvolucao(evolucao);
        } catch (RuntimeException e) {
            evolucao.setAtiva(anterior); // se o banco falhar, volta ao que era
            throw e;
        }
    }

    private ProntuarioProxy proxy(Prontuario prontuario, Funcionario usuario) {
        return new ProntuarioProxy(new ProntuarioReal(prontuario), usuario);
    }

    /** Passa pelo PROXY só para barrar quem não tem permissão (lança SecurityException). */
    private void exigirAcesso(Prontuario prontuario, Funcionario usuario) {
        exigirUsuario(usuario);
        proxy(prontuario, usuario).listarEvolucoes();
    }

    /** Grava na auditoria sem deixar um erro daqui esconder o erro principal. */
    private void registrarSemFalhar(Prontuario prontuario, Funcionario usuario, AcaoAuditoria acao, String detalhes) {
        try {
            auditoriaDAO.registrar(prontuario.getId(), usuario, acao, detalhes);
        } catch (RuntimeException ignorado) {
            // a negativa de acesso continua valendo mesmo se a auditoria falhar
        }
    }

    private void exigirUsuario(Funcionario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Escolha quem está usando o sistema.");
        }
    }

    private SecurityException acessoNegado(Funcionario usuario) {
        return new SecurityException("Acesso negado: " + usuario.getNome()
                + " não tem permissão para acessar o prontuário.");
    }

    /** Lista, campo a campo, o que mudou entre duas fotos do prontuário ("Antes → Depois"). */
    private static String diferencas(ProntuarioMemento antes, ProntuarioMemento depois) {
        StringBuilder texto = new StringBuilder();
        comparar(texto, "Anamnese", antes.getAnamnese(), depois.getAnamnese());
        comparar(texto, "Diagnóstico clínico", antes.getDiagnosticoClinico(), depois.getDiagnosticoClinico());
        comparar(texto, "Diagnóstico fisioterapêutico",
                antes.getDiagnosticoFisioterapeutico(), depois.getDiagnosticoFisioterapeutico());
        comparar(texto, "Exame físico", antes.getExameFisico(), depois.getExameFisico());
        comparar(texto, "Objetivos", antes.getObjetivos(), depois.getObjetivos());
        comparar(texto, "Conduta", antes.getConduta(), depois.getConduta());
        return texto.toString().trim();
    }

    private static void comparar(StringBuilder texto, String campo, String antes, String depois) {
        if (!t(antes).equals(t(depois))) {
            texto.append(campo).append(":\n")
                    .append("   Antes: \"").append(resumo(antes)).append("\"\n")
                    .append("   Depois: \"").append(resumo(depois)).append("\"\n");
        }
    }

    /** Texto curto para a auditoria (corta textos muito grandes). */
    private static String resumo(String texto) {
        String limpo = t(texto).replaceAll("\\s+", " ");
        return limpo.length() > 300 ? limpo.substring(0, 300) + "…" : limpo;
    }

    /** Texto sem espaços nas pontas e sem nulo, para comparar. */
    private static String t(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
