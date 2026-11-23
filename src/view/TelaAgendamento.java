package view;

import controller.AgendamentoController;
import controller.PacienteController;
import model.Agendamento;
import model.Fisioterapeuta;
import model.Paciente;
import model.Pessoa;
import model.StatusAgendamento;
import util.Estilo;
import util.Mascara;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela de agendamentos: o formulário cria um agendamento novo e, ao lado, o calendário do mês
 * mostra cada agendamento em uma caixinha. Com o botão direito na caixinha aparece o menu:
 * Editar, Confirmar, Remarcar, Cancelar e Excluir.
 * Os avisos enviados ao paciente e ao fisioterapeuta (padrão Observer) aparecem em uma janela.
 *
 * Editar leva os dados da caixinha para o formulário; o botão "Agendar" vira "Salvar".
 *
 * O paciente é escolhido digitando o nome (ou CPF): a lista de resultados aparece embaixo do
 * campo e, ao escolher, a ficha do paciente é mostrada. Se ele não existir, o botão
 * "Cadastrar paciente" abre a tela de cadastro.
 */
public class TelaAgendamento extends JFrame {

    private static final DateTimeFormatter HORARIO = DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoController controller = new AgendamentoController();

    // --- escolha do paciente por busca
    private final JTextField txtBuscaPaciente = new JTextField();
    private final DefaultListModel<Paciente> modeloResultados = new DefaultListModel<>();
    private final JList<Paciente> listaResultados = new JList<>(modeloResultados);
    private final JScrollPane rolagemResultados = new JScrollPane(listaResultados);
    private final JPanel painelNaoEncontrado = new JPanel();
    private final JLabel lblFicha = new JLabel();
    private JPanel painelFormulario;
    private List<Paciente> todosPacientes = new ArrayList<>();
    private Paciente pacienteEscolhido = null;
    private boolean atualizandoBusca = false; // true quando o texto do campo é mudado pelo programa

    private final JComboBox<Fisioterapeuta> cmbFisioterapeuta = new JComboBox<>();
    private final JTextField txtData = new JTextField();
    private final JTextField txtHorario = new JTextField();
    private final JLabel lblModo = new JLabel("Novo agendamento");

    private final JButton btnSalvar = Estilo.botaoPrimario("Agendar");
    private final JButton btnLimpar = new JButton("Limpar");

    private PainelCalendario calendario;
    /** Agendamento que está sendo editado no formulário (null = formulário está criando um novo). */
    private Agendamento emEdicao = null;

