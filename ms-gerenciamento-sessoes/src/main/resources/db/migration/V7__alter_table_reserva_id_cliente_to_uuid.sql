-- Alterar o tipo da coluna id_cliente de INT para CHAR(36) para suportar UUID
ALTER TABLE RESERVA MODIFY COLUMN id_cliente CHAR(36) NOT NULL;


