package br.fecap.pi.saferide.ApiService;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // Endpoint GET para buscar todos os usuários
    @GET("tudo")
    Call<List<Usuario>> getTodosUsuarios();

    // Endpoint POST para adicionar novo usuário
    @POST("cadastro")
    Call<RespostaServidor> adicionarUsuario(@Body Usuario usuario);

    @FormUrlEncoded
    @POST("login")
    Call<RespostaLogin> login(
            @Field("email") String email,
            @Field("senha") String senha
    );
}