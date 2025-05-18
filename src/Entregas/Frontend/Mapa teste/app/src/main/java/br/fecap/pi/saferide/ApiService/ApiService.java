package br.fecap.pi.saferide.ApiService;

import java.util.List;

import br.fecap.pi.saferide.ApiMap.Localizacao;
import br.fecap.pi.saferide.ApiMap.RespostaLocalizacoes;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    // Endpoint GET para buscar todos os usuários
    @GET("usuario/tudo")
    Call<List<Usuario>> getTodosUsuarios();

    // Endpoint POST para adicionar novo usuário
    @POST("usuario/cadastro")
    Call<RespostaServidor> adicionarUsuario(@Body Usuario usuario);

    @FormUrlEncoded
    @POST("login")
    Call<RespostaLogin> login(
            @Field("email") String email,
            @Field("senha") String senha
    );

    @POST("localizacao/salvar")
    Call<RespostaServidor> salvarLocalizacao(@Body Localizacao localizacao);

    @GET("localizacao/tudo")
    Call<RespostaLocalizacoes> buscarTodasLocalizacoes();

    // Caso queira buscar por ID
//    @GET("localizacao/{IDUsuario}")
//    Call<RespostaLocalizacoes> buscarLocalizacoes(@Path("IDUsuario") int IDUsuario);

}