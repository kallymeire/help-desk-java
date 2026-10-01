-- =====================================================================
--  Sistema de Chamados Help Desk - Projeto Integrador (Etapa 4)
--  Banco de dados MySQL: estrutura das tabelas e dados iniciais para teste
--  Aluna: Kallymeire Coelho - Técnico em Desenvolvimento de Sistemas
--
--  COMO USAR NO MYSQL WORKBENCH
--   1) Conecte-se ao servidor (File > New Query Tab ou abra este arquivo em File > Open SQL Script).
--   2) Execute o script inteiro (ícone do raio). Ele recria o banco "helpdesk" do zero.
--   3) Ajuste usuário e senha em config/banco.properties, se forem diferentes de root / (vazio).
--   Para ver o diagrama: Database > Reverse Engineer... e escolha o esquema "helpdesk".
--
--  Usuários de teste (senha de todos: 123456)
--   gestor@empresa.com (Gestor) | ana.ribeiro@empresa.com (Técnica N2) | carlos.lima@empresa.com (Técnico N1)
--   maria.souza@empresa.com (Cliente)
-- =====================================================================

DROP DATABASE IF EXISTS helpdesk;
CREATE DATABASE helpdesk DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE helpdesk;

-- Dados comuns de todas as pessoas (generalização: cliente, técnico ou gestor)
CREATE TABLE pessoa (
    id          INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo        ENUM('CLIENTE','TECNICO','GESTOR') NOT NULL,
    nome        VARCHAR(100) NOT NULL,
    email       VARCHAR(120) NOT NULL,
    senha_hash  CHAR(64) NOT NULL,
    CONSTRAINT uq_pessoa_email UNIQUE (email)
) ENGINE=InnoDB;

