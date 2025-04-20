package com.example.mapa;
public class Usuario {
    private String nome;
    private int idade;
    private String email;

    // Construtor
    public Usuario(String nome, int idade, String email) {
        this.nome = nome;
        this.idade = idade;
        this.email = email;
    }

    // Getters
    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public String getEmail() { return email; }
}