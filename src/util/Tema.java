package util;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Define a APARÊNCIA (Look and Feel) das telas Swing.
 * A aparência só muda o visual: os botões, campos e tabelas são os mesmos do Swing.
 *
 * Tenta usar o FlatLaf (biblioteca moderna, o .jar fica na pasta lib).
 * Se o .jar não estiver disponível, usa o Nimbus, que já vem dentro do Java.
 * Deve ser chamado UMA vez, antes de criar qualquer janela.
 */
public final class Tema {

    private Tema() {
    }

    /** Aplica o tema e devolve o nome do que foi usado. */
    public static String aplicar() {
        try {
            FlatLightLaf.setup(); // tema claro e moderno

            // Cantos arredondados em botões, campos de texto e outros componentes
            UIManager.put("Button.arc", 16);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("Component.focusColor", Estilo.PRIMARIA);
            return "FlatLaf";
        } catch (NoClassDefFoundError e) {
            return aplicarNimbus();
        }
    }

    private static String aplicarNimbus() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return "Nimbus";
                }
            }
        } catch (Exception e) {
            // se falhar, o Swing continua com o visual padrão
        }
        return "Padrão do Swing";
    }
}
