import dao.AgendamentoDAO;
import dao.FisioterapeutaDAO;
import dao.PacienteDAO;
import dao.ProntuarioDAO;
import dao.SecretariaDAO;
import model.Agendamento;
import model.Evolucao;
import model.Fisioterapeuta;
import model.Paciente;
import model.Prontuario;
import model.Secretaria;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Teste dos DAOs: grava e lê dados de verdade no banco clinica_fisioterapia.
 * Cada vez que roda, acrescenta um paciente novo (o CPF é gerado para não repetir).
 */
public class MainDAO {

    public static void main(String[] args) {
        PacienteDAO pacienteDAO = new PacienteDAO();
        FisioterapeutaDAO fisioterapeutaDAO = new FisioterapeutaDAO();
        SecretariaDAO secretariaDAO = new SecretariaDAO();
        AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
        ProntuarioDAO prontuarioDAO = new ProntuarioDAO();

        // 1) Paciente novo
        String cpfTeste = "T" + (System.currentTimeMillis() % 1000000000L);
        Paciente paciente = new Paciente(0, "Paciente Teste DAO", cpfTeste, "(45) 90000-0000",
                LocalDate.of(1995, 5, 20), "Dor no ombro");
        pacienteDAO.inserir(paciente);
        System.out.println("Paciente gravado com id " + paciente.getId());

        System.out.println("--- Pacientes no banco ---");
        for (Paciente p : pacienteDAO.listar()) {
            System.out.println(p.getId() + " - " + p.getNome());
        }

        // 2) Fisioterapeuta e secretária que já existem no banco (dados de exemplo)
        Fisioterapeuta fisio = fisioterapeutaDAO.buscarPorId(3);
        Secretaria secretaria = secretariaDAO.buscarPorId(4);
        System.out.println("Fisioterapeuta: " + fisio.getNome() + " | Secretária: " + secretaria.getNome());

        // 3) Agendamento: cria, grava, confirma, atualiza e lê de volta
        LocalTime horario = LocalTime.of(8, (int) (System.currentTimeMillis() / 1000 % 60));
        Agendamento ag = secretaria.criarAgendamento(paciente, fisio, LocalDate.of(2026, 12, 1), horario);
        agendamentoDAO.inserir(ag);
        secretaria.confirmarAgendamento(ag);
        agendamentoDAO.atualizar(ag);
        System.out.println("Agendamento lido do banco: " + agendamentoDAO.buscarPorId(ag.getId()));

        // 4) Prontuário (Builder): grava, registra uma evolução e lê tudo de volta
        Prontuario prontuario = new Prontuario.Builder(paciente)
                .anamnese("Dor no ombro direito há 2 semanas.")
                .diagnosticoClinico("Tendinite do supraespinhal")
                .diagnosticoFisioterapeutico("Limitação de abdução do ombro")
                .exameFisico("Dor à elevação acima de 90 graus")
                .objetivos("Recuperar a amplitude de movimento")
                .conduta("Mobilização e exercícios de fortalecimento")
                .build();
        prontuarioDAO.inserir(prontuario);

        fisio.registrarEvolucao(prontuario, "Primeira sessão: boa resposta à mobilização.");
        List<Evolucao> evolucoes = prontuario.listarEvolucoes();
        prontuarioDAO.inserirEvolucao(prontuario, evolucoes.get(evolucoes.size() - 1));

        Prontuario lido = prontuarioDAO.buscarPorPaciente(paciente);
        System.out.println("Prontuário lido do banco: " + lido.getDiagnosticoClinico()
                + " | evoluções: " + lido.listarEvolucoes());
    }
}
