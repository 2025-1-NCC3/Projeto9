package br.fecap.pi.saferide.ApiService;

public class Usuario {
    private int IDUsuario;  // Novo campo
    private String nome;
    private String email;
    private String senha;

    // Construtor para cadastro (sem ID)
    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // Construtor completo (para respostas do servidor)
    public Usuario(int IDUsuario, String nome, String email, String senha) {
        this.IDUsuario = IDUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // Getters
    public int getIDUsuario() { return IDUsuario; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }

    // Método para descriptografar os dados recebidos do servidor
    public static Usuario decryptFromServer(Usuario encrypted) {
        int id = encrypted.getIDUsuario(); // ID não é criptografado
        String nome = Criptografia.decryptFromServer(encrypted.getNome());
        String email = Criptografia.decryptFromServer(encrypted.getEmail());
        String senha = Criptografia.decryptFromServer(encrypted.getSenha());
        return new Usuario(id, nome, email, senha);
    }
}