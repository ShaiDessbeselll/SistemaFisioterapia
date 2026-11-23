package view;

import model.Agendamento;
import util.Estilo;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.font.TextAttribute;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calendário do mês: cada dia é um quadrinho, e cada agendamento do dia é uma "caixinha"
 * colorida conforme o status. Botão direito na caixinha abre o menu de ações.
 *
 * Este painel só desenha e avisa o que o usuário fez (pelo Ouvinte);
 * quem decide o que acontece é a TelaAgendamento.
 */
public class PainelCalendario extends JPanel {

    /** O que o calendário pede para a tela quando o usuário clica em algo. */
    public interface Ouvinte {
        /** Clicou em um dia (fora das caixinhas). */
        void diaClicado(LocalDate dia);

        /** Menu que aparece com o botão direito em uma caixinha. */
        JPopupMenu menuDe(Agendamento agendamento);

        /** Duplo clique em uma caixinha. */
        void duploClique(Agendamento agendamento);
    }

    private static final String[] DIAS_SEMANA = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color BORDA = new Color(0xE5E7EB);

    private final Ouvinte ouvinte;
    private final JLabel lblMes = new JLabel();
    private final JPanel grade = new JPanel(new GridLayout(6, 7, 4, 4));

    private YearMonth mes = YearMonth.now();
    private LocalDate diaSelecionado = null;
    private List<Agendamento> agendamentos = new ArrayList<>();
    // quantas caixinhas cabem em cada dia (ajusta com o tamanho da janela):
    private int cabemSemAviso = 2; // quando todas as do dia cabem
    private int cabemComAviso = 1; // quando sobra gente e precisa da linha "+N mais" 
    private JDialog dialogoDia = null;

    public PainelCalendario(Ouvinte ouvinte) {
        this.ouvinte = ouvinte;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        add(criarTopo(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.setOpaque(false);
        centro.add(criarDiasDaSemana(), BorderLayout.NORTH);
        grade.setOpaque(false);
        centro.add(grade, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JLabel dica = new JLabel("Clique em um dia para preencher a data. "
                + "Clique com o botão direito em um agendamento para editar, confirmar, remarcar, "
                + "cancelar ou excluir.");
        dica.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dica.setForeground(Estilo.TEXTO_SUAVE);
        add(dica, BorderLayout.SOUTH);

        // quando a janela muda de tamanho, vê quantas caixinhas cabem em cada dia
        grade.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarCaixinhas();
            }
        });

        reconstruir();
    }

    // ---------------------------------------------------------------- uso pela tela

    /** Troca os agendamentos mostrados (chamado depois de carregar do banco). */
    public void setAgendamentos(List<Agendamento> novos) {
        this.agendamentos = new ArrayList<>(novos);
        fecharDialogos();
        reconstruir();
    }

    /** Mostra o mês que contém o dia e marca o dia. */
    public void irPara(LocalDate dia) {
        mes = YearMonth.from(dia);
        diaSelecionado = dia;
        reconstruir();
    }

    public void fecharDialogos() {
        if (dialogoDia != null) {
            dialogoDia.dispose();
            dialogoDia = null;
        }
    }

    // ---------------------------------------------------------------- montagem

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topo.setOpaque(false);

        JButton anterior = new JButton("◀");
        anterior.addActionListener(e -> {
            mes = mes.minusMonths(1);
            reconstruir();
        });
        JButton proximo = new JButton("▶");
        proximo.addActionListener(e -> {
            mes = mes.plusMonths(1);
            reconstruir();
        });
        JButton hoje = new JButton("Hoje");
        hoje.addActionListener(e -> {
            mes = YearMonth.now();
            reconstruir();
        });

        lblMes.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMes.setForeground(Estilo.PRIMARIA_ESCURA);
        lblMes.setPreferredSize(new Dimension(210, 34));
        lblMes.setHorizontalAlignment(JLabel.CENTER);

