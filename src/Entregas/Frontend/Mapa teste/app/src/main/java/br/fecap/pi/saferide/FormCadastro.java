package br.fecap.pi.saferide;

import static br.fecap.pi.saferide.ApiService.Criptografia.encryptForServer;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.saferide.ApiService.ApiService;
import br.fecap.pi.saferide.ApiService.RespostaServidor;
import br.fecap.pi.saferide.ApiService.RetrofitClient;
import br.fecap.pi.saferide.ApiService.Usuario;

import java.util.List;



import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormCadastro extends AppCompatActivity {

    // Componentes de UI
    private ProgressBar progressBar;
    private Button btnTeste, btnCadastrar;
    private EditText editNome, editEmail, editSenha;

    // Serviço de API
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cadastro);

        initViews();
        setupRetrofit();
        setupPasswordToggle();
        setupButtonListeners();
    }

    // 1. Inicialização de Views
    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        btnTeste = findViewById(R.id.btnTeste);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
    }

    // 2. Configuração Retrofit
    private void setupRetrofit() {
        apiService = RetrofitClient.getApiService();
    }

    // 3. Toggle de Senha
    private void setupPasswordToggle() {
        editSenha.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawable = editSenha.getCompoundDrawables()[DRAWABLE_RIGHT];
                if(drawable != null && event.getRawX() >= (editSenha.getRight() - drawable.getBounds().width())) {
                    togglePasswordVisibility();
                    v.performClick(); // Acessibilidade
                    return true;
                }
            }
            return false;
        });
    }

    // 4. Listeners de Botões
    private void setupButtonListeners() {
        btnTeste.setOnClickListener(v -> testConnection());

        btnCadastrar.setOnClickListener(v -> {
            enviarDadosParaServidor(() -> {
                runOnUiThread(this::navigateToLogin);
            });
        });
    }

    // --- Métodos Auxiliares ---

    // Alternar visibilidade da senha
    private void togglePasswordVisibility() {
        boolean isPasswordVisible = (editSenha.getTransformationMethod() instanceof HideReturnsTransformationMethod);
        editSenha.setTransformationMethod(isPasswordVisible ?
                PasswordTransformationMethod.getInstance() :
                HideReturnsTransformationMethod.getInstance());

        editSenha.setSelection(editSenha.getText().length());
    }

    // Navegação para Login
    private void navigateToLogin() {
        Intent intent = new Intent(this, FormLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Testar a conexão
    private void testConnection() {
        progressBar.setVisibility(View.VISIBLE);
        btnTeste.setEnabled(false);

        Call<List<Usuario>> call = apiService.getTodosUsuarios();
        call.enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                progressBar.setVisibility(View.GONE);
                btnTeste.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(FormCadastro.this, "Servidor online!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FormCadastro.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnTeste.setEnabled(true);
                Toast.makeText(FormCadastro.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Envio de Dados
    private void enviarDadosParaServidor(Runnable onSuccessCallback) {
        // Validação
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (!validateInputs(nome, email, senha)) return;

        // Criptografia
        Usuario novoUsuario = new Usuario(
                encryptForServer(nome),
                encryptForServer(email),
                encryptForServer(senha)
        );

        // UI Loading State
        setLoadingState(true);

        // Chamada de Rede
        apiService.adicionarUsuario(novoUsuario).enqueue(new Callback<RespostaServidor>() {
            @Override
            public void onResponse(Call<RespostaServidor> call, Response<RespostaServidor> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessResponse(response.body(), onSuccessCallback);
                } else {
                    showError("Erro ao cadastrar usuário");
                }
            }

            @Override
            public void onFailure(Call<RespostaServidor> call, Throwable t) {
                setLoadingState(false);
                showError("Erro: " + t.getMessage());
            }
        });
    }

    // --- Métodos de Suporte ---

    private boolean validateInputs(String nome, String email, String senha) {
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            showError("Preencha todos os campos");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Digite um email válido");
            return false;
        }

        return true;
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnCadastrar.setEnabled(!isLoading);
    }

    private void handleSuccessResponse(RespostaServidor resposta, Runnable callback) {
        Toast.makeText(this, "ID: " + resposta.getId(), Toast.LENGTH_SHORT).show();
        clearForm();
        if (callback != null) callback.run();
    }

    private void clearForm() {
        editNome.setText("");
        editEmail.setText("");
        editSenha.setText("");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}