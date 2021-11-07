package com.nat.lab.app;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class WeatherInfo {
    @Getter
    private String  temp;
    @Getter
    private String tempFeelsLike;
    @Getter
    private String speedWind;
    @Getter
    private String icon;

    @Override
    public String toString() {
        return String.format("""
                Temperature: %s °C
                Feels like %s °C
                Wind speed: %s m/s
                """, temp, tempFeelsLike, speedWind);
    }

}
