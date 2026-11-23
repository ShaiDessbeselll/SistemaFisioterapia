import dao.ProntuarioProxy;
import dao.ProntuarioReal;
import model.Agendamento;
import model.Evolucao;
import model.Fisioterapeuta;
import model.HistoricoProntuario;
import model.NotificadorFisioterapeuta;
import model.NotificadorPaciente;
import model.Paciente;
import model.Prontuario;
import model.Secretaria;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Teste dos padrões Memento, Proxy e Observer no terminal.
 * Não usa o banco: tudo é feito com objetos na memória.
 */
public class MainPadroes {

    public static void main(String[] args) {
        Paciente paciente = new Paciente(1, "Maria Souza", "111.111.111-11", "(45) 99999-1111",
                LocalDate.of(1985, 4, 12), "Dor lombar");
        Fisioterapeuta fisio = new Fisioterapeuta(3, "Ana Paula", "333.333.333-33", "(45) 99999-3333",
                "F001", LocalDate.of(2023, 2, 1), "12345-F", "Ortopedia");
        Secretaria secretaria = new Secretaria(4, "Juliana Costa", "444.444.444-44", "(45) 99999-4444",
                "F002", LocalDate.of(2024, 6, 15), "Manhã");

        Prontuario prontuario = new Prontuario.Builder(paciente)
                .anamnese("Dor lombar há 3 meses.")
                .diagnosticoClinico("Lombalgia crônica")
                .conduta("Alongamento 2x por semana")
                .build();

        // ---------------- MEMENTO ----------------
        System.out.println("=== MEMENTO (desfazer alterações do prontuário) ===");
        HistoricoProntuario historico = new HistoricoProntuario();

        historico.guardar(prontuario.salvarEstado());              // foto antes de editar
        prontuario.setDiagnosticoClinico("Hérnia de disco L4-L5"); // alteração
        prontuario.setConduta("Fortalecimento do core 3x por semana");
        System.out.println("Depois de editar: " + prontuario.getDiagnosticoClinico()
                + " | " + prontuario.getConduta());

        if (historico.temVersoes()) {
            prontuario.restaurarEstado(historico.desfazer());      // volta à foto
        }
        System.out.println("Depois de desfazer: " + prontuario.getDiagnosticoClinico()
                + " | " + prontuario.getConduta());

        // ---------------- PROXY ----------------
        System.out.println("\n=== PROXY (controle de acesso ao prontuário) ===");
        ProntuarioReal real = new ProntuarioReal(prontuario);
        ProntuarioProxy proxyFisio = new ProntuarioProxy(real, fisio);
        ProntuarioProxy proxySecretaria = new ProntuarioProxy(real, secretaria);

        System.out.println("Fisioterapeuta acessa:\n" + proxyFisio.verDadosClinicos());
        proxyFisio.adicionarEvolucao(new Evolucao(0, LocalDate.now(), "Primeira sessão realizada.", fisio));
        System.out.println("Evoluções registradas: " + proxyFisio.listarEvolucoes().size());

        try {
            proxySecretaria.verDadosClinicos();
        } catch (SecurityException e) {
            System.out.println("Secretária tentou acessar -> " + e.getMessage());
        }

        // ---------------- OBSERVER ----------------
        System.out.println("\n=== OBSERVER (avisos quando o agendamento muda) ===");
        Agendamento ag = secretaria.criarAgendamento(paciente, fisio,
                LocalDate.of(2026, 12, 1), LocalTime.of(9, 0));
        ag.adicionarObservador(new NotificadorPaciente(paciente));
        ag.adicionarObservador(new NotificadorFisioterapeuta(fisio));

        secretaria.confirmarAgendamento(ag);
        secretaria.remarcarAgendamento(ag, LocalDate.of(2026, 12, 2), LocalTime.of(14, 30));
        secretaria.cancelarAgendamento(ag);
    }
}
