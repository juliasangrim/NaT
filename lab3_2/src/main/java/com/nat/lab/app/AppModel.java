package com.nat.lab.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class AppModel {
    private final static String keyOpenTrip = "5ae2e3f221c38a28845f05b6785bf6730720aa3e439523cf2e656370";
    private final static String keyGraphHopper = "a46d7782-ff0c-41d1-b414-0fa88b7659d4";
    private final static String keyOpenWeather = "45029ff16520897b0b55862830db823d";
    private final static String urlOpenTrip = "https://api.opentripmap.com/0.1/en/places/";
    private final static String urlGraphHopper = "https://graphhopper.com/api/1/geocode";
    private final static String urlOpenWeather = "https://api.openweathermap.org/data/2.5/weather";

    @Setter
    private String placeName;
    @Getter
    private CopyOnWriteArrayList<Place> listPlaces;
    @Getter
    private CompletableFuture<CopyOnWriteArrayList<PlaceInfo>> listNearbyPlaces;
    @Getter
    private CompletableFuture<WeatherInfo> weather;
    @Getter
    State state;
    private boolean ifSearchError;


    private HttpResponse<String> body(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            ifSearchError = true;
        }
        return response;
    }

    private String getSearchResponse() throws ExecutionException, InterruptedException, UnsupportedEncodingException {
        var client = HttpClient.newHttpClient();
        var placeNameUTF = URLEncoder.encode(placeName, StandardCharsets.UTF_8.toString());
        var requestURIString = String.format("%s?q=%s&key=%s", urlGraphHopper, placeNameUTF, keyGraphHopper);
        var request = HttpRequest.newBuilder(
                        URI.create(requestURIString))
                .header("accept", "application/json")
                .build();
        System.out.println(requestURIString);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::body).thenApply(HttpResponse::body).get();
    }


    private void setPlaces() throws JsonProcessingException, ExecutionException, InterruptedException, UnsupportedEncodingException, ModelException {
        listPlaces = new CopyOnWriteArrayList<>();
        ifSearchError = false;
        var response = getSearchResponse();
        if (ifSearchError) {
            throw new ModelException("Bad response");
        }
        this.listPlaces = CustomJsonParser.parseGraphHopperJson(response);
    }

    private CompletableFuture<CopyOnWriteArrayList<PlaceInfo>> getNearPlaceResponse(Place place)  {
        var client = HttpClient.newHttpClient();
        var requestURIString = String.format("%sradius?radius=1000&lon=%s&lat=%s&units=metric&apikey=%s", urlOpenTrip, place.getLng(), place.getLat(), keyOpenTrip);
        var request = HttpRequest.newBuilder(
                        URI.create(requestURIString))
                .header("accept", "application/json")
                .build();
        System.out.println(requestURIString);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::body).thenApply(HttpResponse::body).thenApply(CustomJsonParser::parseNearbyPlaceJson);
    }

//    private CompletableFuture<String> getInfoResponse(PlaceInfo place) throws ExecutionException, InterruptedException, UnsupportedEncodingException {
//        var client = HttpClient.newHttpClient();
//        var requestURIString = String.format("%sradius?radius=1000&lon=%s&lat=%s&apikey=%s", urlOpenTrip, place.getLng(), place.getLat(), keyOpenTrip);
//        var request = HttpRequest.newBuilder(
//                        URI.create(requestURIString))
//                .header("accept", "application/json")
//                .build();
//        System.out.println(requestURIString);
//        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenApply(this::body).thenApply(HttpResponse::body);
//    }


    private CompletableFuture<WeatherInfo> getWeatherResponse(Place place) {
        var client = HttpClient.newHttpClient();
        var requestURIString = String.format("%s?lat=%s&lon=%s&units=metric&appid=%s", urlOpenWeather, place.getLat(), place.getLng(), keyOpenWeather);
        var request = HttpRequest.newBuilder(
                        URI.create(requestURIString))
                .header("accept", "application/json")
                .build();
        System.out.println(requestURIString);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::body).thenApply(HttpResponse::body).thenApply(CustomJsonParser::parseWeatherJson);
    }


    public void searchInfo(int index) {
        state = State.INFO;
        Place place = listPlaces.get(index);
        weather = getWeatherResponse(place);
        listNearbyPlaces = getNearPlaceResponse(place);

    }

    public void search() throws UnsupportedEncodingException, ExecutionException, JsonProcessingException, InterruptedException, ModelException {
        state = State.SEARCH;
        setPlaces();
    }
}
