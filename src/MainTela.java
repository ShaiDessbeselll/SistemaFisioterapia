import util.Tema;
import view.TelaLogin;

import javax.swing.SwingUtilities;

/**
 * Ponto de partida do sistema com interface gráfica.
 * Primeiro aplica o tema (aparência), depois abre a tela de login.
 * O menu principal só abre depois de entrar com usuário e senha corretos.
 */
public class MainTela {

    public static void main(String[] args) {
        String tema = Tema.aplicar();
        System.out.println("Tema aplicado: " + tema);

        // Toda janela Swing deve ser criada na "thread" própria da interface
        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}