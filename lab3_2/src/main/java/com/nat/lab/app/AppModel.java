package com.nat.lab.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class AppModel {
    private final static String keyOpenTrip = "5ae2e3f221c38a28845f05b6785bf6730720aa3e439523cf2e656370";
    private final static String keyGraphHopper = "a46d7782-ff0c-41d1-b414-0fa88b7659d4";
    private final static String urlOpenTrip = "https://api.opentripmap.com/0.1/en/places/";
    private final static String urlGraphHopper = "https://graphhopper.com/api/1/geocode";

    @Setter
    private String placeName;
    @Getter
    private CopyOnWriteArrayList<Place> listPlaces;
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

    private CopyOnWriteArrayList<Place> parseGraphHopperJson(String response) throws JsonProcessingException, ModelException {
        CopyOnWriteArrayList<Place> places = new CopyOnWriteArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        var jsonNode = objectMapper.readTree(response);
        var iter = jsonNode.get("hits").elements();
        //if no elements in hits - empty response
        if (!iter.hasNext()) {
            throw new ModelException("Don't find this place");
        }
        while (iter.hasNext()) {
            var currElemJson = iter.next();
            Place currPlace = new Place(
                    currElemJson.get("name") == null ? null : currElemJson.get("name").asText(),
                    currElemJson.get("city") == null ? null : currElemJson.get("city").asText(),
                    currElemJson.get("country") == null ? null : currElemJson.get("country").asText(),
                    currElemJson.get("street") == null ? null : currElemJson.get("street").asText(),
                    currElemJson.get("housenumber") == null ? null : currElemJson.get("housenumber").asText(),
                    currElemJson.get("lat") == null ? null : currElemJson.get("lat").asText(),
                    currElemJson.get("lng") == null ? null : currElemJson.get("lng").asText()
            );
            places.add(currPlace);
        }
        return places;
    }

    private void setPlaces() throws JsonProcessingException, ExecutionException, InterruptedException, UnsupportedEncodingException, ModelException {
        listPlaces = new CopyOnWriteArrayList<>();
        ifSearchError = false;
        CopyOnWriteArrayList<Place> places = new CopyOnWriteArrayList<>();
        var response = getSearchResponse();
        if (ifSearchError) {
            throw new ModelException("Bad response");
        }
        this.listPlaces = parseGraphHopperJson(response);
    }

    private String getInfoResponse(Place place) throws ExecutionException, InterruptedException, UnsupportedEncodingException {
        var client = HttpClient.newHttpClient();
        System.out.println(place.getLat());
        System.out.println(place.getLng());
        var requestURIString = String.format("%sradius?radius=5000&lon=%s&lat=%s&apikey=%s", urlOpenTrip, place.getLng(), place.getLat(), keyOpenTrip);
        var request = HttpRequest.newBuilder(
                        URI.create(requestURIString))
                .header("accept", "application/json")
                .build();
        System.out.println(requestURIString);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::body).thenApply(HttpResponse::body).get();
    }



    public void searchInfo(int index) throws UnsupportedEncodingException, ExecutionException, InterruptedException {
        state = State.INFO;
        Place place = listPlaces.get(index);
        System.out.println(getInfoResponse(place));

    }

    public void search() throws UnsupportedEncodingException, ExecutionException, JsonProcessingException, InterruptedException, ModelException {
        state = State.SEARCH;
        setPlaces();
    }
}
