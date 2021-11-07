package com.nat.lab.app;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PlaceInfo {
    @NonNull
    @Getter
    String name;
    @NonNull
    @Getter
    String xid;
    @Getter @Setter
    String info;

    @Override
    public String toString() {
        return String.format(" Place name: %s ", name);
    }
}
