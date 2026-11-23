package util;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;

/**
 * Cores, fontes e componentes prontos, para todas as telas terem o mesmo visual.
 * Se quiser mudar a cor do sistema inteiro, é só alterar PRIMARIA aqui.
 */
public final class Estilo {

    public static final Color PRIMARIA = new Color(0x0F766E);
    public static final Color PRIMARIA_ESCURA = new Color(0x115E59);
    public static final Color FUNDO = new Color(0xF4F7F6);
    public static final Color TEXTO = new Color(0x1F2937);
    public static final Color TEXTO_SUAVE = new Color(0x6B7280);

    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SUBTITULO = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font TEXTO_BOTAO = new Font("Segoe UI", Font.BOLD, 14);

    private Estilo() {
    }

    /** Faixa colorida no topo da tela, com título e subtítulo. */
    public static JPanel criarCabecalho(String titulo, String subtitulo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(PRIMARIA);
        painel.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(TITULO);
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setFont(SUBTITULO);
        lblSubtitulo.setForeground(new Color(0xCCEDE9));

        painel.add(lblTitulo);
        painel.add(javax.swing.Box.createVerticalStrut(4));
        painel.add(lblSubtitulo);
        return painel;
    }

    /** Botão grande (cartão) com título e descrição, usado no menu principal. */
    public static JButton criarCard(String titulo, String descricao, Runnable acao) {
        JButton botao = new JButton("<html><div style='text-align:center'>"
                + "<span style='font-size:16pt'><b>" + titulo + "</b></span><br>"
                + "<span style='font-size:10pt;color:#6B7280'>" + descricao + "</span>"
                + "</div></html>");
        botao.setBackground(Color.WHITE);
        botao.setForeground(TEXTO);
        botao.setFocusPainted(false);
        botao.setMargin(new Insets(24, 24, 24, 24));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.putClientProperty("JButton.buttonType", "roundRect"); // arredondado no FlatLaf
        botao.addActionListener(e -> acao.run());
        return botao;
    }

    /** Botão de ação principal (Salvar, Entrar...), na cor do sistema. */
    public static JButton botaoPrimario(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(PRIMARIA);
        botao.setForeground(Color.WHITE);
        botao.setFont(TEXTO_BOTAO);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.putClientProperty("JButton.buttonType", "roundRect");
        return botao;
    }
}
