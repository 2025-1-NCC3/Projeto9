package br.fecap.pi.saferide;

import static br.fecap.pi.saferide.ApiService.Criptografia.encryptForServer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import br.fecap.pi.saferide.ApiService.ApiService;
import br.fecap.pi.saferide.ApiService.RespostaLogin;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FormLogin extends AppCompatActivity {

    // Componentes de UI
    private EditText editLoginEmail, editLoginSenha;
    private AppCompatButton btnEntrar;
    private TextView textTelaCadastro;
    private ProgressBar progressBar;

    // Serviço de API
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);

        initViews();
        setupRetrofit();
        setupPasswordToggle();
        setupLoginButton();
        setupCadastroRedirect();
    }

    // 1. Inicialização de Views
    private void initViews() {
        editLoginEmail = findViewById(R.id.editLoginEmail);
        editLoginSenha = findViewById(R.id.editLoginSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        textTelaCadastro = findViewById(R.id.textTelaCadastro);
        progressBar = findViewById(R.id.progressBar);
    }

    // 2. Configuração Retrofit (Reutilizável)
    private void setupRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://qhq65s-8080.csb.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // 3. Toggle de Senha
    private void setupPasswordToggle() {
        editLoginSenha.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            Drawable drawable = editLoginSenha.getCompoundDrawables()[DRAWABLE_RIGHT];

            if (drawable != null && event.getAction() == MotionEvent.ACTION_UP &&
                    event.getRawX() >= (editLoginSenha.getRight() - drawable.getBounds().width())) {
                togglePasswordVisibility(editLoginSenha);
                return true;
            }
            return false;
        });
    }

    // 4. Configuração do Botão de Login
    private void setupLoginButton() {
        btnEntrar.setOnClickListener(v -> {
            String email = editLoginEmail.getText().toString().trim();
            String senha = editLoginSenha.getText().toString().trim();

            if (validateInputs(email, senha)) {
                attemptLogin(email, senha);
            }
        });
    }

    // 5. Validação de Campos
    private boolean validateInputs(String email, String senha) {
        if (email.isEmpty() || senha.isEmpty()) {
            showError("Preencha todos os campos");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Email inválido");
            return false;
        }

        return true;
    }

    // 6. Tentativa de Login
    private void attemptLogin(String email, String senha) {
        setLoadingState(true);

        String encryptedEmail = encryptForServer(email);
        String encryptedSenha = encryptForServer(senha);

        apiService.login(encryptedEmail, encryptedSenha).enqueue(new Callback<RespostaLogin>() {
            @Override
            public void onResponse(Call<RespostaLogin> call, Response<RespostaLogin> response) {
                setLoadingState(false);

                if (response.isSuccessful()) {
                    RespostaLogin resposta = response.body();
                    if (resposta != null) {
                        if (resposta.isSucesso()) {
                            navigateToMaps(resposta.getUsuario().getIDUsuario());
                        } else {
                            // Mensagem personalizada para credenciais inválidas
                            showError("E-mail ou senha incorretos. Tente novamente.");
                        }
                    }
                } else if (response.code() == 401) {
                    // Tratamento específico para erro 401
                    showError("E-mail ou senha incorretos. Tente novamente.");
                } else {
                    showError("Erro no servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RespostaLogin> call, Throwable t) {
                setLoadingState(false);
                showError("Falha na conexão: " + t.getMessage());
            }
        });
    }

    // 7. Ir para a próxima tela com o ID
    private void navigateToMaps(int userId) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("USER_ID", userId); // Adiciona o ID como extra
        startActivity(intent);
    }

    // 8. Tratamento da Resposta
    private void handleLoginResponse(Response<RespostaLogin> response) {
        if (response.isSuccessful() && response.body() != null) {
            RespostaLogin resposta = response.body();

            if (resposta.isSucesso()) {
                navigateToMaps();
            } else {
                showError(resposta.getMensagem() != null ? resposta.getMensagem() : "Credenciais inválidas");
            }
        } else {
            showError("Erro no servidor: " + response.code());
        }
    }

    // 9. Métodos Auxiliares
    private void togglePasswordVisibility(EditText editText) {
        boolean isPasswordVisible = (editText.getTransformationMethod() instanceof PasswordTransformationMethod);
        editText.setTransformationMethod(isPasswordVisible ?
                HideReturnsTransformationMethod.getInstance() :
                PasswordTransformationMethod.getInstance());
        editText.setSelection(editText.getText().length());
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnEntrar.setEnabled(!isLoading);
        textTelaCadastro.setEnabled(!isLoading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMaps() {
        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }

    // 9. Redirecionamento para Cadastro
    private void setupCadastroRedirect() {
        textTelaCadastro.setOnClickListener(v -> {
            startActivity(new Intent(this, FormCadastro.class));
        });
    }
}

