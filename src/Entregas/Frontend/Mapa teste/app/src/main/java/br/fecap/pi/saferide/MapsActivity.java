package br.fecap.pi.saferide;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import br.fecap.pi.saferide.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import br.fecap.pi.saferide.databinding.ActivityMapsBinding;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private AppCompatButton btn_menu;
    private int userId;

    List<Location> savedLocations;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        userId = getIntent().getIntExtra("USER_ID", 0);
        if (userId == 0) {
            Log.e("MAPS", "Erro: ID do usuário não recebido");
            Toast.makeText(this, "Erro: Sessão inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use o userId conforme necessário
        Log.d("MapsActivity", "ID do usuário: " + userId);




        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Pega o SupportMapFragment e notifica quando o mapa está pronto para uso.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        // Botão para abrir o menu
        btn_menu = findViewById(R.id.btn_menu);
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Adiciona um ping em São Paulo
        LatLng sp = new LatLng(-23.5489, -46.6388);
       // mMap.addMarker(new MarkerOptions().position(sp).title("Ping em São Paulo"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sp));

        LatLng lastlocationPlaced = sp;

        for (Location location: savedLocations
        ){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Lat:" + location.getLatitude()+" Lon:"+ location.getLongitude());
            mMap.addMarker(markerOptions);
            lastlocationPlaced = latLng;
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastlocationPlaced, 12.0f));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                //Conta quantas vezes o ping foi clicado
                Integer clicks = (Integer) marker.getTag();
                if (clicks == null){
                    clicks = 0;
                }
                clicks++;
                marker.setTag(clicks);
                Toast.makeText(MapsActivity.this, "Ping"+ marker.getTitle() + "foi clicado"+ marker.getTag() + "vezes.", Toast.LENGTH_SHORT).show();

                return false;
            }
        });
    }
}