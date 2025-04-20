package com.example.mapa;
public class Usuario {
    private String nome;
    private int idade;
    private String email;

    public Usuario(String nome, int idade, String email) {
        this.nome = nome;
        this.idade = idade;
        this.email = email;
    }

    // Getters
    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public String getEmail() { return email; }

    // Metodo para descriptografar os dados recebidos do servidor
    public static Usuario decryptFromServer(Usuario encrypted) {
        String nome = Criptografia.decryptFromServer(encrypted.getNome());
        String email = Criptografia.decryptFromServer(encrypted.getEmail());
        return new Usuario(nome, encrypted.getIdade(), email);
    }
}