package view;

import model.Paciente;
import util.Estilo;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Campo para achar um paciente DIGITANDO: ele filtra por nome (sem diferenciar acento e
 * maiúscula) ou por CPF, e mostra os resultados logo abaixo do campo.
 * Se ninguém for encontrado, aparece o botão "Cadastrar paciente".
 *
 * A tela que usa este campo só precisa dizer o que fazer quando:
 *  - um paciente é escolhido (aoEscolher),
 *  - o usuário digita de novo (aoDigitar),
 *  - o usuário clica em cadastrar (aoCadastrar).
 */
public class CampoBuscaPaciente extends JPanel {

    private static final int LARGURA = 380;

    private final JTextField txtBusca = new JTextField();
    private final DefaultListModel<Paciente> modeloResultados = new DefaultListModel<>();
    private final JList<Paciente> listaResultados = new JList<>(modeloResultados);
    private final JScrollPane rolagemResultados = new JScrollPane(listaResultados);
    private final JPanel painelNaoEncontrado = new JPanel();

    private final Consumer<Paciente> aoEscolher;
    private final Runnable aoDigitar;
    private final Runnable aoCadastrar;

    private List<Paciente> pacientes = new ArrayList<>();
    private boolean atualizando = false; // true quando o texto é mudado pelo programa, não pelo usuário

    public CampoBuscaPaciente(String rotulo, Consumer<Paciente> aoEscolher,
                              Runnable aoDigitar, Runnable aoCadastrar) {
        this.aoEscolher = aoEscolher;
        this.aoDigitar = aoDigitar;
        this.aoCadastrar = aoCadastrar;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JLabel label = new JLabel(rotulo);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Estilo.TEXTO_SUAVE);
        label.setAlignmentX(LEFT_ALIGNMENT);
        add(label);
        add(javax.swing.Box.createVerticalStrut(4));

        txtBusca.putClientProperty("JTextField.placeholderText", "Digite o nome ou o CPF do paciente");
        txtBusca.setAlignmentX(LEFT_ALIGNMENT);
        txtBusca.setPreferredSize(new Dimension(LARGURA, 36));
        txtBusca.setMaximumSize(new Dimension(LARGURA, 36));
        add(txtBusca);

        // lista de resultados (só aparece quando há resultados)
        listaResultados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaResultados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listaResultados.setFixedCellHeight(28);
        rolagemResultados.setAlignmentX(LEFT_ALIGNMENT);
        rolagemResultados.setVisible(false);
        add(javax.swing.Box.createVerticalStrut(4));
        add(rolagemResultados);

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
        btnCadastrar.addActionListener(e -> aoCadastrar.run());
        painelNaoEncontrado.add(javax.swing.Box.createVerticalStrut(6));
        painelNaoEncontrado.add(lblNaoEncontrado);
        painelNaoEncontrado.add(javax.swing.Box.createVerticalStrut(6));
        painelNaoEncontrado.add(btnCadastrar);
        painelNaoEncontrado.setVisible(false);
        add(painelNaoEncontrado);

        // digitar no campo refaz a busca
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                digitou();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                digitou();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                digitou();
            }
        });

        // teclado: seta para baixo vai para a lista; Enter escolhe o primeiro
        txtBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && modeloResultados.size() > 0) {
                    listaResultados.requestFocusInWindow();
                    listaResultados.setSelectedIndex(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && modeloResultados.size() > 0) {
                    escolherDaLista(modeloResultados.get(0));
                }
            }
        });
        listaResultados.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && listaResultados.getSelectedValue() != null) {
                    escolherDaLista(listaResultados.getSelectedValue());
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
                    escolherDaLista(modeloResultados.get(indice));
                }
            }
        });
    }

    // ---------------------------------------------------------------- uso pela tela

    /** Troca a lista de pacientes onde a busca procura (e refaz a busca do que está digitado). */
    public void setPacientes(List<Paciente> novos) {
        this.pacientes = new ArrayList<>(novos);
        filtrar();
    }

    /** Texto digitado no campo. */
    public String getTexto() {
        return txtBusca.getText().trim();
    }

    /** Deixa o paciente no campo (sem chamar aoEscolher), por exemplo quando a tela abre já em um paciente. */
    public void escolher(Paciente paciente) {
        preencher(paciente);
    }

    public void limpar() {
        atualizando = true;
        txtBusca.setText("");
        atualizando = false;
        modeloResultados.clear();
        mostrar(false, false);
    }

    /** Depois de cadastrar um paciente: se o que foi digitado acha exatamente ele, já escolhe. */
    public void escolherSeUnico() {
        if (modeloResultados.size() == 1
                && semAcento(modeloResultados.get(0).getNome()).equals(semAcento(getTexto()))) {
            escolherDaLista(modeloResultados.get(0));
        }
    }

    // ---------------------------------------------------------------- busca

    private void digitou() {
        if (atualizando) {
            return;
        }
        aoDigitar.run();
        filtrar();
    }

    private void filtrar() {
        modeloResultados.clear();
        String texto = getTexto();
        if (texto.isEmpty()) {
            mostrar(false, false);
            return;
        }
        String busca = semAcento(texto);
        String digitos = texto.replaceAll("\\D", "");
        for (Paciente p : pacientes) {
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
            rolagemResultados.setPreferredSize(new Dimension(LARGURA, altura));
            rolagemResultados.setMaximumSize(new Dimension(LARGURA, altura));
        }
        mostrar(achou, !achou);
    }

    private void escolherDaLista(Paciente paciente) {
        preencher(paciente);
        aoEscolher.accept(paciente);
    }

    private void preencher(Paciente paciente) {
        atualizando = true;
        txtBusca.setText(paciente.getNome());
        atualizando = false;
        modeloResultados.clear();
        mostrar(false, false);
    }

    private void mostrar(boolean lista, boolean naoEncontrado) {
        rolagemResultados.setVisible(lista);
        painelNaoEncontrado.setVisible(naoEncontrado);
        revalidate();
        repaint();
    }

    private static String semAcento(String texto) {
        String base = texto == null ? "" : texto;
        return Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}
