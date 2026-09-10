-- =====================================================================
-- ClyvoVet - Massa de teste para dev local / demo do app mobile
--
-- Duas decisoes de portabilidade aqui:
--
-- 1) Nenhum ID e informado no INSERT. As FKs sao resolvidas por subquery
--    sobre a coluna unica (email / nome). Se fixassemos os IDs, a sequence
--    IDENTITY nao avancaria e o primeiro POST da API colidiria com a PK.
--
-- 2) As datas sao relativas a CURRENT_DATE usando literais INTERVAL
--    (ANSI, aceito por H2 e Oracle). Assim a demo continua fazendo sentido
--    daqui a seis meses: sempre havera vacina atrasada e vacina proxima.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuarios (senhas em BCrypt, cost 10)
--   admin@clyvovet.com.br   -> admin123   (ROLE_ADMIN)
--   gabriel@clyvovet.com.br -> tutor123   (ROLE_TUTOR)
-- ---------------------------------------------------------------------
INSERT INTO USUARIOS (usuario_email, usuario_senha, usuario_role) VALUES
    ('admin@clyvovet.com.br',   '$2a$10$M05S2r8VPFECSwoAQ09lR.lzbHFDNlowPf1Pc3BKzMjPMboYAMYSa', 'ROLE_ADMIN');
INSERT INTO USUARIOS (usuario_email, usuario_senha, usuario_role) VALUES
    ('gabriel@clyvovet.com.br', '$2a$10$WhVS5wErwkOSupZ0x5jt/OFGBwBTKeD6BNs7hZTeIk8gsrj5xHp0a', 'ROLE_TUTOR');

-- ---------------------------------------------------------------------
-- Tutores
-- ---------------------------------------------------------------------
INSERT INTO DONOS (dono_nome, dono_email, dono_telefone) VALUES
    ('Gabriel Oliveira', 'gabriel@clyvovet.com.br', '(11) 98877-1234');
INSERT INTO DONOS (dono_nome, dono_email, dono_telefone) VALUES
    ('Marina Souza', 'marina@clyvovet.com.br', '(11) 97766-4321');

-- ---------------------------------------------------------------------
-- Equipe
-- ---------------------------------------------------------------------
INSERT INTO FUNCIONARIOS (funcionario_nome, funcionario_setor, funcionario_cargo, funcionario_email, funcionario_telefone) VALUES
    ('Dra. Helena Prado', 'Clinica Geral', 'Veterinaria', 'helena@clyvovet.com.br', '(11) 3344-1010');
INSERT INTO FUNCIONARIOS (funcionario_nome, funcionario_setor, funcionario_cargo, funcionario_email, funcionario_telefone) VALUES
    ('Dr. Rafael Lima', 'Cirurgia', 'Cirurgiao Veterinario', 'rafael@clyvovet.com.br', '(11) 3344-2020');

-- ---------------------------------------------------------------------
-- Pets
--   Rex -> 3 anos exatos  (DTO devolve "3 anos")
--   Mel -> 5 meses        (DTO devolve "5 meses")
-- ---------------------------------------------------------------------
INSERT INTO PETS (pet_nome, pet_data_nascimento, pet_peso, pet_especie, pet_raca, dono_id) VALUES
    ('Rex', CURRENT_DATE - INTERVAL '3' YEAR, 28.50, 'Cachorro', 'Golden Retriever',
     (SELECT dono_id FROM DONOS WHERE dono_email = 'gabriel@clyvovet.com.br'));
INSERT INTO PETS (pet_nome, pet_data_nascimento, pet_peso, pet_especie, pet_raca, dono_id) VALUES
    ('Mel', CURRENT_DATE - INTERVAL '5' MONTH, 3.20, 'Gato', 'Siames',
     (SELECT dono_id FROM DONOS WHERE dono_email = 'marina@clyvovet.com.br'));

-- ---------------------------------------------------------------------
-- Calendario vacinal
-- Cobre os tres cenarios que o IntelligenceService avalia:
-- aplicada ('A'), atrasada ('P' no passado) e proxima ('P' dentro de 7 dias).
-- ---------------------------------------------------------------------
INSERT INTO VACINAS (vacina_nome, vacina_data, vacina_status, pet_id) VALUES
    ('V10 (Polivalente)', CURRENT_DATE - INTERVAL '10' MONTH, 'A', (SELECT pet_id FROM PETS WHERE pet_nome = 'Rex'));
