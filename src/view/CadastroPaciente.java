package view;

import controller.PacienteController;
import model.Paciente;
import util.Estilo;
import util.Mascara;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela de cadastro de pacientes: formulário à esquerda e lista de pacientes à direita.
 * Clicar em um paciente da lista carrega os dados no formulário para editar ou excluir,
 * e libera o botão "Ver prontuário", que abre o prontuário desse paciente.
 * A tela não conhece SQL: tudo passa pelo PacienteController.
 */
public class CadastroPaciente extends JFrame {

    private final PacienteController controller = new PacienteController();

    private final JTextField txtNome = new JTextField();
    private final JTextField txtCpf = new JTextField();
    private final JTextField txtTelefone = new JTextField();
    private final JTextField txtNascimento = new JTextField();
    private final JTextField txtQueixa = new JTextField();
    private final JLabel lblModo = new JLabel("Novo paciente");

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"ID", "Nome", "CPF", "Telefone"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false; // a tabela só mostra; a edição é feita no formulário
        }
    };
    private final JTable tabela = new JTable(modeloTabela);
    private final JButton btnProntuario = Estilo.botaoPrimario("Ver prontuário");
    private List<Paciente> pacientesNaTabela = new ArrayList<>();

    /** 0 = cadastrando um paciente novo; diferente de 0 = editando o paciente com esse id. */
    private int idEmEdicao = 0;

    public CadastroPaciente() {
        super("Pacientes");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // fecha só esta janela, não o sistema
        setSize(1060, 640);
        setMinimumSize(new Dimension(900, 540));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(Estilo.criarCabecalho("Pacientes",
                "Cadastre e consulte os pacientes da clínica"), BorderLayout.NORTH);
        norte.add(criarBarraVoltar(), BorderLayout.SOUTH);
        raiz.add(norte, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(20, 0));
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(24, 24, 24, 24));
        corpo.add(criarFormulario(), BorderLayout.WEST);
        corpo.add(criarPainelLista(), BorderLayout.CENTER);
        raiz.add(corpo, BorderLayout.CENTER);

        setContentPane(raiz);
        carregarTabela();
    }

    /** Abre a tela já com o nome preenchido (usado pela tela de agendamentos). */
    public CadastroPaciente(String nomeInicial) {
        this();
        txtNome.setText(nomeInicial == null ? "" : nomeInicial);
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

    private JPanel criarFormulario() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));
        painel.setPreferredSize(new Dimension(340, 0));

        lblModo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblModo.setForeground(Estilo.PRIMARIA_ESCURA);
        lblModo.setAlignmentX(LEFT_ALIGNMENT);
        painel.add(lblModo);
        painel.add(Box.createVerticalStrut(14));

        txtCpf.putClientProperty("JTextField.placeholderText", "000.000.000-00");
        txtTelefone.putClientProperty("JTextField.placeholderText", "(00) 00000-0000");
        txtNascimento.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        Mascara.cpf(txtCpf);
        Mascara.telefone(txtTelefone);
        Mascara.data(txtNascimento);

        adicionarCampo(painel, "Nome *", txtNome);
        adicionarCampo(painel, "CPF *", txtCpf);
        adicionarCampo(painel, "Telefone", txtTelefone);
        adicionarCampo(painel, "Data de nascimento", txtNascimento);
        adicionarCampo(painel, "Queixa principal", txtQueixa);

        painel.add(Box.createVerticalGlue());

        JButton btnSalvar = Estilo.botaoPrimario("Salvar");
        btnSalvar.addActionListener(e -> salvar());
        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(e -> limpar());
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(e -> excluir());

        JPanel botoes = new JPanel(new GridLayout(1, 3, 8, 0));
        botoes.setOpaque(false);
        botoes.setAlignmentX(LEFT_ALIGNMENT);
        botoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        botoes.add(btnSalvar);
        botoes.add(btnLimpar);
        botoes.add(btnExcluir);
        painel.add(botoes);
        return painel;
    }

    /** Coloca um rótulo e o campo de texto, um abaixo do outro. */
    private void adicionarCampo(JPanel painel, String rotulo, JTextField campo) {
        JLabel label = new JLabel(rotulo);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Estilo.TEXTO_SUAVE);
        label.setAlignmentX(LEFT_ALIGNMENT);

        campo.setAlignmentX(LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campo.setPreferredSize(new Dimension(280, 36));

        painel.add(label);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campo);
        painel.add(Box.createVerticalStrut(12));
    }

    /** Lado direito: a lista de pacientes e, embaixo, o botão que abre o prontuário. */
    private JPanel criarPainelLista() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setOpaque(false);
        painel.add(criarTabela(), BorderLayout.CENTER);

        JLabel dica = new JLabel("Clique em um paciente da lista para ver o prontuário");
        dica.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dica.setForeground(Estilo.TEXTO_SUAVE);

        btnProntuario.setEnabled(false); // só liga quando um paciente é selecionado
        btnProntuario.addActionListener(e -> verProntuario());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        barra.setOpaque(false);
        barra.add(dica);
        barra.add(btnProntuario);
        painel.add(barra, BorderLayout.SOUTH);
        return painel;
    }

    private JScrollPane criarTabela() {
        tabela.setRowHeight(30);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(240);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(140);

        // Ao clicar em uma linha, os dados vão para o formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarSelecionadoNoFormulario();
                btnProntuario.setEnabled(tabela.getSelectedRow() >= 0);
            }
        });

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.getViewport().setBackground(Color.WHITE);
        return rolagem;
    }

    // ---------------------------------------------------------------- ações

    private void carregarTabela() {
        try {
            pacientesNaTabela = controller.listar();
            modeloTabela.setRowCount(0);
            for (Paciente p : pacientesNaTabela) {
                modeloTabela.addRow(new Object[]{
                        p.getId(), p.getNome(), p.getCpf(), p.getTelefone()});
            }
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    private void carregarSelecionadoNoFormulario() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return;
        }
        Paciente p = pacientesNaTabela.get(linha);
        idEmEdicao = p.getId();
        txtNome.setText(p.getNome());
        txtCpf.setText(p.getCpf());
        txtTelefone.setText(p.getTelefone() == null ? "" : p.getTelefone());
        txtNascimento.setText(PacienteController.formatarData(p.getDataNascimento()));
        txtQueixa.setText(p.getQueixa() == null ? "" : p.getQueixa());
        lblModo.setText("Editando paciente nº " + p.getId());
    }

    /** Abre a tela de prontuários já no paciente selecionado na lista. */
    private void verProntuario() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return;
        }
        Paciente paciente = pacientesNaTabela.get(linha);
        TelaProntuario tela = new TelaProntuario();
        tela.setVisible(true);
        tela.abrirPaciente(paciente);
    }

    private void salvar() {
        try {
            controller.salvar(idEmEdicao, txtNome.getText(), txtCpf.getText(), txtTelefone.getText(),
                    txtNascimento.getText(), txtQueixa.getText());
            JOptionPane.showMessageDialog(this, "Paciente salvo com sucesso.",
                    "Pacientes", JOptionPane.INFORMATION_MESSAGE);
            limpar();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            // erro de preenchimento (campo vazio, CPF inválido...)
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    private void excluir() {
        if (idEmEdicao == 0) {
            JOptionPane.showMessageDialog(this, "Clique em um paciente da lista para excluí-lo.",
                    "Pacientes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja excluir o paciente " + txtNome.getText() + "?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.excluir(idEmEdicao);
            limpar();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            mostrarErroBanco(e);
        }
    }

    private void limpar() {
        idEmEdicao = 0;
        txtNome.setText("");
        txtCpf.setText("");
        txtTelefone.setText("");
        txtNascimento.setText("");
        txtQueixa.setText("");
        lblModo.setText("Novo paciente");
        tabela.clearSelection();
    }

    private void mostrarErroBanco(RuntimeException e) {
        JOptionPane.showMessageDialog(this,
                "Não foi possível acessar o banco de dados.\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
