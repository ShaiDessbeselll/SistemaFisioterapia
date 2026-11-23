package util;

import model.Fisioterapeuta;
import model.Funcionario;
import model.Secretaria;

/**
 * Guarda quem está usando o sistema neste momento (o usuário que fez login).
 * As telas e os controllers consultam aqui para saber o perfil (fisioterapeuta ou secretária).
 */
public final class Sessao {

    private static Funcionario usuario;

    private Sessao() {
    }

    public static void entrar(Funcionario funcionario) {
        usuario = funcionario;
    }

    public static void sair() {
        usuario = null;
    }

    public static Funcionario getUsuario() {
        return usuario;
    }

    /** Texto como "Ana Paula (Fisioterapeuta)", para mostrar nas telas. */
    public static String descricao() {
        if (usuario == null) {
            return "";
        }
        if (usuario instanceof Fisioterapeuta) {
            return usuario.getNome() + " (Fisioterapeuta)";
        }
        if (usuario instanceof Secretaria) {
            return usuario.getNome() + " (Secretária)";
        }
        return usuario.getNome();
    }
}
