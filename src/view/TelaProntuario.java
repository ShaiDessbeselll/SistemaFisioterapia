package view;

import controller.PacienteController;
import controller.ProntuarioController;
import model.Evolucao;
import model.Funcionario;
import model.Paciente;
import model.Prontuario;
import util.Estilo;
import util.Sessao;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Tela do prontuário. Mostra os padrões de projeto em ação:
 *  - Builder: o botão "Criar prontuário" monta um prontuário novo com os campos preenchidos.
 *  - Memento: "Salvar alterações" guarda a versão anterior, e "Desfazer" volta a ela.
 *  - Proxy: o prontuário só abre para o fisioterapeuta; a secretária recebe "Acesso negado".
 *
 * Quem está usando o sistema vem do login (classe Sessao); não dá mais para trocar de usuário aqui.
 */
public class TelaProntuario extends JFrame {

    private final ProntuarioController controller = new ProntuarioController();

    private CampoBuscaPaciente campoBusca; // o paciente é achado digitando o nome ou o CPF

    private final JTextArea txtAnamnese = criarArea(3);
    private final JTextArea txtDiagClinico = criarArea(2);
    private final JTextArea txtDiagFisio = criarArea(2);
    private final JTextArea txtExameFisico = criarArea(3);
    private final JTextArea txtObjetivos = criarArea(2);
    private final JTextArea txtConduta = criarArea(3);
    private final JTextArea txtEvolucao = criarArea(4);

    private final JLabel lblStatus = new JLabel();
    private final JButton btnSalvar = Estilo.botaoPrimario("Salvar alterações");
    private final JButton btnDesfazer = new JButton("Desfazer");
    private final JButton btnRegistrar = Estilo.botaoPrimario("Registrar evolução");

