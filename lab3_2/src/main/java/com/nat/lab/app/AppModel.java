package com.nat.lab.app;

import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppModel {
    private final CopyOnWriteArrayList<IListener> listeners = new CopyOnWriteArrayList<>();
    @Setter
    private String placeName;
    private String keyOpenTrip = "5ae2e3f221c38a28845f05b6785bf6730720aa3e439523cf2e656370";
    private String urlOpenTrip = "https://api.opentripmap.com/0.1/en/places/";


    public void  startWork() {
        var client = HttpClient.newHttpClient();
// create a request
        CompletableFuture <String> c = new CompletableFuture<>();
        var request = HttpRequest.newBuilder(
                        URI.create("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY"))
                .header("accept", "application/json")
                .build();

// use the client to send the request
       // var response = client.send(request, HttpResponse.BodyHandlers.ofString());

// the response:
      //  System.out.println(response);
    }



}
