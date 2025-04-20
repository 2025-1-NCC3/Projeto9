package com.example.mapa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TesteBackEnd extends AppCompatActivity {

    private TextView resultadoConexao;
    private ProgressBar progressBar;
    private Button btnTeste, btnEnviarDados;
    private EditText editNome, editIdade, editEmail;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste_back_end);

        // Inicializar views com os IDs
        resultadoConexao = findViewById(R.id.resultadoConexao);
        progressBar = findViewById(R.id.progressBar);
        btnTeste = findViewById(R.id.btn_teste);
        btnEnviarDados = findViewById(R.id.btn_enviarDados);
        editNome = findViewById(R.id.edit_nome);
        editIdade = findViewById(R.id.edit_idade);
        editEmail = findViewById(R.id.edit_email);

        //TimeOut
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://536c6f70-9966-4de4-9a00-3a2c34980c77-00-3s47vp1qw0n15.spock.replit.dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Configurar clique dos botões
        btnTeste.setOnClickListener(v -> testarConexao());
        btnEnviarDados.setOnClickListener(v -> enviarDadosParaServidor());
    }


    // Testar a conexão
    private void testarConexao() {
        progressBar.setVisibility(View.VISIBLE);
        btnTeste.setEnabled(false);
        resultadoConexao.setText("Testando conexão...");

        Call<List<Usuario>> call = apiService.getTodosUsuarios();
        call.enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                progressBar.setVisibility(View.GONE);
                btnTeste.setEnabled(true);

                if (response.isSuccessful()) {
                    resultadoConexao.setText("Conexão bem-sucedida!");
                    Toast.makeText(TesteBackEnd.this, "Servidor online!", Toast.LENGTH_SHORT).show();
                } else {
                    resultadoConexao.setText("Erro na conexão");
                    Toast.makeText(TesteBackEnd.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnTeste.setEnabled(true);
                resultadoConexao.setText("Falha na conexão");
                Toast.makeText(TesteBackEnd.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarDadosParaServidor() {
        String nome = editNome.getText().toString().trim();
        String idadeStr = editIdade.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        if (nome.isEmpty() || idadeStr.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int idade;
        try {
            idade = Integer.parseInt(idadeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Idade deve ser um número", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnEnviarDados.setEnabled(false);

        Usuario novoUsuario = new Usuario(nome, idade, email);
        Call<RespostaServidor> call = apiService.adicionarUsuario(novoUsuario);
        call.enqueue(new Callback<RespostaServidor>() {
            @Override
            public void onResponse(Call<RespostaServidor> call, Response<RespostaServidor> response) {
                progressBar.setVisibility(View.GONE);
                btnEnviarDados.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    RespostaServidor resposta = response.body();
                    resultadoConexao.setText("Usuário cadastrado com sucesso!");
                    Toast.makeText(TesteBackEnd.this, "ID: " + resposta.getId(), Toast.LENGTH_SHORT).show();
                    editNome.setText("");
                    editIdade.setText("");
                    editEmail.setText("");
                } else {
                    resultadoConexao.setText("Erro no cadastro");
                    Toast.makeText(TesteBackEnd.this, "Erro ao cadastrar usuário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespostaServidor> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnEnviarDados.setEnabled(true);
                resultadoConexao.setText("Falha na conexão");
                Toast.makeText(TesteBackEnd.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}