package com.nat.lab.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.concurrent.CopyOnWriteArrayList;

public class CustomJsonParser {

    @SneakyThrows
    public static WeatherInfo parseWeatherJson(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        var jsonNode = objectMapper.readTree(response);
        var iter = jsonNode.get("weather").elements();
        String icon = iter.next().get("icon").asText();
        return new WeatherInfo(
                jsonNode.get("main").get("temp").asText(),
                jsonNode.get("main").get("feels_like").asText(),
                jsonNode.get("wind").get("speed").asText(),
                icon);
    }

    public static CopyOnWriteArrayList<Place> parseGraphHopperJson(String response) throws JsonProcessingException, ModelException {
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
                    currElemJson.get("point").get("lat") == null ? null : currElemJson.get("point").get("lat").asText(),
                    currElemJson.get("point").get("lng") == null ? null : currElemJson.get("point").get("lng").asText()
            );
            places.add(currPlace);
        }
        return places;
    }

    @SneakyThrows
    public static CopyOnWriteArrayList<PlaceInfo> parseNearbyPlaceJson(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        CopyOnWriteArrayList<PlaceInfo> places = new CopyOnWriteArrayList<>();

        var jsonNode = objectMapper.readTree(response);

        var iter = jsonNode.get("features").elements();
        while (iter.hasNext()) {
            var curElem = iter.next();
            //there can be empty response
            if (!curElem.get("properties").get("name").asText().isEmpty()) {
                places.add(new PlaceInfo(
                        curElem.get("properties").get("name").asText(),
                        curElem.get("properties").get("xid").asText()));
            }
        }
        return places;
    }


    @SneakyThrows
    public static Info parseInfo(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        CopyOnWriteArrayList<PlaceInfo> places = new CopyOnWriteArrayList<>();

        var jsonNode = objectMapper.readTree(response);

        //here may be no field wikipedia_extracts
        var name = jsonNode.get("wikipedia_extracts") == null ? null : jsonNode.get("wikipedia_extracts").get("title").asText().split(":")[1];
        var info = jsonNode.get("wikipedia_extracts") == null ? null : jsonNode.get("wikipedia_extracts").get("text").asText();
        return new Info(name, info);
    }
}
