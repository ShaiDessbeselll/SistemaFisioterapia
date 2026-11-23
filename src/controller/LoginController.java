package controller;

import dao.UsuarioDAO;
import model.Funcionario;
import util.Sessao;

/**
 * CONTROLLER do login: valida o que foi digitado, confere no banco
 * e, se estiver certo, registra o usuário na Sessao.
 */
public class LoginController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** Faz o login. Lança IllegalArgumentException (com mensagem para a tela) se não der certo. */
    public Funcionario entrar(String login, char[] senha) {
        login = login.trim();
        if (login.isEmpty()) {
            throw new IllegalArgumentException("Informe o usuário.");
        }
        if (senha.length == 0) {
            throw new IllegalArgumentException("Informe a senha.");
        }
        Funcionario usuario = usuarioDAO.autenticar(login, new String(senha));
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário ou senha incorretos.");
        }
        Sessao.entrar(usuario);
        return usuario;
    }
}
