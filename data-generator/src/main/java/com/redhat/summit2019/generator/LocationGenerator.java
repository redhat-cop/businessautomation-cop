package com.redhat.summit2019.generator;

import com.redhat.summit2019.model.ImmutableLocation;
import com.redhat.summit2019.model.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LocationGenerator {
    private Location[] locations;

    public LocationGenerator() {
        List<Location> locationList = new ArrayList<>();
        locationList.add(ImmutableLocation.of("Dunfermline", "Fife"));
        locationList.add(ImmutableLocation.of("Kirkcaldy", "Fife"));
        locationList.add(ImmutableLocation.of("Greenock", "Inverclyde"));
        locationList.add(ImmutableLocation.of("Port Glasgow", "Inverclyde"));
        locationList.add(ImmutableLocation.of("Airdrie", "North Lanarkshire"));
        locationList.add(ImmutableLocation.of("Coatbridge", "North Lanarkshire"));
        locationList.add(ImmutableLocation.of("Hamilton", "South Lanarkshire"));
        locationList.add(ImmutableLocation.of("Larkhall", "South Lanarkshire"));
        locationList.add(ImmutableLocation.of("Bathgate", "West Lothian"));
        locationList.add(ImmutableLocation.of("Livingston", "West Lothian"));
        locationList.add(ImmutableLocation.of("Bara", "East Lothian"));
        locationList.add(ImmutableLocation.of("Humbie", "East Lothian"));
        locations = locationList.toArray(new Location[0]);
    }

    public Location getLocation() {
        int random = ThreadLocalRandom.current().nextInt(locations.length);
        return locations[random];
    }
}
