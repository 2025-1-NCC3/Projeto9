create database teste;

use teste;

create table teste_link(
teste_id int auto_increment primary key,
nome varchar(255),
idade varchar(3),
email varchar(255)
);

insert into teste_link (nome, idade, email) values ("Antônio", "19", "oliveira.antonio@edu.fecap.com.br"), ("Daniel", "18", "daniel.moribe@edu.fecap.com.br"), ("Thiago", "19", "thiago.akira@edu.fecap.com.br");

select * from teste_link;