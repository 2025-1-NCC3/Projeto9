package br.fecap.pi.saferide;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import br.fecap.pi.saferide.ApiMap.MyApplication;

import java.util.List;

public class ShowSavedLocationsList extends AppCompatActivity {
    // 1. Componentes de UI
    private ListView lv_savedLocations;
    private AppCompatButton btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list);

        // 2. Inicialização de Views
        initViews();

        // 3. Carregar e exibir localizações
        loadAndDisplayLocations();
        backButton();
    }

    // --- Métodos Principais ---

    // 1. Inicializa views
    private void initViews() {
        lv_savedLocations = findViewById(R.id.lv_wayPoints);
        btn_back = findViewById(R.id.btn_back);

    }

    // 2. Carrega e exibe localizações
    private void loadAndDisplayLocations() {
        MyApplication myApplication = (MyApplication) getApplicationContext();
        List<Location> savedLocations = myApplication.getMyLocations();

        // Configura adapter para a ListView
        lv_savedLocations.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                savedLocations
        ));
    }

    private void navigateToMenu() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void backButton(){
        btn_back.setOnClickListener(v -> {
            navigateToMenu();
        });
    }


}