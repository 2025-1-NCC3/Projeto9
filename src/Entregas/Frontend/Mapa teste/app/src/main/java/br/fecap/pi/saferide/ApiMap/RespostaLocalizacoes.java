package br.fecap.pi.saferide.ApiMap;


import java.util.List;

public class RespostaLocalizacoes {
    private boolean sucesso;
    private List<Localizacao> localizacoes;

    // Getters e Setters
    public boolean isSucesso() {
        return sucesso;
    }

    public List<Localizacao> getLocalizacoes() {
        return localizacoes;
    }
}