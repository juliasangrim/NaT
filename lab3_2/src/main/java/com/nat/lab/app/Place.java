package com.nat.lab.app;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Place {
       @Getter
       private final String name;
       @Getter
       private final String country;
       @Getter
       private final String city;
       @Getter
       private final String street;
       @Getter
       private final String housenamber;
       @Getter
       private final String lat;
       @Getter
       private final String lng;



       @Override
       public String toString() {
              String output = new String("");
              if (country != null) {
                     output += "Country: ";
                     output += country;
              }
              if (city != null) {
                     output += "\nCity: ";
                     output += city;
              }
              if (street != null) {
                     output += "\nStreet: ";
                     output += street;
              }
              if (housenamber != null) {
                     output += "\nHouse number: ";
                     output += housenamber;
              }
              if (name != null) {
                     output += "\nPlace name: ";
                     output += name;
              }
              return output;
       }
}
