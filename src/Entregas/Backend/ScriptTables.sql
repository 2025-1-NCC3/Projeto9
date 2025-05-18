create database uberSafeRide;

use uberSafeRide;

CREATE TABLE usuario (
    IDUsuario INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(100) NOT NULL
);


select * from usuario;	
    
CREATE TABLE localizacoes (
    IDLocalizacao INT AUTO_INCREMENT PRIMARY KEY,
    IDUsuario INT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    altitude DECIMAL(10, 2),
    accuracy DECIMAL(10, 2),
    speed DECIMAL(10, 2),
    endereco TEXT,
    dataHora DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IDUsuario) REFERENCES usuario(IDUsuario)
);

select * from localizacoes;	