-- Especialização: dados exclusivos do cliente
CREATE TABLE cliente (
    pessoa_id   INT NOT NULL PRIMARY KEY,
    setor       VARCHAR(60) NOT NULL DEFAULT '',
    telefone    VARCHAR(20) NOT NULL DEFAULT '',
    CONSTRAINT fk_cliente_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Especialização: dados exclusivos do técnico (nível 1, 2 ou 3)
CREATE TABLE tecnico (
    pessoa_id      INT NOT NULL PRIMARY KEY,
    nivel          TINYINT NOT NULL,
    especialidade  VARCHAR(60) NOT NULL DEFAULT '',
    CONSTRAINT ck_tecnico_nivel CHECK (nivel BETWEEN 1 AND 3),
    CONSTRAINT fk_tecnico_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE chamado (
    id               INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    titulo           VARCHAR(120) NOT NULL,
    descricao        TEXT NOT NULL,
    prioridade       ENUM('BAIXA','MEDIA','ALTA','CRITICA') NOT NULL,
    status           ENUM('ABERTO','EM_ATENDIMENTO','AGUARDANDO_CLIENTE','RESOLVIDO','FECHADO') NOT NULL DEFAULT 'ABERTO',
    data_abertura    DATETIME NOT NULL,
    data_fechamento  DATETIME NULL,
    cliente_id       INT NOT NULL,
    tecnico_id       INT NULL,
    CONSTRAINT fk_chamado_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (pessoa_id),
    CONSTRAINT fk_chamado_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnico (pessoa_id)
) ENGINE=InnoDB;

CREATE TABLE interacao (
    id          INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chamado_id  INT NOT NULL,
    autor_id    INT NOT NULL,
    texto       TEXT NOT NULL,
    data_hora   DATETIME NOT NULL,
    CONSTRAINT fk_interacao_chamado FOREIGN KEY (chamado_id) REFERENCES chamado (id) ON DELETE CASCADE,
    CONSTRAINT fk_interacao_autor FOREIGN KEY (autor_id) REFERENCES pessoa (id)
) ENGINE=InnoDB;

CREATE INDEX idx_chamado_status  ON chamado (status);
CREATE INDEX idx_chamado_cliente ON chamado (cliente_id);
CREATE INDEX idx_chamado_tecnico ON chamado (tecnico_id);
CREATE INDEX idx_interacao_chamado ON interacao (chamado_id);

-- ---------------------------------------------------------------------
-- Dados iniciais para teste (datas calculadas a partir do momento da execução)
-- ---------------------------------------------------------------------
SET @agora = NOW();

INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (1, 'GESTOR', 'Marcos Teixeira', 'gestor@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (2, 'TECNICO', 'Ana Ribeiro', 'ana.ribeiro@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (3, 'TECNICO', 'Carlos Lima', 'carlos.lima@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (4, 'TECNICO', 'Bruno Faria', 'bruno.faria@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (5, 'TECNICO', 'Lívia Costa', 'livia.costa@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (6, 'CLIENTE', 'Maria Souza', 'maria.souza@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (7, 'CLIENTE', 'João Pires', 'joao.pires@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (8, 'CLIENTE', 'Paula Dias', 'paula.dias@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (9, 'CLIENTE', 'Luiz Alves', 'luiz.alves@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (10, 'CLIENTE', 'Rita Melo', 'rita.melo@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO pessoa (id, tipo, nome, email, senha_hash) VALUES (11, 'CLIENTE', 'Caio Neves', 'caio.neves@empresa.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');

INSERT INTO tecnico (pessoa_id, nivel, especialidade) VALUES (2, 2, 'Redes');
INSERT INTO tecnico (pessoa_id, nivel, especialidade) VALUES (3, 1, 'Periféricos');
INSERT INTO tecnico (pessoa_id, nivel, especialidade) VALUES (4, 3, 'Servidores');
INSERT INTO tecnico (pessoa_id, nivel, especialidade) VALUES (5, 1, 'Suporte geral');
INSERT INTO cliente (pessoa_id, setor, telefone) VALUES (6, 'Financeiro', '(37) 99999-0000');
INSERT INTO cliente (pessoa_id, setor, telefone) VALUES (7, 'Comercial', '(37) 99999-1111');
INSERT INTO cliente (pessoa_id, setor, telefone) VALUES (8, 'RH', '(37) 99999-2222');
INSERT INTO cliente (pessoa_id, setor, telefone) VALUES (9, 'Logística', '(37) 99999-3333');
INSERT INTO cliente (pessoa_id, setor, telefone) VALUES (10, 'Compras', '(37) 99999-4444');
INSERT INTO cliente (pessoa_id, setor, telefone) VALUES (11, 'Diretoria', '(37) 99999-5555');

INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (1, 'Lentidão no sistema', 'O sistema interno está muito lento desde ontem.', 'ALTA', 'FECHADO', DATE_SUB(@agora, INTERVAL 96 HOUR), DATE_SUB(@agora, INTERVAL 90 HOUR), 11, 2);
INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (2, 'Troca de teclado', 'Algumas teclas do teclado pararam de funcionar.', 'BAIXA', 'FECHADO', DATE_SUB(@agora, INTERVAL 30 HOUR), DATE_SUB(@agora, INTERVAL 2 HOUR), 10, 3);
INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (3, 'Acesso ao sistema de folha', 'Não consigo entrar no sistema de folha de pagamento.', 'MEDIA', 'FECHADO', DATE_SUB(@agora, INTERVAL 20 HOUR), DATE_SUB(@agora, INTERVAL 1 HOUR), 8, 4);
INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (4, 'Solicitação de acesso VPN', 'Preciso de acesso à VPN para trabalhar de casa.', 'ALTA', 'RESOLVIDO', DATE_SUB(@agora, INTERVAL 5 HOUR), NULL, 9, 2);
INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (5, 'Erro ao abrir planilha', 'A planilha de férias mostra erro ao abrir.', 'BAIXA', 'AGUARDANDO_CLIENTE', DATE_SUB(@agora, INTERVAL 12 HOUR), NULL, 8, 3);
INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (6, 'Impressora não imprime', 'A impressora do comercial não imprime desde cedo.', 'MEDIA', 'ABERTO', DATE_SUB(@agora, INTERVAL 8 HOUR), NULL, 7, NULL);
INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (7, 'Sem acesso à rede', 'O computador do financeiro não conecta na rede interna desde cedo.', 'CRITICA', 'EM_ATENDIMENTO', DATE_SUB(@agora, INTERVAL 6 HOUR), NULL, 6, 2);
INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (8, 'Servidor de arquivos indisponível', 'Não consigo acessar as pastas compartilhadas.', 'ALTA', 'EM_ATENDIMENTO', DATE_SUB(@agora, INTERVAL 3 HOUR), NULL, 9, 4);
INSERT INTO chamado (id, titulo, descricao, prioridade, status, data_abertura, data_fechamento, cliente_id, tecnico_id) VALUES (9, 'Instalar leitor de PDF', 'Preciso do leitor de PDF instalado no meu computador.', 'BAIXA', 'ABERTO', DATE_SUB(@agora, INTERVAL 1 HOUR), NULL, 6, NULL);

INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (1, 11, 'Chamado aberto: Lentidão no sistema.', DATE_ADD(DATE_SUB(@agora, INTERVAL 96 HOUR), INTERVAL 0 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (1, 2, 'Limpeza de cache e reinício do serviço realizados.', DATE_ADD(DATE_SUB(@agora, INTERVAL 96 HOUR), INTERVAL 20 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (2, 10, 'Chamado aberto: Troca de teclado.', DATE_ADD(DATE_SUB(@agora, INTERVAL 30 HOUR), INTERVAL 0 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (2, 3, 'Teclado substituído por um novo.', DATE_ADD(DATE_SUB(@agora, INTERVAL 30 HOUR), INTERVAL 20 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (3, 8, 'Chamado aberto: Acesso ao sistema de folha.', DATE_ADD(DATE_SUB(@agora, INTERVAL 20 HOUR), INTERVAL 0 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (3, 4, 'Permissão liberada no servidor de aplicações.', DATE_ADD(DATE_SUB(@agora, INTERVAL 20 HOUR), INTERVAL 20 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (4, 9, 'Chamado aberto: Solicitação de acesso VPN.', DATE_ADD(DATE_SUB(@agora, INTERVAL 5 HOUR), INTERVAL 0 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (4, 2, 'Acesso criado. Envie o print da conexão para confirmar.', DATE_ADD(DATE_SUB(@agora, INTERVAL 5 HOUR), INTERVAL 20 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (5, 8, 'Chamado aberto: Erro ao abrir planilha.', DATE_ADD(DATE_SUB(@agora, INTERVAL 12 HOUR), INTERVAL 0 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (5, 3, 'Pode enviar uma cópia do arquivo para eu testar?', DATE_ADD(DATE_SUB(@agora, INTERVAL 12 HOUR), INTERVAL 20 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (6, 7, 'Chamado aberto: Impressora não imprime.', DATE_ADD(DATE_SUB(@agora, INTERVAL 8 HOUR), INTERVAL 0 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (7, 6, 'Chamado aberto: Sem acesso à rede.', DATE_ADD(DATE_SUB(@agora, INTERVAL 6 HOUR), INTERVAL 0 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (7, 2, 'Cabo e porta do switch verificados.', DATE_ADD(DATE_SUB(@agora, INTERVAL 6 HOUR), INTERVAL 20 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (8, 9, 'Chamado aberto: Servidor de arquivos indisponível.', DATE_ADD(DATE_SUB(@agora, INTERVAL 3 HOUR), INTERVAL 0 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (8, 4, 'Serviço de compartilhamento reiniciado; monitorando.', DATE_ADD(DATE_SUB(@agora, INTERVAL 3 HOUR), INTERVAL 20 MINUTE));
INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (9, 6, 'Chamado aberto: Instalar leitor de PDF.', DATE_ADD(DATE_SUB(@agora, INTERVAL 1 HOUR), INTERVAL 0 MINUTE));
