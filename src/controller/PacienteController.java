package controller;

import dao.PacienteDAO;
import model.Paciente;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * CONTROLLER (MVC): fica entre a tela (view) e o banco (dao).
 * A tela só mostra e coleta dados; aqui ficam as regras: validar o que foi digitado,
 * converter os textos nos tipos certos e chamar o DAO.
 * Quando algo está errado, lança IllegalArgumentException com uma mensagem que a tela exibe.
 */
public class PacienteController {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final PacienteDAO dao = new PacienteDAO();

    public List<Paciente> listar() {
        return dao.listar();
    }

    /**
     * Valida e grava o paciente. Se id for 0, cria um novo; senão, atualiza o existente.
     */
    public void salvar(int id, String nome, String cpf, String telefone, String nascimento, String queixa) {
        nome = nome.trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do paciente.");
        }
        String cpfFormatado = formatarCpf(cpf);
        LocalDate dataNascimento = converterData(nascimento);

        Paciente paciente = new Paciente(id, nome, cpfFormatado, telefone.trim(), dataNascimento, queixa.trim());
        try {
            if (id == 0) {
                dao.inserir(paciente);
            } else {
                dao.atualizar(paciente);
            }
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IllegalArgumentException("Já existe um paciente com este CPF.");
            }
            throw e;
        }
    }

    public void excluir(int id) {
        try {
            dao.excluir(id);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IllegalArgumentException(
                        "Este paciente tem agendamento ou prontuário e não pode ser excluído.");
            }
            throw e;
        }
    }

    /** Aceita CPF com ou sem pontos e traço e devolve no formato 000.000.000-00. */
    static String formatarCpf(String cpf) {
        String numeros = cpf.replaceAll("\\D", "");
        if (numeros.length() != 11) {
            throw new IllegalArgumentException("O CPF deve ter 11 números.");
        }
        return numeros.substring(0, 3) + "." + numeros.substring(3, 6) + "."
                + numeros.substring(6, 9) + "-" + numeros.substring(9);
    }

    /** Converte "dd/mm/aaaa" em data. Texto vazio vira null (data opcional). */
    static LocalDate converterData(String texto) {
        texto = texto.trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            LocalDate data = LocalDate.parse(texto, FORMATO_DATA);
            if (data.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("A data de nascimento não pode estar no futuro.");
            }
            return data;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de nascimento inválida. Use o formato dd/mm/aaaa.");
        }
    }

    /** Transforma a data em texto "dd/mm/aaaa" para mostrar na tela. */
    public static String formatarData(LocalDate data) {
        return data == null ? "" : data.format(FORMATO_DATA);
    }
}