INSERT INTO VACINAS (vacina_nome, vacina_data, vacina_status, pet_id) VALUES
    ('Antirrabica', CURRENT_DATE - INTERVAL '20' DAY, 'P', (SELECT pet_id FROM PETS WHERE pet_nome = 'Rex'));
INSERT INTO VACINAS (vacina_nome, vacina_data, vacina_status, pet_id) VALUES
    ('Giardia', CURRENT_DATE + INTERVAL '5' DAY, 'P', (SELECT pet_id FROM PETS WHERE pet_nome = 'Rex'));
INSERT INTO VACINAS (vacina_nome, vacina_data, vacina_status, pet_id) VALUES
    ('Triplice Felina', CURRENT_DATE - INTERVAL '2' MONTH, 'A', (SELECT pet_id FROM PETS WHERE pet_nome = 'Mel'));
INSERT INTO VACINAS (vacina_nome, vacina_data, vacina_status, pet_id) VALUES
    ('Antirrabica', CURRENT_DATE + INTERVAL '3' DAY, 'P', (SELECT pet_id FROM PETS WHERE pet_nome = 'Mel'));

-- ---------------------------------------------------------------------
-- Medicamentos
-- ---------------------------------------------------------------------
INSERT INTO MEDICAMENTOS (medicamento_nome) VALUES ('Vermifugo Drontal Plus');
INSERT INTO MEDICAMENTOS (medicamento_nome) VALUES ('Anti-inflamatorio Meloxicam');

-- ---------------------------------------------------------------------
-- Consultas
-- ---------------------------------------------------------------------
INSERT INTO CONSULTAS (consulta_tipo, consulta_valor, consulta_descricao, consulta_status, consulta_data, pet_id, funcionario_id) VALUES
    ('Rotina', 180.00, 'Check-up anual preventivo. Animal saudavel, peso estavel.', 'R',
     CURRENT_TIMESTAMP - INTERVAL '45' DAY,
     (SELECT pet_id FROM PETS WHERE pet_nome = 'Rex'),
     (SELECT funcionario_id FROM FUNCIONARIOS WHERE funcionario_email = 'helena@clyvovet.com.br'));

INSERT INTO CONSULTAS (consulta_tipo, consulta_valor, consulta_descricao, consulta_status, consulta_data, pet_id, funcionario_id) VALUES
    ('Retorno', 90.00, 'Retorno para avaliacao do calendario vacinal.', 'A',
     CURRENT_TIMESTAMP + INTERVAL '10' DAY,
     (SELECT pet_id FROM PETS WHERE pet_nome = 'Rex'),
     (SELECT funcionario_id FROM FUNCIONARIOS WHERE funcionario_email = 'helena@clyvovet.com.br'));

INSERT INTO CONSULTAS (consulta_tipo, consulta_valor, consulta_descricao, consulta_status, consulta_data, pet_id, funcionario_id) VALUES
    ('Primeira Consulta', 150.00, 'Avaliacao inicial de filhote. Iniciado protocolo vacinal.', 'R',
     CURRENT_TIMESTAMP - INTERVAL '60' DAY,
     (SELECT pet_id FROM PETS WHERE pet_nome = 'Mel'),
     (SELECT funcionario_id FROM FUNCIONARIOS WHERE funcionario_email = 'rafael@clyvovet.com.br'));

-- ---------------------------------------------------------------------
-- Prescricoes
-- ---------------------------------------------------------------------
INSERT INTO CONSULTAS_MEDICAMENTOS (consulta_id, medicamento_id, cm_dosagem) VALUES
    ((SELECT consulta_id FROM CONSULTAS WHERE consulta_tipo = 'Rotina'),
     (SELECT medicamento_id FROM MEDICAMENTOS WHERE medicamento_nome = 'Vermifugo Drontal Plus'),
     '1 comprimido a cada 10kg, dose unica');
INSERT INTO CONSULTAS_MEDICAMENTOS (consulta_id, medicamento_id, cm_dosagem) VALUES
    ((SELECT consulta_id FROM CONSULTAS WHERE consulta_tipo = 'Primeira Consulta'),
     (SELECT medicamento_id FROM MEDICAMENTOS WHERE medicamento_nome = 'Anti-inflamatorio Meloxicam'),
     '0,1 mg/kg via oral, 1x ao dia por 3 dias');
