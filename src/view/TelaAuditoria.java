package view;

import model.RegistroAuditoria;
import util.Estilo;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Histórico (auditoria) do prontuário: quem fez o quê e quando.
 * Só leitura: os registros não podem ser editados nem apagados.
 */
public class TelaAuditoria extends JDialog {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final List<RegistroAuditoria> registros;
    private final JTextArea txtDetalhes = new JTextArea();
    private final JTable tabela;

    public TelaAuditoria(JFrame dono, String titulo, List<RegistroAuditoria> registros) {
        super(dono, "Histórico do prontuário", true);
        this.registros = registros;
        setSize(900, 560);
        setLocationRelativeTo(dono);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(Estilo.criarCabecalho("Histórico do prontuário", titulo), BorderLayout.NORTH);

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Data/hora", "Usuário", "Ação"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        for (RegistroAuditoria r : registros) {
            modelo.addRow(new Object[]{r.getDataHora().format(FORMATO), r.getUsuario(), r.getAcaoLegivel()});
        }
        tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabela.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(220);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalhes();
            }
        });

        txtDetalhes.setEditable(false);
        txtDetalhes.setLineWrap(true);
        txtDetalhes.setWrapStyleWord(true);
        txtDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDetalhes.setMargin(new java.awt.Insets(8, 10, 8, 10));
        JScrollPane rolagemDetalhes = new JScrollPane(txtDetalhes);
        rolagemDetalhes.setPreferredSize(new Dimension(300, 150));

        JSplitPane divisor = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabela), rolagemDetalhes);
        divisor.setResizeWeight(0.65);
        divisor.setBorder(new EmptyBorder(14, 24, 8, 24));
        raiz.add(divisor, BorderLayout.CENTER);

        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(e -> dispose());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 10));
        rodape.add(fechar);
        raiz.add(rodape, BorderLayout.SOUTH);

        setContentPane(raiz);
        if (registros.isEmpty()) {
            txtDetalhes.setText("Ainda não há registros.");
        } else {
            tabela.setRowSelectionInterval(0, 0);
        }
    }

    private void mostrarDetalhes() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            txtDetalhes.setText("");
            return;
        }
        RegistroAuditoria r = registros.get(linha);
        String detalhes = r.getDetalhes() == null ? "" : r.getDetalhes();
        txtDetalhes.setText(detalhes);
        txtDetalhes.setCaretPosition(0);
    }
}