    public TelaAgendamento() {
        super("Agendamentos");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1280, tela.width - 40), Math.min(860, tela.height - 80));
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(Estilo.criarCabecalho("Agendamentos",
                "Marque, confirme, remarque e cancele as consultas"), BorderLayout.NORTH);
        norte.add(criarBarraVoltar(), BorderLayout.SOUTH);
        raiz.add(norte, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(20, 0));
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(8, 24, 24, 24));
        corpo.add(criarFormulario(), BorderLayout.WEST);
        calendario = new PainelCalendario(new PainelCalendario.Ouvinte() {
            @Override
            public void diaClicado(LocalDate dia) {
                aoClicarDia(dia);
            }

            @Override
            public JPopupMenu menuDe(Agendamento agendamento) {
                return criarMenu(agendamento);
            }

            @Override
            public void duploClique(Agendamento agendamento) {
                if (podeEditar(agendamento)) {
                    entrarEdicao(agendamento);
                }
            }
        });
        corpo.add(calendario, BorderLayout.CENTER);
        raiz.add(corpo, BorderLayout.CENTER);

        setContentPane(raiz);
        carregarCombos();
        carregarAgenda();
        atualizarFicha();
    }

    // ---------------------------------------------------------------- montagem da tela

    /** Faixa logo abaixo do cabeçalho, com o botão que volta para a tela inicial. */
    private JPanel criarBarraVoltar() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        barra.setBackground(Estilo.FUNDO);
        barra.setBorder(new EmptyBorder(4, 20, 0, 20));

        JButton btnVoltar = new JButton("← Voltar ao menu");
        btnVoltar.addActionListener(e -> dispose()); // fecha esta tela; o menu principal já está aberto atrás
        barra.add(btnVoltar);
        return barra;
    }

    private JScrollPane criarFormulario() {
        JPanel painel = new PainelRolavel();
        painelFormulario = painel;
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));

        lblModo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblModo.setForeground(Estilo.PRIMARIA_ESCURA);
        lblModo.setAlignmentX(LEFT_ALIGNMENT);
        painel.add(lblModo);
        painel.add(Box.createVerticalStrut(14));

        txtData.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtHorario.putClientProperty("JTextField.placeholderText", "hh:mm");
        Mascara.data(txtData);
        Mascara.horario(txtHorario);

        adicionarBuscaPaciente(painel);
        adicionarCampo(painel, "Fisioterapeuta *", cmbFisioterapeuta);
        adicionarCampo(painel, "Data *", txtData);
        adicionarCampo(painel, "Horário *", txtHorario);

        btnSalvar.addActionListener(e -> agendarOuSalvar());
        btnLimpar.addActionListener(e -> limpar());
        JPanel linhaNovo = new JPanel(new GridLayout(1, 2, 8, 0));
        linhaNovo.setOpaque(false);
        linhaNovo.setAlignmentX(LEFT_ALIGNMENT);
        linhaNovo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        linhaNovo.add(btnSalvar);
        linhaNovo.add(btnLimpar);
        painel.add(linhaNovo);

        painel.add(Box.createVerticalGlue());

        JScrollPane rolagem = new JScrollPane(painel);
        rolagem.setBorder(null);
        rolagem.getViewport().setBackground(Color.WHITE);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        rolagem.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rolagem.setPreferredSize(new Dimension(400, 0));
        return rolagem;
    }

    /**
     * Campo de busca do paciente: digitou, a lista de resultados aparece embaixo.
     * Escolhendo um da lista, a ficha dele é mostrada. Se não achar ninguém, aparece o
     * botão para cadastrar.
     */
    private void adicionarBuscaPaciente(JPanel painel) {
        JLabel label = new JLabel("Paciente *");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Estilo.TEXTO_SUAVE);
        label.setAlignmentX(LEFT_ALIGNMENT);
        painel.add(label);
        painel.add(Box.createVerticalStrut(4));

        txtBuscaPaciente.putClientProperty("JTextField.placeholderText", "Digite o nome ou o CPF");
        txtBuscaPaciente.setAlignmentX(LEFT_ALIGNMENT);
        txtBuscaPaciente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtBuscaPaciente.setPreferredSize(new Dimension(300, 36));
        painel.add(txtBuscaPaciente);

        // lista de resultados (só aparece quando há resultados)
        listaResultados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaResultados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listaResultados.setFixedCellHeight(28);
        rolagemResultados.setAlignmentX(LEFT_ALIGNMENT);
        rolagemResultados.setVisible(false);
        painel.add(Box.createVerticalStrut(4));
        painel.add(rolagemResultados);

        // aviso + botão de cadastro (só aparece quando ninguém foi encontrado)
        painelNaoEncontrado.setLayout(new BoxLayout(painelNaoEncontrado, BoxLayout.Y_AXIS));
        painelNaoEncontrado.setOpaque(false);
        painelNaoEncontrado.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblNaoEncontrado = new JLabel("Nenhum paciente encontrado.");
        lblNaoEncontrado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNaoEncontrado.setForeground(new Color(0xB45309));
        lblNaoEncontrado.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCadastrar = new JButton("+ Cadastrar paciente");
        btnCadastrar.setAlignmentX(LEFT_ALIGNMENT);
        btnCadastrar.addActionListener(e -> abrirCadastro());
        painelNaoEncontrado.add(Box.createVerticalStrut(6));
        painelNaoEncontrado.add(lblNaoEncontrado);
        painelNaoEncontrado.add(Box.createVerticalStrut(6));
        painelNaoEncontrado.add(btnCadastrar);
        painelNaoEncontrado.setVisible(false);
        painel.add(painelNaoEncontrado);

        // ficha do paciente escolhido
        lblFicha.setOpaque(true);
        lblFicha.setBackground(Estilo.FUNDO);
        lblFicha.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFicha.setVerticalAlignment(JLabel.TOP);
        lblFicha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(8, 10, 8, 10)));
        lblFicha.setAlignmentX(LEFT_ALIGNMENT);
        lblFicha.setPreferredSize(new Dimension(300, 112));
        lblFicha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        painel.add(Box.createVerticalStrut(8));
        painel.add(lblFicha);
        painel.add(Box.createVerticalStrut(12));

        // digitar no campo refaz a busca
        txtBuscaPaciente.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aoDigitar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aoDigitar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aoDigitar();
            }
        });

        // teclado: seta para baixo vai para a lista; Enter escolhe
        txtBuscaPaciente.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && modeloResultados.size() > 0) {
                    listaResultados.requestFocusInWindow();
                    listaResultados.setSelectedIndex(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && modeloResultados.size() > 0) {
                    escolherPaciente(modeloResultados.get(0));
                }
            }
        });
        listaResultados.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && listaResultados.getSelectedValue() != null) {
                    escolherPaciente(listaResultados.getSelectedValue());
                }
            }
        });
        // mouse: um clique em um paciente da lista escolhe
        listaResultados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int indice = listaResultados.locationToIndex(e.getPoint());
                if (indice < 0) {
                    return;
                }
                Rectangle area = listaResultados.getCellBounds(indice, indice);
                if (area != null && area.contains(e.getPoint())) {
                    escolherPaciente(modeloResultados.get(indice));
                }
            }
        });
    }

    private void adicionarCampo(JPanel painel, String rotulo, Component campo) {
        JLabel label = new JLabel(rotulo);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Estilo.TEXTO_SUAVE);
        label.setAlignmentX(LEFT_ALIGNMENT);

        ((javax.swing.JComponent) campo).setAlignmentX(LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campo.setPreferredSize(new Dimension(300, 36));

        painel.add(label);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campo);
        painel.add(Box.createVerticalStrut(12));
    }

    // ---------------------------------------------------------------- busca do paciente

    /** Chamado a cada letra digitada: esquece o paciente escolhido e refaz a busca. */
    private void aoDigitar() {
        if (atualizandoBusca) {
            return; // o texto foi mudado pelo programa (ao escolher um paciente), não pelo usuário
        }
        pacienteEscolhido = null;
        atualizarFicha();
        filtrar();
    }

    /** Procura nos pacientes pelo nome (sem diferenciar acento e maiúscula) ou pelo CPF. */
    private void filtrar() {
        modeloResultados.clear();
        String texto = txtBuscaPaciente.getText().trim();
        if (texto.isEmpty()) {
            mostrarResultados(false, false);
            return;
        }
        String busca = semAcento(texto);
        String digitos = texto.replaceAll("\\D", "");
        for (Paciente p : todosPacientes) {
            boolean achouNome = semAcento(p.getNome()).contains(busca);
            boolean achouCpf = !digitos.isEmpty()
                    && p.getCpf() != null && p.getCpf().replaceAll("\\D", "").contains(digitos);
            if (achouNome || achouCpf) {
                modeloResultados.addElement(p);
                if (modeloResultados.size() >= 30) {
                    break;
                }
            }
        }
        boolean achou = !modeloResultados.isEmpty();
        if (achou) {
            int altura = Math.min(modeloResultados.size(), 4) * 28 + 4;
            rolagemResultados.setPreferredSize(new Dimension(300, altura));
            rolagemResultados.setMaximumSize(new Dimension(Integer.MAX_VALUE, altura));
        }
        mostrarResultados(achou, !achou);
    }

    private void mostrarResultados(boolean lista, boolean naoEncontrado) {
        rolagemResultados.setVisible(lista);
        painelNaoEncontrado.setVisible(naoEncontrado);
        if (painelFormulario != null) {
            painelFormulario.revalidate();
            painelFormulario.repaint();
        }
    }

    /** Deixa o paciente escolhido: preenche o campo, esconde a lista e mostra a ficha. */
    private void escolherPaciente(Paciente paciente) {
        pacienteEscolhido = paciente;
        atualizandoBusca = true;
        txtBuscaPaciente.setText(paciente.getNome());
        atualizandoBusca = false;
        modeloResultados.clear();
        mostrarResultados(false, false);
        atualizarFicha();
    }

    /** Mostra, no quadro abaixo do campo, os dados do paciente escolhido. */
    private void atualizarFicha() {
        if (pacienteEscolhido == null) {
            lblFicha.setText("<html><span style='color:#6B7280'>Nenhum paciente escolhido.<br>"
                    + "Digite o nome ou o CPF acima para buscar.</span></html>");
            return;
        }
        Paciente p = pacienteEscolhido;
        String nascimento = PacienteController.formatarData(p.getDataNascimento());
        lblFicha.setText("<html><div style='width:250px'><b>" + html(p.getNome()) + "</b><br>"
                + "CPF: " + html(p.getCpf()) + "<br>"
                + "Telefone: " + html(vazioSe(p.getTelefone())) + "<br>"
                + "Nascimento: " + html(vazioSe(nascimento)) + "<br>"
                + "Queixa: " + html(vazioSe(p.getQueixa())) + "</div></html>");
    }

    /** Abre a tela de cadastro (já com o nome digitado) e, ao fechar, atualiza a lista de pacientes. */
    private void abrirCadastro() {
        CadastroPaciente tela = new CadastroPaciente(txtBuscaPaciente.getText().trim());
        tela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                recarregarPacientes();
            }
        });
        tela.setVisible(true);
    }

    /** Lê os pacientes de novo (depois de cadastrar um) e refaz a busca. */
    private void recarregarPacientes() {
        try {
            todosPacientes = controller.listarPacientes();
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
            return;
        }
        filtrar();
        // se o que foi digitado agora acha exatamente um paciente, já escolhe
        String digitado = txtBuscaPaciente.getText().trim();
        if (modeloResultados.size() == 1
                && semAcento(modeloResultados.get(0).getNome()).equals(semAcento(digitado))) {
            escolherPaciente(modeloResultados.get(0));
        }
    }

    /**
     * Painel do formulário. Ele acompanha a LARGURA da área visível (só cresce para baixo,
     * com barra de rolagem), assim os campos nunca ficam cortados na lateral.
     */
    private static class PainelRolavel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle area, int orientacao, int direcao) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle area, int orientacao, int direcao) {
            return area.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static String semAcento(String texto) {
        String base = texto == null ? "" : texto;
        return Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private static String vazioSe(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    private static String html(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ---------------------------------------------------------------- carregar dados

    private void carregarCombos() {
        try {
            todosPacientes = controller.listarPacientes();
            cmbFisioterapeuta.removeAllItems();
            for (Fisioterapeuta f : controller.listarFisioterapeutas()) {
                cmbFisioterapeuta.addItem(f);
            }
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    private void carregarAgenda() {
        try {
            calendario.setAgendamentos(controller.listar());
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    private <T extends Pessoa> void selecionarPorId(JComboBox<T> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- calendário

    /** Clicou em um dia do calendário: preenche a data do formulário. */
    private void aoClicarDia(LocalDate dia) {
        txtData.setText(PacienteController.formatarData(dia));
        txtHorario.requestFocusInWindow();
    }

    private boolean podeEditar(Agendamento a) {
        return a.getStatus() != StatusAgendamento.CANCELADO && a.getStatus() != StatusAgendamento.REALIZADO;
    }

    /** Menu do botão direito em uma caixinha. Cada opção só liga quando faz sentido para o status. */
    private JPopupMenu criarMenu(Agendamento a) {
        StatusAgendamento status = a.getStatus();
        boolean ativo = podeEditar(a);
        boolean podeConfirmar = status == StatusAgendamento.AGENDADO || status == StatusAgendamento.REMARCADO;

        JPopupMenu menu = new JPopupMenu();
        JMenuItem titulo = new JMenuItem(a.getHorario().format(HORARIO) + " · "
                + a.getPaciente().getNome() + " (" + status.name() + ")");
        titulo.setEnabled(false);
        menu.add(titulo);
        menu.addSeparator();
        menu.add(itemDoMenu("Editar", ativo, () -> entrarEdicao(a)));
        menu.addSeparator();
        menu.add(itemDoMenu("Confirmar", podeConfirmar, () -> confirmar(a)));
        menu.add(itemDoMenu("Remarcar...", ativo, () -> remarcar(a)));
        menu.add(itemDoMenu("Cancelar agendamento", ativo, () -> cancelar(a)));
        menu.addSeparator();
        menu.add(itemDoMenu("Excluir", true, () -> excluir(a)));
        return menu;
    }

    private JMenuItem itemDoMenu(String texto, boolean ligado, Runnable acao) {
        JMenuItem item = new JMenuItem(texto);
        item.setEnabled(ligado);
        item.addActionListener(e -> acao.run());
        return item;
    }

    // ---------------------------------------------------------------- criar e editar

    /** O botão da esquerda: cria um agendamento novo ou salva a edição, conforme o modo. */
    private void agendarOuSalvar() {
        if (emEdicao == null) {
            agendar();
        } else {
            salvarEdicao();
        }
    }

    private void agendar() {
        try {
            controller.agendar(pacienteEscolhido,
                    (Fisioterapeuta) cmbFisioterapeuta.getSelectedItem(),
                    txtData.getText(), txtHorario.getText());
            LocalDate dia = lerData(txtData.getText());
            JOptionPane.showMessageDialog(this, "Consulta agendada com sucesso.",
                    "Agendamentos", JOptionPane.INFORMATION_MESSAGE);
            limpar();
            carregarAgenda();
            if (dia != null) {
                calendario.irPara(dia); // mostra o mês do agendamento que acabou de criar
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    /** Editar: leva os dados da caixinha para o formulário e troca o botão para "Salvar". */
    private void entrarEdicao(Agendamento a) {
        calendario.fecharDialogos();
        emEdicao = a;
        escolherPaciente(a.getPaciente());
        selecionarPorId(cmbFisioterapeuta, a.getFisioterapeuta().getId());
        txtData.setText(PacienteController.formatarData(a.getData()));
        txtHorario.setText(a.getHorario().format(HORARIO));
        lblModo.setText("Editando o agendamento nº " + a.getId());
        btnSalvar.setText("Salvar");
        btnLimpar.setText("Cancelar edição");
        calendario.irPara(a.getData());
    }

    private void salvarEdicao() {
        try {
            controller.editar(emEdicao, pacienteEscolhido,
                    (Fisioterapeuta) cmbFisioterapeuta.getSelectedItem(),
                    txtData.getText(), txtHorario.getText());
            LocalDate dia = lerData(txtData.getText());
            JOptionPane.showMessageDialog(this, "Alterações salvas com sucesso.",
                    "Agendamentos", JOptionPane.INFORMATION_MESSAGE);
            limpar();
            carregarAgenda();
            if (dia != null) {
                calendario.irPara(dia);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
            carregarAgenda(); // o objeto em memória pode ter ficado diferente do banco
        }
    }

    // ---------------------------------------------------------------- ações do menu

    private void confirmar(Agendamento a) {
        executarAcao(a, () -> mostrarAvisos(controller.confirmar(a)));
    }

    private void remarcar(Agendamento a) {
        JTextField novaData = new JTextField(PacienteController.formatarData(a.getData()), 10);
        JTextField novoHorario = new JTextField(a.getHorario().format(HORARIO), 6);
        Mascara.data(novaData);
        Mascara.horario(novoHorario);
        JPanel painel = new JPanel(new GridLayout(2, 2, 8, 8));
        painel.add(new JLabel("Nova data (dd/mm/aaaa):"));
        painel.add(novaData);
        painel.add(new JLabel("Novo horário (hh:mm):"));
        painel.add(novoHorario);

        int resposta = JOptionPane.showConfirmDialog(this, painel,
                "Remarcar - " + a.getPaciente().getNome(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resposta != JOptionPane.OK_OPTION) {
            return;
        }
        executarAcao(a, () -> mostrarAvisos(controller.remarcar(a, novaData.getText(), novoHorario.getText())));
    }

    private void cancelar(Agendamento a) {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja cancelar o agendamento de " + a.getPaciente().getNome() + "?",
                "Confirmar cancelamento", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        executarAcao(a, () -> mostrarAvisos(controller.cancelar(a)));
    }

    private void excluir(Agendamento a) {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Excluir o agendamento de " + a.getPaciente().getNome() + " em "
                        + PacienteController.formatarData(a.getData()) + " às "
                        + a.getHorario().format(HORARIO) + "?\nEssa ação não pode ser desfeita.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        executarAcao(a, () -> controller.excluir(a));
    }

    /**
     * Roda uma ação do menu e trata os erros do mesmo jeito para todas:
     * aviso para erro de regra, mensagem para erro de banco. No fim, atualiza o calendário.
     */
    private void executarAcao(Agendamento alvo, Runnable acao) {
        try {
            acao.run();
            if (emEdicao != null && emEdicao.getId() == alvo.getId()) {
                limpar(); // o agendamento que estava sendo editado mudou: sai da edição
            }
            carregarAgenda();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
            carregarAgenda();
        }
    }

    /** Lê "dd/mm/aaaa"; devolve null se estiver inválido. */
    private static LocalDate lerData(String texto) {
        try {
            return LocalDate.parse(texto.trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Volta o formulário para "Novo agendamento" (também cancela uma edição em andamento). */
    private void limpar() {
        emEdicao = null;
        pacienteEscolhido = null;
        atualizandoBusca = true;
        txtBuscaPaciente.setText("");
        atualizandoBusca = false;
        modeloResultados.clear();
        mostrarResultados(false, false);
        atualizarFicha();
        txtData.setText("");
        txtHorario.setText("");
        lblModo.setText("Novo agendamento");
        btnSalvar.setText("Agendar");
        btnLimpar.setText("Limpar");
    }

    /** Mostra os avisos que os observers enviaram ao paciente e ao fisioterapeuta. */
    private void mostrarAvisos(List<String> avisos) {
        if (!avisos.isEmpty()) {
            JOptionPane.showMessageDialog(this, String.join("\n\n", avisos),
                    "Avisos enviados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarErroBanco(RuntimeException e) {
        JOptionPane.showMessageDialog(this,
                "Não foi possível acessar o banco de dados.\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
