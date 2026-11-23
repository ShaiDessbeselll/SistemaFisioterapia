import dao.Conexao;
import model.Agendamento;
import model.Fisioterapeuta;
import model.Paciente;
import model.Prontuario;
import model.Secretaria;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Teste rápido: monta objetos do model na memória e testa a conexão com o banco.
 */
public class Main {

    public static void main(String[] args) {
        // 1) Testa o model (não precisa do banco)
        Paciente paciente = new Paciente(1, "Maria Souza", "111.111.111-11", "(45) 99999-1111",
                LocalDate.of(1985, 4, 12), "Dor lombar");
        Fisioterapeuta fisio = new Fisioterapeuta(3, "Ana Paula", "333.333.333-33", "(45) 99999-3333",
                "F001", LocalDate.of(2023, 2, 1), "12345-F", "Ortopedia");
        Secretaria secretaria = new Secretaria(4, "Juliana Costa", "444.444.444-44", "(45) 99999-4444",
                "F002", LocalDate.of(2024, 6, 15), "Manhã");

        Agendamento ag = secretaria.criarAgendamento(paciente, fisio,
                LocalDate.of(2026, 11, 25), LocalTime.of(9, 0));
        secretaria.confirmarAgendamento(ag);
        System.out.println(ag);

        // Prontuário montado com o padrão Builder
        Prontuario prontuario = new Prontuario.Builder(paciente)
                .anamnese("Dor lombar há 3 meses, piora ao ficar sentada.")
                .diagnosticoClinico("Lombalgia crônica")
                .diagnosticoFisioterapeutico("Encurtamento de isquiotibiais e fraqueza do core")
                .exameFisico("Limitação na flexão de tronco")
                .objetivos("Reduzir a dor e melhorar a mobilidade")
                .conduta("Alongamento e fortalecimento, 2 vezes por semana")
                .build();
        paciente.setProntuario(prontuario);
        fisio.registrarEvolucao(prontuario, "Avaliação inicial e início do alongamento.");
        System.out.println(paciente.consultarProntuario().listarEvolucoes());

        // 2) Testa a conexão com o banco
        try (Connection conn = Conexao.getConexao()) {
            System.out.println("Conexão com o banco OK: " + !conn.isClosed());
        } catch (Exception e) {
            System.out.println("Falha na conexão: " + e.getMessage());
        }
    }
}
