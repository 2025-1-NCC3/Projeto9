package br.fecap.pi.saferide;

import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import br.fecap.pi.saferide.ApiMap.IDUsuario;
import br.fecap.pi.saferide.ApiMap.MyApplication;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import br.fecap.pi.saferide.databinding.ActivityMapsBinding;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // 1. Componentes de UI
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private AppCompatButton btn_menu;

    // 2. Dados
    private List<Location> savedLocations;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializa o FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 1. Verificação de autenticação
        checkUserAuthentication();

        // 2. Inicialização de Views
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3. Configuração do Mapa
        setupMapFragment();

        // 4. Configuração de Listeners
        setupButtonListeners();

        // 5. Carregar localizações
        loadSavedLocations();

        // Configura o comportamento do botão de voltar
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToLogin();
            }
        });
    }

    // --- Métodos de Inicialização ---

    // 1. Verifica autenticação do usuário
    private void checkUserAuthentication() {
        userId = IDUsuario.getUserId();
        if (userId == 0) {
            Log.e("MAPS", "Erro: ID do usuário não recebido");
            Toast.makeText(this, "Erro: Sessão inválida", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 2. Configura fragmento do mapa
    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // 3. Configura listeners de botões
    private void setupButtonListeners() {
        btn_menu = findViewById(R.id.btn_menu);
        btn_menu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    // 4. Carrega localizações salvas
    private void loadSavedLocations() {
        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();
        Log.d("MapsActivity", "Total de pings carregados: " + (savedLocations != null ? savedLocations.size() : 0));
    }

    // --- Métodos do Mapa ---

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 1. Verifica permissão de localização
        if (checkLocationPermission()) {
            // 2. Obtém a localização atual
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }

        // 3. Mostra todos os pings salvos
        showSavedPings();
    }

    // Método para verificar permissão
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Método para solicitar permissão
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Método para obter localização atual
    private void getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Centraliza o mapa na localização atual
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));

                            // Adiciona marcador da posição atual (opcional)
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLatLng)
                                    .title("Você está aqui"));
                        } else {
                            showToast("Não foi possível obter a localização atual");
                            showDefaultLocation();
                        }
                    });
        }
    }

    // Método para mostrar pings salvos
    private void showSavedPings() {
        if (savedLocations != null && !savedLocations.isEmpty()) {
            Log.d("MapsActivity", "Exibindo " + savedLocations.size() + " pings");
            for (int i = 0; i < savedLocations.size(); i++) {
                Location location = savedLocations.get(i);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = createMarkerOptions(latLng, location)
                        .title("Ping #" + (i+1));
                mMap.addMarker(markerOptions);
                Log.d("MapsActivity", "Adicionado ping: " + latLng.toString());
            }
        } else {
            Log.d("MapsActivity", "Nenhum ping para exibir");
        }
    }

    // Método para mostrar localização padrão (caso falhe obter a atual)
    private void showDefaultLocation() {
        LatLng defaultLocation = new LatLng(-23.5489, -46.6388); // São Paulo
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                showToast("Permissão de localização negada - mostrando localização padrão");
                showDefaultLocation();
            }
        }
    }

    // Cria MarkerOptions para uma localização
    private MarkerOptions createMarkerOptions(LatLng latLng, Location location) {
        return new MarkerOptions()
                .position(latLng)
                .title("Lat:" + location.getLatitude() + " Lon:" + location.getLongitude());
    }

    // Configura listener de clique nos marcadores
    private void setupMarkerClickListener() {
        mMap.setOnMarkerClickListener(marker -> {
            // Conta cliques no marcador
            Integer clicks = (Integer) marker.getTag();
            clicks = (clicks == null) ? 1 : clicks + 1;
            marker.setTag(clicks);

            showToast("Ping " + marker.getTitle() + " foi clicado " + clicks + " vezes.");
            return false;
        });
    }

    // Mostra Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Voltar para o menu
    protected void returnToLogin() {
        Intent intent = new Intent(this, FormLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}