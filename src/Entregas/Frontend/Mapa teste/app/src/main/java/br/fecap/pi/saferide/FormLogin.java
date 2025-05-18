package br.fecap.pi.saferide;

import static br.fecap.pi.saferide.ApiService.Criptografia.encryptForServer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import br.fecap.pi.saferide.ApiService.ApiService;
import br.fecap.pi.saferide.ApiMap.IDUsuario;
import br.fecap.pi.saferide.ApiService.RespostaLogin;


import br.fecap.pi.saferide.ApiService.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;

public class FormLogin extends AppCompatActivity {

    private static final int PERMISSIONS_FINE_LOCATION = 100;

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
        checkLocationPermission();
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
        apiService = RetrofitClient.getApiService();
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
                            IDUsuario.setUserId(resposta.getUsuario().getIDUsuario());
                            navigateToMaps();
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

    // 7. Tratamento da Resposta
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

    // 8. Métodos Auxiliares
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
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // 9. Redirecionamento para Cadastro
    private void setupCadastroRedirect() {
        textTelaCadastro.setOnClickListener(v -> {
            startActivity(new Intent(this, FormCadastro.class));
        });
    }

    // Metodo para pedir permissão de GPS

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de localização é necessária para o funcionamento do app", Toast.LENGTH_LONG).show();
            }
        }
    }

}

