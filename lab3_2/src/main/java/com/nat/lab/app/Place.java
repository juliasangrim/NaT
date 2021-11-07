package com.nat.lab.app;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Place {
       @Getter
       private final String name;
       @Getter
       private final String city;
       @Getter
       private final String country;
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
                     output += String.format("""
                             Country: %s
                             """, country);
              }
              if (city != null) {
                     output += String.format("""
                             City: %s
                             """, city);
              }
              if (street != null) {
                     output += String.format("""
                             Street: %s
                             """, street);
              }
              if (housenamber != null) {
                     output += String.format("""
                             House number: %s
                             """, housenamber);
              }
              if (name != null) {
                     output += String.format("""
                             Place name: %s
                             """, name);
              }
              return output;
       }
}
