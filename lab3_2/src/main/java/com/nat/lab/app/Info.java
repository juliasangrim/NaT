package com.nat.lab.app;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Info {
    @Getter
    String title;
    @Getter
    String text;

    @Override
    public String toString() {
        return String.format("""
                %s
                %s
                """, title, text);
    }
}
