CREATE TABLE reserva(
    id INT(10) AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT(10) NOT NULL,
    id_sessao INT(10) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    pagamento_confirmado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_reserva_sessao FOREIGN KEY (id_sessao) REFERENCES sessoes(id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;