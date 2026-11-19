-- =====================================================
-- Sistema da Clínica de Fisioterapia
-- Script de criação do banco de dados (MySQL)
-- Baseado no diagrama de classes da abstração
-- =====================================================

DROP DATABASE IF EXISTS clinica_fisioterapia;
CREATE DATABASE clinica_fisioterapia
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE clinica_fisioterapia;

-- -----------------------------------------------------
-- Pessoa (classe abstrata): dados comuns a todos
-- -----------------------------------------------------
CREATE TABLE pessoa (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    cpf       VARCHAR(14)  NOT NULL UNIQUE,
    telefone  VARCHAR(20)
);

-- -----------------------------------------------------
-- Paciente (herda de Pessoa)
-- -----------------------------------------------------
CREATE TABLE paciente (
    id_pessoa        INT PRIMARY KEY,
    data_nascimento  DATE,
    queixa           VARCHAR(255),
    CONSTRAINT fk_paciente_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa (id)
        ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Funcionario (classe abstrata, herda de Pessoa)
-- -----------------------------------------------------
CREATE TABLE funcionario (
    id_pessoa       INT PRIMARY KEY,
    matricula       VARCHAR(20) NOT NULL UNIQUE,
    data_admissao   DATE,
    CONSTRAINT fk_funcionario_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa (id)
        ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Fisioterapeuta (herda de Funcionario)
-- -----------------------------------------------------
CREATE TABLE fisioterapeuta (
    id_funcionario  INT PRIMARY KEY,
    crefito         VARCHAR(20) NOT NULL UNIQUE,
    especialidade   VARCHAR(100),
    CONSTRAINT fk_fisioterapeuta_funcionario
        FOREIGN KEY (id_funcionario) REFERENCES funcionario (id_pessoa)
        ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Secretaria (herda de Funcionario)
-- -----------------------------------------------------
CREATE TABLE secretaria (
    id_funcionario  INT PRIMARY KEY,
    turno           VARCHAR(20),
    CONSTRAINT fk_secretaria_funcionario
        FOREIGN KEY (id_funcionario) REFERENCES funcionario (id_pessoa)
        ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Agendamento
-- Liga um paciente a um fisioterapeuta e é registrado por uma secretária
-- -----------------------------------------------------
CREATE TABLE agendamento (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    data                DATE NOT NULL,
    horario             TIME NOT NULL,
    status              ENUM('AGENDADO', 'CONFIRMADO', 'CANCELADO', 'REMARCADO', 'REALIZADO')
                        NOT NULL DEFAULT 'AGENDADO',
    id_paciente         INT NOT NULL,
    id_fisioterapeuta   INT NOT NULL,
    id_secretaria       INT NOT NULL,
    CONSTRAINT fk_agendamento_paciente
        FOREIGN KEY (id_paciente) REFERENCES paciente (id_pessoa),
    CONSTRAINT fk_agendamento_fisioterapeuta
        FOREIGN KEY (id_fisioterapeuta) REFERENCES fisioterapeuta (id_funcionario),
    CONSTRAINT fk_agendamento_secretaria
        FOREIGN KEY (id_secretaria) REFERENCES secretaria (id_funcionario),
    -- evita dois agendamentos do mesmo fisioterapeuta no mesmo dia e horário
    CONSTRAINT uq_agendamento_fisio_horario
        UNIQUE (id_fisioterapeuta, data, horario)
);

-- -----------------------------------------------------
-- Prontuario (um por paciente)
-- -----------------------------------------------------
CREATE TABLE prontuario (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    diagnostico     VARCHAR(255),
    data_abertura   DATE NOT NULL,
    id_paciente     INT NOT NULL UNIQUE,
    CONSTRAINT fk_prontuario_paciente
        FOREIGN KEY (id_paciente) REFERENCES paciente (id_pessoa)
);

-- -----------------------------------------------------
-- Evolucao (pertence a um prontuário; composição)
-- -----------------------------------------------------
CREATE TABLE evolucao (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    data                DATE NOT NULL,
    descricao           TEXT NOT NULL,
    id_prontuario       INT NOT NULL,
    id_fisioterapeuta   INT NOT NULL,
    CONSTRAINT fk_evolucao_prontuario
        FOREIGN KEY (id_prontuario) REFERENCES prontuario (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_evolucao_fisioterapeuta
        FOREIGN KEY (id_fisioterapeuta) REFERENCES fisioterapeuta (id_funcionario)
);

-- =====================================================
-- Dados de exemplo para testar o sistema
-- =====================================================

-- Pessoas (ids 1 a 4)
INSERT INTO pessoa (nome, cpf, telefone) VALUES
    ('Maria Souza',   '111.111.111-11', '(45) 99999-1111'),
    ('Carlos Lima',   '222.222.222-22', '(45) 99999-2222'),
    ('Ana Paula',     '333.333.333-33', '(45) 99999-3333'),
    ('Juliana Costa', '444.444.444-44', '(45) 99999-4444');

INSERT INTO paciente (id_pessoa, data_nascimento, queixa) VALUES
    (1, '1985-04-12', 'Dor lombar'),
    (2, '1990-09-30', 'Lesão no joelho');

INSERT INTO funcionario (id_pessoa, matricula, data_admissao) VALUES
    (3, 'F001', '2023-02-01'),
    (4, 'F002', '2024-06-15');

INSERT INTO fisioterapeuta (id_funcionario, crefito, especialidade) VALUES
    (3, '12345-F', 'Ortopedia');

INSERT INTO secretaria (id_funcionario, turno) VALUES
    (4, 'Manhã');

INSERT INTO agendamento (data, horario, status, id_paciente, id_fisioterapeuta, id_secretaria) VALUES
    ('2026-11-25', '09:00:00', 'AGENDADO',   1, 3, 4),
    ('2026-11-25', '10:00:00', 'CONFIRMADO', 2, 3, 4);

INSERT INTO prontuario (diagnostico, data_abertura, id_paciente) VALUES
    ('Lombalgia crônica', '2026-11-10', 1),
    ('Lesão de ligamento no joelho', '2026-11-12', 2);

INSERT INTO evolucao (data, descricao, id_prontuario, id_fisioterapeuta) VALUES
    ('2026-11-10', 'Avaliação inicial e início do alongamento.', 1, 3),
    ('2026-11-17', 'Melhora da dor, mantida a série de exercícios.', 1, 3);

-- Consulta de teste: agenda com nomes de paciente e fisioterapeuta
-- SELECT a.data, a.horario, a.status, p.nome AS paciente, f.nome AS fisioterapeuta
-- FROM agendamento a
-- JOIN pessoa p ON p.id = a.id_paciente
-- JOIN pessoa f ON f.id = a.id_fisioterapeuta;
