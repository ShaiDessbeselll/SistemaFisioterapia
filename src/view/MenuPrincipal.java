package view;

import util.Estilo;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * Tela inicial do sistema: um menu com botões que levam às outras telas.
 * (Por enquanto os botões só mostram um aviso; as telas são criadas nos próximos passos.)
 */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        super("Clínica de Fisioterapia");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(criarConteudo(this));
        setSize(860, 560);
        setMinimumSize(new Dimension(700, 480));
        setLocationRelativeTo(null); // centraliza na tela
    }

    /** Monta o conteúdo da janela: cabeçalho em cima e os botões no centro. */
    static JPanel criarConteudo(Component dono) {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        raiz.add(Estilo.criarCabecalho("Clínica de Fisioterapia",
                "Gestão de pacientes, agendamentos e prontuários"), BorderLayout.NORTH);

        JPanel grade = new JPanel(new GridLayout(2, 2, 20, 20));
        grade.setOpaque(false);
        grade.setBorder(new EmptyBorder(32, 32, 32, 32));

        grade.add(Estilo.criarCard("Pacientes", "Cadastrar e consultar pacientes",
                () -> new CadastroPaciente().setVisible(true)));
        grade.add(Estilo.criarCard("Agendamentos", "Marcar, confirmar, remarcar e cancelar consultas",
                () -> new TelaAgendamento().setVisible(true)));
        grade.add(Estilo.criarCard("Prontuários", "Registrar atendimentos e evoluções",
                () -> new TelaProntuario().setVisible(true)));
        grade.add(Estilo.criarCard("Sair", "Fechar o sistema",
                () -> System.exit(0)));

        raiz.add(grade, BorderLayout.CENTER);
        return raiz;
    }

    private static void emConstrucao(Component dono, String nome) {
        JOptionPane.showMessageDialog(dono,
                "A tela de " + nome + " será criada no próximo passo.",
                nome, JOptionPane.INFORMATION_MESSAGE);
    }
}
