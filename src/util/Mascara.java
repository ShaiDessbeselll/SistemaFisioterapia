package util;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import java.util.function.Function;

/**
 * Máscaras para campos de texto: enquanto a pessoa digita só números, o campo coloca
 * sozinho os símbolos (barra, ponto, traço, parênteses e dois pontos).
 *
 * Exemplo: digitando 12345678901 no campo de CPF aparece 123.456.789-01.
 * O texto continua sendo um JTextField comum; getText() devolve o texto já formatado,
 * que os controllers conhecem e validam.
 *
 * Uso: Mascara.cpf(txtCpf);
 */
public final class Mascara {

    private Mascara() {
    }

    /** 000.000.000-00 */
    public static void cpf(JTextField campo) {
        aplicar(campo, 11, digitos -> formatar(digitos, "###.###.###-##"));
    }

    /** (00) 0000-0000 (fixo) ou (00) 00000-0000 (celular): o formato muda sozinho com o 11º número. */
    public static void telefone(JTextField campo) {
        aplicar(campo, 11,
                digitos -> formatar(digitos, digitos.length() <= 10 ? "(##) ####-####" : "(##) #####-####"));
    }

    /** dd/mm/aaaa */
    public static void data(JTextField campo) {
        aplicar(campo, 8, digitos -> formatar(digitos, "##/##/####"));
    }

    /** hh:mm */
    public static void horario(JTextField campo) {
        aplicar(campo, 4, digitos -> formatar(digitos, "##:##"));
    }

    private static void aplicar(JTextField campo, int maxDigitos, Function<String, String> formatador) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new Filtro(campo, maxDigitos, formatador));
    }

    /**
     * Preenche o padrão com os dígitos, na ordem. Cada "#" recebe um dígito; os outros
     * caracteres são símbolos, que só aparecem quando ainda há dígito para vir depois deles.
     * Exemplo: formatar("12345", "##/##/####") devolve "12/34/5".
     */
    static String formatar(String digitos, String padrao) {
        StringBuilder saida = new StringBuilder();
        int usados = 0;
        for (int i = 0; i < padrao.length() && usados < digitos.length(); i++) {
            char c = padrao.charAt(i);
            if (c == '#') {
                saida.append(digitos.charAt(usados++));
            } else {
                saida.append(c);
            }
        }
        return saida.toString();
    }

    private static String apenasDigitos(String texto) {
        StringBuilder digitos = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (c >= '0' && c <= '9') {
                digitos.append(c);
            }
        }
        return digitos.toString();
    }

    /** Posição logo depois do n-ésimo dígito do texto formatado. */
    private static int posicaoApos(String formatado, int n) {
        if (n <= 0) {
            return 0;
        }
        int contados = 0;
        for (int i = 0; i < formatado.length(); i++) {
            char c = formatado.charAt(i);
            if (c >= '0' && c <= '9') {
                contados++;
                if (contados == n) {
                    return i + 1;
                }
            }
        }
        return formatado.length();
    }

    /** Filtro que reformata o texto a cada alteração (digitar, colar, apagar). */
    private static class Filtro extends DocumentFilter {

        private final JTextField campo;
        private final int maxDigitos;
        private final Function<String, String> formatador;

        Filtro(JTextField campo, int maxDigitos, Function<String, String> formatador) {
            this.campo = campo;
            this.maxDigitos = maxDigitos;
            this.formatador = formatador;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attrs)
                throws BadLocationException {
            replace(fb, offset, 0, texto, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int tamanho) throws BadLocationException {
            String removido = fb.getDocument().getText(offset, tamanho);
            if (!removido.isEmpty() && apenasDigitos(removido).isEmpty()) {
                // Só apagaria um símbolo (/ . - : parênteses): em vez de travar, o cursor pula o símbolo
                boolean apagouParaTras = campo.getCaretPosition() == offset + tamanho;
                campo.setCaretPosition(apagouParaTras ? offset : offset + tamanho);
                return;
            }
            replace(fb, offset, tamanho, "", null);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int tamanho, String texto, AttributeSet attrs)
                throws BadLocationException {
            Document doc = fb.getDocument();
            String atual = doc.getText(0, doc.getLength());
            String inserido = texto == null ? "" : texto;
            String novo = atual.substring(0, offset) + inserido + atual.substring(offset + tamanho);

            String digitos = apenasDigitos(novo);
            if (digitos.length() > maxDigitos) {
                digitos = digitos.substring(0, maxDigitos);
            }
            String formatado = formatador.apply(digitos);
            int digitosAntesDoCursor = Math.min(
                    apenasDigitos(novo.substring(0, offset + inserido.length())).length(), digitos.length());

            if (!formatado.equals(atual)) {
                fb.replace(0, doc.getLength(), formatado, attrs);
            }
            campo.setCaretPosition(posicaoApos(formatado, digitosAntesDoCursor));
        }
    }
}
