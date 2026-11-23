package view;

import controller.LoginController;
import util.Estilo;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Arrays;

/**
 * Tela de login: a primeira que aparece. Só depois de entrar com usuário e senha
 * corretos o menu principal é aberto.
 */
public class TelaLogin extends JFrame {

    private final LoginController controller = new LoginController();

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtSenha = new JPasswordField();
    private final JLabel lblErro = new JLabel(" ");

    public TelaLogin() {
        super("Clínica de Fisioterapia - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 540);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.add(criarPainelMarca());
        raiz.add(criarPainelFormulario());
        setContentPane(raiz);
    }

    /** Lado esquerdo: a faixa colorida com o nome do sistema. */
    private JPanel criarPainelMarca() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Estilo.PRIMARIA);
        painel.setBorder(new EmptyBorder(48, 48, 48, 48));

        JLabel titulo = new JLabel("<html>Clínica de<br>Fisioterapia</html>");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descricao = new JLabel("<html>Pacientes, agendamentos e<br>prontuários em um só lugar.</html>");
        descricao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descricao.setForeground(new Color(0xCCEDE9));
        descricao.setAlignmentX(Component.LEFT_ALIGNMENT);

        painel.add(Box.createVerticalGlue());
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(16));
        painel.add(descricao);
        painel.add(Box.createVerticalGlue());
        return painel;
    }

    /** Lado direito: o formulário de entrada. */
    private JPanel criarPainelFormulario() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new EmptyBorder(48, 56, 48, 56));

        JLabel titulo = new JLabel("Entrar");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Estilo.TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Use seu usuário e senha para continuar");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(Estilo.TEXTO_SUAVE);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsuario.putClientProperty("JTextField.placeholderText", "Seu usuário");
        txtSenha.putClientProperty("JTextField.placeholderText", "Sua senha");
        txtSenha.putClientProperty("FlatLaf.style", "showRevealButton: true"); // olhinho (FlatLaf)
        lblErro.setForeground(new Color(0xB91C1C));
        lblErro.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblErro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEntrar = Estilo.botaoPrimario("Entrar");
        btnEntrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnEntrar.addActionListener(e -> entrar());
        getRootPane().setDefaultButton(btnEntrar); // a tecla Enter também faz o login

        JLabel dica = new JLabel("<html>Acesso de demonstração:<br>"
                + "ana.paula / fisio123 (fisioterapeuta)<br>"
                + "juliana / secretaria123 (secretária)</html>");
        dica.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dica.setForeground(Estilo.TEXTO_SUAVE);
        dica.setAlignmentX(Component.LEFT_ALIGNMENT);

        painel.add(Box.createVerticalGlue());
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(6));
        painel.add(subtitulo);
        painel.add(Box.createVerticalStrut(28));
        adicionarCampo(painel, "Usuário", txtUsuario);
        adicionarCampo(painel, "Senha", txtSenha);
        painel.add(lblErro);
        painel.add(Box.createVerticalStrut(10));
        painel.add(btnEntrar);
        painel.add(Box.createVerticalStrut(24));
        painel.add(dica);
        painel.add(Box.createVerticalGlue());
        return painel;
    }

    private void adicionarCampo(JPanel painel, String rotulo, JTextField campo) {
        JLabel label = new JLabel(rotulo);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Estilo.TEXTO_SUAVE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        campo.setPreferredSize(new Dimension(300, 40));

        painel.add(label);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campo);
        painel.add(Box.createVerticalStrut(14));
    }

    private void entrar() {
        lblErro.setText(" ");
        char[] senha = txtSenha.getPassword();
        try {
            controller.entrar(txtUsuario.getText(), senha);
            dispose();                              // fecha o login
            new MenuPrincipal().setVisible(true);   // abre o menu
        } catch (IllegalArgumentException e) {
            lblErro.setText(e.getMessage());
            txtSenha.setText("");
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível acessar o banco de dados.\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(senha, '0'); // apaga a senha da memória
        }
    }
}
