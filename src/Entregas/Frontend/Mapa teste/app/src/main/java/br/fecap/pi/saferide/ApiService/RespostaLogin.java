package br.fecap.pi.saferide.ApiService;

public class RespostaLogin {
    private boolean sucesso;
    private String mensagem;
    private Usuario usuario;

    // Getters
    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