        topo.add(anterior);
        topo.add(lblMes);
        topo.add(proximo);
        topo.add(hoje);
        return topo;
    }

    private JPanel criarDiasDaSemana() {
        JPanel linha = new JPanel(new GridLayout(1, 7, 4, 0));
        linha.setOpaque(false);
        for (String nome : DIAS_SEMANA) {
            JLabel l = new JLabel(nome, JLabel.CENTER);
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            l.setForeground(Estilo.TEXTO_SUAVE);
            linha.add(l);
        }
        return linha;
    }

    /** Calcula quantas caixinhas cabem em um dia, de acordo com a altura disponível. */
    private void ajustarCaixinhas() {
        int alturaDia = (grade.getHeight() - 5 * 4) / 6;
        int espaco = alturaDia - 30;                 // desconta bordas e o número do dia
        int semAviso = Math.max(espaco / 24, 0);     // cada caixinha ocupa 24 pixels
        int comAviso = Math.max((espaco - 16) / 24, 0); // a linha "+N mais" ocupa 16
        if (semAviso != cabemSemAviso || comAviso != cabemComAviso) {
            cabemSemAviso = semAviso;
            cabemComAviso = comAviso;
            SwingUtilities.invokeLater(this::reconstruir);
        }
    }

    /** Desenha o mês de novo (42 quadrinhos: 6 semanas). */
    private void reconstruir() {
        String nomeMes = mes.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
        lblMes.setText(Character.toUpperCase(nomeMes.charAt(0)) + nomeMes.substring(1) + " " + mes.getYear());

        // agrupa os agendamentos por dia (já vêm em ordem de horário)
        Map<LocalDate, List<Agendamento>> porDia = new TreeMap<>();
        for (Agendamento a : agendamentos) {
            porDia.computeIfAbsent(a.getData(), d -> new ArrayList<>()).add(a);
        }

        LocalDate primeiro = mes.atDay(1);
        int deslocamento = primeiro.getDayOfWeek().getValue() % 7; // domingo = 0
        LocalDate dia = primeiro.minusDays(deslocamento);

        grade.removeAll();
        for (int i = 0; i < 42; i++) {
            grade.add(criarDia(dia, porDia.getOrDefault(dia, List.of())));
            dia = dia.plusDays(1);
        }
        grade.revalidate();
        grade.repaint();
    }

    private JPanel criarDia(LocalDate dia, List<Agendamento> doDia) {
        boolean doMes = YearMonth.from(dia).equals(mes);
        boolean ehHoje = dia.equals(LocalDate.now());
        boolean selecionado = dia.equals(diaSelecionado);

        JPanel celula = new JPanel(new BorderLayout(0, 2));
        celula.setBackground(doMes ? Color.WHITE : new Color(0xF3F4F6));
        celula.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selecionado ? Estilo.PRIMARIA : BORDA, selecionado ? 2 : 1),
                new EmptyBorder(3, 4, 3, 4)));

        JLabel numero = new JLabel(String.valueOf(dia.getDayOfMonth()));
        numero.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if (ehHoje) {
            numero.setOpaque(true);
            numero.setBackground(Estilo.PRIMARIA);
            numero.setForeground(Color.WHITE);
            numero.setBorder(new EmptyBorder(0, 5, 0, 5));
        } else {
            numero.setForeground(doMes ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
        }
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topo.setOpaque(false);
        topo.add(numero);
        celula.add(topo, BorderLayout.NORTH);

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);

        // se não couber tudo, mostra uma a menos e o aviso "+N mais"
        int cabem = doDia.size() <= cabemSemAviso ? doDia.size() : cabemComAviso;
        for (int i = 0; i < cabem; i++) {
            lista.add(criarCaixinha(doDia.get(i), 22));
            lista.add(javax.swing.Box.createVerticalStrut(2));
        }
        if (doDia.size() > cabem) {
            JLabel mais = new JLabel("+" + (doDia.size() - cabem) + " mais");
            mais.setFont(new Font("Segoe UI", Font.BOLD, 11));
            mais.setForeground(Estilo.PRIMARIA);
            mais.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            mais.setAlignmentX(LEFT_ALIGNMENT);
            mais.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    abrirDialogoDia(dia, doDia);
                }
            });
            lista.add(mais);
        }
        celula.add(lista, BorderLayout.CENTER);

        // clicar no quadrinho (fora das caixinhas) escolhe o dia
        MouseAdapter cliqueNoDia = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    diaSelecionado = dia;
                    reconstruir();
                    ouvinte.diaClicado(dia);
                }
            }
        };
        celula.addMouseListener(cliqueNoDia);
        topo.addMouseListener(cliqueNoDia);
        numero.addMouseListener(cliqueNoDia);
        lista.addMouseListener(cliqueNoDia);
        return celula;
    }

    /** Uma caixinha de agendamento: horário + nome do paciente, colorida pelo status. */
    private JLabel criarCaixinha(Agendamento a, int altura) {
        Color[] cor = coresDo(a);
        String texto = a.getHorario().format(HORA) + "  " + a.getPaciente().getNome();
        JLabel caixa = new JLabel(texto);
        caixa.setOpaque(true);
        caixa.setBackground(cor[0]);
        caixa.setForeground(cor[1]);
        Font fonte = new Font("Segoe UI", Font.PLAIN, 12);
        if (a.getStatus().name().equals("CANCELADO")) {
            Map<TextAttribute, Object> atributos = new HashMap<>(fonte.getAttributes());
            atributos.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON); // texto riscado
            fonte = fonte.deriveFont(atributos);
        }
        caixa.setFont(fonte);
        caixa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, cor[2]),
                new EmptyBorder(2, 5, 2, 3)));
        caixa.setAlignmentX(LEFT_ALIGNMENT);
        caixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, altura));
        caixa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        caixa.setToolTipText("<html><b>" + html(a.getPaciente().getNome()) + "</b><br>"
                + "Fisioterapeuta: " + html(a.getFisioterapeuta().getNome()) + "<br>"
                + a.getData().format(DATA) + " às " + a.getHorario().format(HORA) + "<br>"
                + "Status: " + a.getStatus().name() + "</html>");

        caixa.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                abrirMenu(e, a);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                abrirMenu(e, a);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    ouvinte.duploClique(a);
                }
            }
        });
        return caixa;
    }

    /** Botão direito: abre o menu de ações (no Windows o gatilho vem ao soltar o botão). */
    private void abrirMenu(MouseEvent e, Agendamento a) {
        if (e.isPopupTrigger()) {
            JPopupMenu menu = ouvinte.menuDe(a);
            if (menu != null) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /** Janelinha com TODOS os agendamentos de um dia (para quando não cabem no quadrinho). */
    private void abrirDialogoDia(LocalDate dia, List<Agendamento> doDia) {
        fecharDialogos();
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Agendamentos de " + dia.format(DATA));
        dialogo.setModal(false);

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBorder(new EmptyBorder(12, 12, 12, 12));
        for (Agendamento a : doDia) {
            conteudo.add(criarCaixinha(a, 28));
            conteudo.add(javax.swing.Box.createVerticalStrut(6));
        }
        dialogo.setContentPane(new JScrollPane(conteudo));
        dialogo.setSize(380, Math.min(480, 100 + doDia.size() * 34));
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
        dialogoDia = dialogo;
    }

    /** {fundo, texto, faixa da esquerda} conforme o status. */
    private static Color[] coresDo(Agendamento a) {
        switch (a.getStatus().name()) {
            case "CONFIRMADO":
                return new Color[]{new Color(0xDCFCE7), new Color(0x166534), new Color(0x16A34A)};
            case "REMARCADO":
                return new Color[]{new Color(0xFEF3C7), new Color(0x92400E), new Color(0xD97706)};
            case "CANCELADO":
                return new Color[]{new Color(0xFEE2E2), new Color(0x991B1B), new Color(0xDC2626)};
            case "REALIZADO":
                return new Color[]{new Color(0xE5E7EB), new Color(0x374151), new Color(0x6B7280)};
            default: // AGENDADO
                return new Color[]{new Color(0xDBEAFE), new Color(0x1E40AF), new Color(0x2563EB)};
        }
    }

    private static String html(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