    private final DefaultTableModel modeloEvolucoes = new DefaultTableModel(
            new Object[]{"Data", "Fisioterapeuta", "Evolução", "Situação"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabelaEvolucoes = new JTable(modeloEvolucoes);
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnInativar = new JButton("Inativar");
    private final JButton btnCancelarEdicao = new JButton("Cancelar edição");
    private final JButton btnHistorico = new JButton("Histórico do prontuário");
    private JLabel lblTituloEvolucao;

    /** Evoluções na mesma ordem das linhas da tabela. */
    private List<Evolucao> evolucoesNaTabela = new java.util.ArrayList<>();
    /** Evolução que está sendo editada no momento (null = escrevendo uma nova). */
    private Evolucao evolucaoEmEdicao = null;

    /** Prontuário aberto no momento (null = nenhum aberto, ou paciente ainda sem prontuário). */
    private Prontuario prontuarioAberto = null;
    private Paciente pacienteAtual = null;

    public TelaProntuario() {
        super("Prontuários");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(Estilo.criarCabecalho("Prontuários",
                "Dados clínicos e evolução de cada paciente"), BorderLayout.NORTH);
        norte.add(criarBarra(), BorderLayout.SOUTH);
        raiz.add(norte, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new GridLayout(1, 2, 20, 0));
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(0, 24, 24, 24));
        corpo.add(criarPainelDados());
        corpo.add(criarPainelEvolucoes());
        raiz.add(corpo, BorderLayout.CENTER);

        setContentPane(raiz);
        carregarPacientes();
        fechar();
    }

    /**
     * Abre a tela já mostrando o prontuário do paciente escolhido
     * (usado pelo botão "Ver prontuário" da tela de pacientes).
     * Chame depois de setVisible(true).
     */
    public void abrirPaciente(Paciente paciente) {
        campoBusca.escolher(paciente);
        abrir(paciente);
    }

    // ---------------------------------------------------------------- montagem da tela

    private JPanel criarBarra() {
        JPanel barra = new JPanel();
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBackground(Estilo.FUNDO);
        barra.setBorder(new EmptyBorder(14, 24, 10, 24));

        // linha de cima: voltar + quem está usando o sistema
        JPanel linhaTopo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaTopo.setOpaque(false);
        linhaTopo.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaTopo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JButton btnVoltar = new JButton("← Voltar ao menu");
        btnVoltar.addActionListener(e -> dispose()); // fecha esta tela; o menu principal já está aberto atrás
        linhaTopo.add(btnVoltar);
        linhaTopo.add(Box.createHorizontalStrut(16));
        linhaTopo.add(rotulo("Acessando como: " + Sessao.descricao()));
        linhaTopo.add(Box.createHorizontalStrut(16));
        btnHistorico.addActionListener(e -> verHistorico());
        btnHistorico.setEnabled(false);
        linhaTopo.add(btnHistorico);
        barra.add(linhaTopo);
        barra.add(Box.createVerticalStrut(10));

        // busca do paciente: escolheu na lista => abre o prontuário; digitou de novo => fecha o aberto
        campoBusca = new CampoBuscaPaciente("Paciente", this::abrir, this::fechar, this::abrirCadastro);
        campoBusca.setAlignmentX(Component.LEFT_ALIGNMENT);
        barra.add(campoBusca);
        return barra;
    }

    private JScrollPane criarPainelDados() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));

        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblStatus.setForeground(Estilo.PRIMARIA_ESCURA);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblStatus);
        painel.add(Box.createVerticalStrut(14));

        adicionarArea(painel, "Anamnese", txtAnamnese);
        adicionarArea(painel, "Diagnóstico clínico", txtDiagClinico);
        adicionarArea(painel, "Diagnóstico fisioterapêutico", txtDiagFisio);
        adicionarArea(painel, "Exame físico", txtExameFisico);
        adicionarArea(painel, "Objetivos", txtObjetivos);
        adicionarArea(painel, "Conduta", txtConduta);

        btnSalvar.addActionListener(e -> salvar());
        btnDesfazer.addActionListener(e -> desfazer());
        JPanel botoes = new JPanel(new GridLayout(1, 2, 8, 0));
        botoes.setOpaque(false);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        botoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        botoes.add(btnSalvar);
        botoes.add(btnDesfazer);
        painel.add(botoes);

        JScrollPane rolagem = new JScrollPane(painel);
        rolagem.setBorder(null);
        rolagem.getViewport().setBackground(Color.WHITE);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        return rolagem;
    }

    private JPanel criarPainelEvolucoes() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setOpaque(false);

        tabelaEvolucoes.setRowHeight(30);
        tabelaEvolucoes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaEvolucoes.getTableHeader().setReorderingAllowed(false);
        tabelaEvolucoes.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabelaEvolucoes.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabelaEvolucoes.getColumnModel().getColumn(2).setPreferredWidth(300);
        tabelaEvolucoes.getColumnModel().getColumn(3).setPreferredWidth(70);
        tabelaEvolucoes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaEvolucoes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int linha, int coluna) {
                Component c = super.getTableCellRendererComponent(t, valor, sel, foco, linha, coluna);
                boolean inativa = linha < evolucoesNaTabela.size() && !evolucoesNaTabela.get(linha).isAtiva();
                Font base = t.getFont();
                if (inativa) {
                    c.setFont(base.deriveFont(Map.of(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON)));
                    if (!sel) {
                        c.setForeground(Estilo.TEXTO_SUAVE);
                    }
                } else {
                    c.setFont(base);
                    if (!sel) {
                        c.setForeground(Estilo.TEXTO);
                    }
                }
                return c;
            }
        });
        tabelaEvolucoes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarBotoesEvolucao();
            }
        });
        tabelaEvolucoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    iniciarEdicao();
                }
            }
        });
        JScrollPane rolagem = new JScrollPane(tabelaEvolucoes);
        rolagem.getViewport().setBackground(Color.WHITE);

        btnEditar.addActionListener(e -> iniciarEdicao());
        btnInativar.addActionListener(e -> inativarOuReativar());
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setOpaque(false);
        acoes.add(btnEditar);
        acoes.add(btnInativar);
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.add(rolagem, BorderLayout.CENTER);
        centro.add(acoes, BorderLayout.SOUTH);
        painel.add(centro, BorderLayout.CENTER);

        JPanel nova = new JPanel();
        nova.setLayout(new BoxLayout(nova, BoxLayout.Y_AXIS));
        nova.setBackground(Color.WHITE);
        nova.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Nova evolução");
        lblTituloEvolucao = titulo;
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titulo.setForeground(Estilo.PRIMARIA_ESCURA);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        nova.add(titulo);
        nova.add(Box.createVerticalStrut(8));

        JScrollPane areaRolagem = new JScrollPane(txtEvolucao);
        areaRolagem.setAlignmentX(Component.LEFT_ALIGNMENT);
        areaRolagem.setPreferredSize(new Dimension(300, 100));
        areaRolagem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        nova.add(areaRolagem);
        nova.add(Box.createVerticalStrut(10));

        btnRegistrar.addActionListener(e -> registrarEvolucao());
        btnRegistrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        nova.add(btnRegistrar);
        btnCancelarEdicao.addActionListener(e -> cancelarEdicao());
        btnCancelarEdicao.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancelarEdicao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnCancelarEdicao.setVisible(false);
        nova.add(Box.createVerticalStrut(6));
        nova.add(btnCancelarEdicao);

        painel.add(nova, BorderLayout.SOUTH);
        return painel;
    }

    private JLabel rotulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Estilo.TEXTO_SUAVE);
        return label;
    }

    private JTextArea criarArea(int linhas) {
        JTextArea area = new JTextArea(linhas, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setMargin(new java.awt.Insets(6, 8, 6, 8));
        // A tecla Tab passa para o próximo campo (em vez de escrever um espaço de tabulação)
        area.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        area.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        return area;
    }

    private void adicionarArea(JPanel painel, String titulo, JTextArea area) {
        JLabel label = rotulo(titulo);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane rolagem = new JScrollPane(area);
        rolagem.setAlignmentX(Component.LEFT_ALIGNMENT);
        rolagem.setPreferredSize(new Dimension(300, area.getRows() * 22 + 16));
        rolagem.setMaximumSize(new Dimension(Integer.MAX_VALUE, area.getRows() * 22 + 16));
        painel.add(label);
        painel.add(Box.createVerticalStrut(4));
        painel.add(rolagem);
        painel.add(Box.createVerticalStrut(12));
    }

    // ---------------------------------------------------------------- estado da tela

    private void carregarPacientes() {
        try {
            campoBusca.setPacientes(controller.listarPacientes());
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    /** Abre a tela de cadastro (já com o nome digitado) e, ao fechar, atualiza a busca. */
    private void abrirCadastro() {
        CadastroPaciente tela = new CadastroPaciente(campoBusca.getTexto());
        tela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                carregarPacientes();
                campoBusca.escolherSeUnico();
            }
        });
        tela.setVisible(true);
    }

    /** Deixa a tela sem nenhum prontuário aberto. */
    private void fechar() {
        prontuarioAberto = null;
        pacienteAtual = null;
        limparCampos();
        modeloEvolucoes.setRowCount(0);
        evolucoesNaTabela = new java.util.ArrayList<>();
        sairDaEdicao();
        habilitar(false);
        btnSalvar.setText("Salvar alterações");
        lblStatus.setText("Digite o nome do paciente acima e escolha na lista para abrir o prontuário");
    }

    private void habilitar(boolean ligado) {
        for (JTextArea area : new JTextArea[]{txtAnamnese, txtDiagClinico, txtDiagFisio,
                txtExameFisico, txtObjetivos, txtConduta}) {
            area.setEnabled(ligado);
        }
        btnSalvar.setEnabled(ligado);
        btnDesfazer.setEnabled(ligado && prontuarioAberto != null);
        txtEvolucao.setEnabled(ligado && prontuarioAberto != null);
        btnRegistrar.setEnabled(ligado && prontuarioAberto != null);
        btnHistorico.setEnabled(ligado && prontuarioAberto != null);
        atualizarBotoesEvolucao();
    }

    private void limparCampos() {
        txtAnamnese.setText("");
        txtDiagClinico.setText("");
        txtDiagFisio.setText("");
        txtExameFisico.setText("");
        txtObjetivos.setText("");
        txtConduta.setText("");
        txtEvolucao.setText("");
    }

    private void preencher(Prontuario p) {
        txtAnamnese.setText(nulo(p.getAnamnese()));
        txtDiagClinico.setText(nulo(p.getDiagnosticoClinico()));
        txtDiagFisio.setText(nulo(p.getDiagnosticoFisioterapeutico()));
        txtExameFisico.setText(nulo(p.getExameFisico()));
        txtObjetivos.setText(nulo(p.getObjetivos()));
        txtConduta.setText(nulo(p.getConduta()));
    }

    private String nulo(String texto) {
        return texto == null ? "" : texto;
    }

    private void mostrarProntuarioAberto() {
        preencher(prontuarioAberto);
        btnSalvar.setText("Salvar alterações");
        lblStatus.setText("Prontuário nº " + prontuarioAberto.getId() + " de "
                + pacienteAtual.getNome() + " (aberto em "
                + PacienteController.formatarData(prontuarioAberto.getDataAbertura()) + ")");
        habilitar(true);
        carregarEvolucoes();
    }

    private void carregarEvolucoes() {
        modeloEvolucoes.setRowCount(0);
        evolucoesNaTabela = new java.util.ArrayList<>(
                controller.listarEvolucoes(prontuarioAberto, usuarioLogado()));
        for (Evolucao e : evolucoesNaTabela) {
            modeloEvolucoes.addRow(new Object[]{
                    PacienteController.formatarData(e.getData()),
                    e.getFisioterapeuta() == null ? "" : e.getFisioterapeuta().getNome(),
                    e.getDescricao(),
                    e.isAtiva() ? "Ativa" : "Inativa"});
        }
        atualizarBotoesEvolucao();
    }

    private Evolucao evolucaoSelecionada() {
        int linha = tabelaEvolucoes.getSelectedRow();
        return linha >= 0 && linha < evolucoesNaTabela.size() ? evolucoesNaTabela.get(linha) : null;
    }

    private void atualizarBotoesEvolucao() {
        Evolucao sel = evolucaoSelecionada();
        boolean aberto = prontuarioAberto != null;
        btnEditar.setEnabled(aberto && sel != null && sel.isAtiva() && evolucaoEmEdicao == null);
        btnInativar.setEnabled(aberto && sel != null && evolucaoEmEdicao == null);
        btnInativar.setText(sel != null && !sel.isAtiva() ? "Reativar" : "Inativar");
    }

    // ---------------------------------------------------------------- ações

    private void abrir(Paciente paciente) {
        Funcionario usuario = usuarioLogado();
        if (paciente == null) {
            avisar("Escolha o paciente.");
            return;
        }
        try {
            Prontuario prontuario = controller.abrir(paciente, usuario);
            pacienteAtual = paciente;
            prontuarioAberto = prontuario;
            if (prontuario == null) {
                limparCampos();
                modeloEvolucoes.setRowCount(0);
                btnSalvar.setText("Criar prontuário");
                lblStatus.setText(paciente.getNome() + " ainda não tem prontuário. Preencha e clique em Criar.");
                habilitar(true);
            } else {
                mostrarProntuarioAberto();
            }
        } catch (SecurityException e) {
            fechar();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso negado", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    private void salvar() {
        Funcionario usuario = usuarioLogado();
        try {
            if (prontuarioAberto == null) {
                // BUILDER: cria o prontuário novo
                prontuarioAberto = controller.criar(pacienteAtual, usuario, txtAnamnese.getText(),
                        txtDiagClinico.getText(), txtDiagFisio.getText(), txtExameFisico.getText(),
                        txtObjetivos.getText(), txtConduta.getText());
                mostrarProntuarioAberto();
                JOptionPane.showMessageDialog(this, "Prontuário criado com sucesso.",
                        "Prontuários", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // MEMENTO: guarda a versão anterior e salva a nova
                controller.salvarAlteracoes(prontuarioAberto, usuario, txtAnamnese.getText(),
                        txtDiagClinico.getText(), txtDiagFisio.getText(), txtExameFisico.getText(),
                        txtObjetivos.getText(), txtConduta.getText());
                JOptionPane.showMessageDialog(this,
                        "Alterações salvas. Use o botão Desfazer para voltar à versão anterior.",
                        "Prontuários", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso negado", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    private void desfazer() {
        try {
            controller.desfazer(prontuarioAberto, usuarioLogado()); // MEMENTO: volta à versão anterior
            preencher(prontuarioAberto);
            JOptionPane.showMessageDialog(this, "Versão anterior restaurada.",
                    "Prontuários", JOptionPane.INFORMATION_MESSAGE);
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso negado", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    /** Botão do formulário: registra uma evolução nova ou salva a edição de uma existente. */
    private void registrarEvolucao() {
        try {
            if (evolucaoEmEdicao != null) {
                controller.editarEvolucao(prontuarioAberto, usuarioLogado(), evolucaoEmEdicao, txtEvolucao.getText());
                sairDaEdicao();
            } else {
                controller.registrarEvolucao(prontuarioAberto,
                        usuarioLogado(), txtEvolucao.getText());
                txtEvolucao.setText("");
            }
            carregarEvolucoes();
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso negado", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    /** Joga o texto da evolução selecionada no formulário para editar. */
    private void iniciarEdicao() {
        Evolucao sel = evolucaoSelecionada();
        if (prontuarioAberto == null || sel == null || evolucaoEmEdicao != null) {
            return;
        }
        if (!sel.isAtiva()) {
            avisar("Reative a evolução antes de editar.");
            return;
        }
        evolucaoEmEdicao = sel;
        txtEvolucao.setText(nulo(sel.getDescricao()));
        lblTituloEvolucao.setText("Editando evolução de " + PacienteController.formatarData(sel.getData()));
        btnRegistrar.setText("Salvar edição");
        btnCancelarEdicao.setVisible(true);
        atualizarBotoesEvolucao();
        txtEvolucao.requestFocusInWindow();
    }

    private void cancelarEdicao() {
        sairDaEdicao();
        txtEvolucao.setText("");
    }

    /** Volta o formulário para o modo "Nova evolução". */
    private void sairDaEdicao() {
        evolucaoEmEdicao = null;
        lblTituloEvolucao.setText("Nova evolução");
        btnRegistrar.setText("Registrar evolução");
        btnCancelarEdicao.setVisible(false);
        atualizarBotoesEvolucao();
    }

    private void inativarOuReativar() {
        Evolucao sel = evolucaoSelecionada();
        if (sel == null) {
            return;
        }
        try {
            if (sel.isAtiva()) {
                String motivo = JOptionPane.showInputDialog(this,
                        "Por que esta evolução será inativada?\n(Ela não é apagada; o motivo fica no histórico.)",
                        "Inativar evolução", JOptionPane.QUESTION_MESSAGE);
                if (motivo == null) {
                    return; // cancelou
                }
                controller.inativarEvolucao(prontuarioAberto, usuarioLogado(), sel, motivo);
            } else {
                controller.reativarEvolucao(prontuarioAberto, usuarioLogado(), sel);
            }
            carregarEvolucoes();
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso negado", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    private void verHistorico() {
        try {
            new TelaAuditoria(this, lblStatus.getText(),
                    controller.listarAuditoria(prontuarioAberto, usuarioLogado())).setVisible(true);
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso negado", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    /** Quem fez login. Pode ser null se a tela for aberta sem passar pelo login. */
    private Funcionario usuarioLogado() {
        return Sessao.getUsuario();
    }

    private void avisar(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarErroBanco(RuntimeException e) {
        JOptionPane.showMessageDialog(this,
                "Não foi possível acessar o banco de dados.\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
