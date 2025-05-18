package br.fecap.pi.saferide.ApiMap;
import android.app.Application;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.fecap.pi.saferide.ApiService.ApiService;
import br.fecap.pi.saferide.ApiService.RetrofitClient;
import retrofit2.*;


public class MyApplication extends Application {

    private static MyApplication singleton;
    private List<Location> myLocations;
    private boolean locationsLoaded = false;

    public List<Location> getMyLocations() {
        if (!locationsLoaded) {
            loadAllLocationsFromServer();
            return new ArrayList<>(); // Retorna lista vazia enquanto carrega
        }
        return myLocations;
    }

    public void setMyLocations(List<Location> myLocations) {
        this.myLocations = myLocations;
        this.locationsLoaded = true;
    }

    public MyApplication getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        myLocations = new ArrayList<>();
        loadAllLocationsFromServer();
    }

    public void loadAllLocationsFromServer() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.buscarTodasLocalizacoes().enqueue(new Callback<RespostaLocalizacoes>() {
            @Override
            public void onResponse(Call<RespostaLocalizacoes> call, Response<RespostaLocalizacoes> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSucesso()) {
                    List<Location> locations = new ArrayList<>();
                    for (Localizacao loc : response.body().getLocalizacoes()) {
                        Location location = new Location("server");
                        location.setLatitude(loc.getLatitude());
                        location.setLongitude(loc.getLongitude());

                        if (loc.getAltitude() != null) {
                            location.setAltitude(loc.getAltitude());
                        }

                        if (loc.getAccuracy() != null) {
                            location.setAccuracy(loc.getAccuracy().floatValue());
                        }

                        if (loc.getSpeed() != null) {
                            location.setSpeed(loc.getSpeed().floatValue());
                        }

                        locations.add(location);
                    }
                    setMyLocations(locations);
                    Log.d("MyApplication", "Todas as localizações carregadas: " + locations.size());
                } else {
                    Log.e("MyApplication", "Erro ao carregar todas as localizações: " +
                            (response.body() == null ? "Resposta vazia" : response.code()));
                }
            }

            @Override
            public void onFailure(Call<RespostaLocalizacoes> call, Throwable t) {
                Log.e("MyApplication", "Falha na conexão ao buscar todas as localizações: " + t.getMessage());
            }
        });
    }



    // Caso queira buscar por ID
//    private void loadLocationsFromServer() {
//        int userId = IDUsuario.getUserId();
//        if (userId == 0) {
//            Log.e("MyApplication", "Usuário não autenticado");
//            return;
//        }
//
//        ApiService apiService = RetrofitClient.getApiService();
//        apiService.buscarLocalizacoes(userId).enqueue(new Callback<RespostaLocalizacoes>() {
//            @Override
//            public void onResponse(Call<RespostaLocalizacoes> call, Response<RespostaLocalizacoes> response) {
//                if (response.isSuccessful() && response.body() != null && response.body().isSucesso()) {
//                    List<Location> locations = new ArrayList<>();
//                    for (Localizacao loc : response.body().getLocalizacoes()) {
//                        Location location = new Location("server");
//                        location.setLatitude(loc.getLatitude());
//                        location.setLongitude(loc.getLongitude());
//
//                        // Verifique se os valores estão presentes antes de definir
//                        if (loc.getAltitude() != null) {
//                            location.setAltitude(loc.getAltitude());
//                        }
//
//                        if (loc.getAccuracy() != null) {
//                            location.setAccuracy(loc.getAccuracy().floatValue());
//                        }
//
//                        if (loc.getSpeed() != null) {
//                            location.setSpeed(loc.getSpeed().floatValue());
//                        }
//
//                        locations.add(location);
//                    }
//                    setMyLocations(locations);
//                    Log.d("MyApplication", "Localizações carregadas: " + locations.size());
//                } else {
//                    Log.e("MyApplication", "Erro ao carregar localizações: " + (response.body() == null ? "Resposta vazia" : response.code()));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<RespostaLocalizacoes> call, Throwable t) {
//                Log.e("MyApplication", "Falha na conexão: " + t.getMessage());
//            }
//        });
//    }
}

