package br.fecap.pi.saferide;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // Endpoint GET para buscar todos os usuários
    @GET("tudo")
    Call<List<Usuario>> getTodosUsuarios();

    // Endpoint POST para adicionar novo usuário
    @POST("add")
    Call<RespostaServidor> adicionarUsuario(@Body Usuario usuario);
}
