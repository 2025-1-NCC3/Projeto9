package br.fecap.pi.saferide;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import br.fecap.pi.saferide.ApiService.ApiService;
import br.fecap.pi.saferide.ApiMap.IDUsuario;
import br.fecap.pi.saferide.ApiMap.Localizacao;
import br.fecap.pi.saferide.ApiMap.MyApplication;
import br.fecap.pi.saferide.ApiService.RespostaServidor;
import br.fecap.pi.saferide.ApiService.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 1. Constantes e Variáveis
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private static int FAST_UPDATE_INTERVAL = 5;
    private static int DEFAULT_UPDATE_INTERVAL = 30;

    // Componentes de UI
    private TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_wayPointCounts;
    private Button btn_newWayPoint, btn_showWaypointList, btn_showMap;
    private Switch sw_locationupdates, sw_gps;

    // Estado e Dados
    private boolean updateOn = false;
    private Location currentLocation;
    private List<Location> savedLocations;

    // Serviços de Localização
    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Inicialização de Views
        initViews();

        // 3. Configuração de Serviços
        setupLocationServices();

        // 4. Configuração de Listeners
        setupButtonListeners();
        setupSwitchListeners();
    }

    // --- Métodos de Inicialização ---

    // 1. Inicialização de Views
    private void initViews() {
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWaypointList = findViewById(R.id.btn_showWayPointList);
        btn_showMap = findViewById(R.id.btn_showMap);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumbs);
    }

    // 2. Configuração de Serviços de Localização
    private void setupLocationServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Configura LocationRequest
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);  // Alta precisão por padrão

        // Configura LocationCallback
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        updateUIValues(location);
                        currentLocation = location;
                    }
                }
            }
        };

        // Inicia atualizações automaticamente
        startLocationUpdates();
    }

    // 3. Configuração de Listeners
    private void setupButtonListeners() {
        // Botão para mostrar lista de localizações salvas
        btn_showWaypointList.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ShowSavedLocationsList.class));
        });

        // Botão para adicionar novo waypoint
        btn_newWayPoint.setOnClickListener(v -> {
            if (currentLocation != null) {
                addNewWaypoint(currentLocation);
            } else {
                showToast("Localização não disponível");
            }
        });

        // Botão para mostrar mapa
        btn_showMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });
    }

    private void setupSwitchListeners() {
        // Switch para GPS de alta precisão
        sw_gps.setOnClickListener(v -> {
            if (sw_gps.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                tv_sensor.setText("Usando sensores do GPS");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                tv_sensor.setText("Usando Torres + WIFI");
            }
            // Reinicia atualizações com nova prioridade
            startLocationUpdates();
        });
    }

    // --- Métodos de Localização ---

    // Inicia atualizações de localização
    private void startLocationUpdates() {
        if (checkLocationPermission()) {
            tv_updates.setText("Localização está sendo rastreada");
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
            updateGPS();
        }
    }

    // Para atualizações de localização
    private void stopLocationUpdates() {
        tv_updates.setText("Localização não está sendo rastreada");
        clearLocationUI();
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    // Atualiza dados do GPS
    private void updateGPS() {
        if (checkLocationPermission()) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    updateUIValues(location);
                    currentLocation = location;
                }
            });
        } else {
            requestLocationPermission();
        }
    }

    // --- Métodos Auxiliares ---

    // Adiciona novo waypoint
    private void addNewWaypoint(Location location) {
        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();
        savedLocations.add(location);
        salvarLocalizacaoNoServidor(location);
    }

    // Salva localização no servidor
    private void salvarLocalizacaoNoServidor(Location location) {
        int userId = IDUsuario.getUserId();
        if (userId == 0) {
            showToast("Usuário não autenticado");
            return;
        }

        Localizacao localizacao = createLocalizacaoFromLocation(userId, location);
        ApiService apiService = RetrofitClient.getApiService();

        apiService.salvarLocalizacao(localizacao).enqueue(new Callback<RespostaServidor>() {
            @Override
            public void onResponse(Call<RespostaServidor> call, Response<RespostaServidor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MainActivity", "Localização salva. ID: " + response.body().getId());
                }
            }

            @Override
            public void onFailure(Call<RespostaServidor> call, Throwable t) {
                Log.e("MainActivity", "Falha na conexão: " + t.getMessage());
            }
        });
    }

    // Cria objeto Localizacao a partir de Location
    private Localizacao createLocalizacaoFromLocation(int userId, Location location) {
        Localizacao localizacao = new Localizacao();
        localizacao.setIDUsuario(userId);
        localizacao.setLatitude(location.getLatitude());
        localizacao.setLongitude(location.getLongitude());

        if (location.hasAltitude()) {
            localizacao.setAltitude(location.getAltitude());
        }
        if (location.hasAccuracy()) {
            localizacao.setAccuracy((double) location.getAccuracy());
        }
        if (location.hasSpeed()) {
            localizacao.setSpeed((double) location.getSpeed());
        }

        try {
            List<Address> addresses = new Geocoder(this).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                localizacao.setEndereco(addresses.get(0).getAddressLine(0));
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao obter endereço: " + e.getMessage());
        }

        return localizacao;
    }

    // Atualiza UI com valores de localização
    private void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        tv_altitude.setText(location.hasAltitude() ? String.valueOf(location.getAltitude()) : "Não Disponível");
        tv_speed.setText(location.hasSpeed() ? String.valueOf(location.getSpeed()) : "Não Disponível");

        updateAddress(location);
        updateWaypointCount();
    }

    // Atualiza endereço na UI
    private void updateAddress(Location location) {
        new Thread(() -> {
            try {
                List<Address> addresses = new Geocoder(this).getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1);

                if (addresses != null && !addresses.isEmpty()) {
                    runOnUiThread(() -> {
                        tv_address.setText(addresses.get(0).getAddressLine(0));
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tv_address.setText("Endereço indisponível");
                });
            }
        }).start();
    }

    // Atualiza contador de waypoints
    private void updateWaypointCount() {
        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();
        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));
    }

    // Limpa UI de localização
    private void clearLocationUI() {
        tv_lat.setText("Não está rastreando a localização");
        tv_lon.setText("Não está rastreando a localização");
        tv_speed.setText("Não está rastreando a localização");
        tv_address.setText("Não está rastreando a localização");
        tv_accuracy.setText("Não está rastreando a localização");
        tv_altitude.setText("Não está rastreando a localização");
        tv_sensor.setText("Não está rastreando a localização");
    }

    // Verifica permissão de localização
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Solicita permissão de localização
    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    // Mostra Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                showToast("Permissão necessária para funcionar");
                finish();
            }
        }
    }
}