package com.example.mapa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mapa.R;

public class FormLogin extends AppCompatActivity {

    private TextView text_tela_cadastro;
    private AppCompatButton btn_entrar, btn_teste;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//      Iniciar o componente
        text_tela_cadastro = findViewById(R.id.text_tela_cadastro);
        btn_entrar = findViewById(R.id.btn_entrar);
        btn_teste = findViewById(R.id.btn_teste);

//      Fazer o TextView aceitar cliques, virar um botão
        text_tela_cadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FormLogin.this, FormCadastro.class);
                startActivity(intent);
            }
        });

        btn_entrar.setOnClickListener(view -> {
            Intent intent = new Intent(FormLogin.this, MapsActivity.class);
            startActivity(intent);
        });

        btn_teste.setOnClickListener(view -> {
            Intent intent = new Intent(FormLogin.this, TesteBackEnd.class);
            startActivity(intent);
        });




    }
}