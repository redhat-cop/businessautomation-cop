package com.redhat.summit2019.generator;

import com.redhat.summit2019.model.Farm;
import com.redhat.summit2019.model.ImmutableFarm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FarmGenerator {
    private String[] farmNames;
    private String[] farmTypes;
    private ThreadLocalRandom random = ThreadLocalRandom.current();

    public FarmGenerator() {
        List<String> farmName = new ArrayList<>();
        List<String> farmType = new ArrayList<>();

        farmName.add("Old MacDonald");
        farmName.add("MacLeod");
        farmName.add("New MacDonald");
        farmName.add("Green Field");
        farmName.add("Finedrummond");
        farmName.add("Northwesthouse");
        farmName.add("Dungal and Sons");
        farmName.add("Brucecroft");
        farmName.add("Agnes and Sisters");
        farmName.add("Fergus and Daughters");
        farmName.add("Claire and Sons");
        farmName.add("Muir Family");
        farmName.add("Kirkton");
        farmName.add("Birckly");
        farmName.add("Rawhills");
        farmName.add("Langside");
        farmName.add("Dorothy and Toto");
        farmName.add("Lakebottom");
        farmName.add("Draffan");
        farmName.add("Bellscroft");
        farmNames = farmName.toArray(new String[0]);

        farmType.add("Dairy");
        farmType.add("Pig");
        farmType.add("Beef");
        farmType.add("Sheep");
        farmType.add("Horticulture");
        farmType.add("Cereals");
        farmType.add("Orchards");
        farmTypes = farmType.toArray(new String[0]);
    }

    public Farm getFarm() {
        String name = farmNames[random.nextInt(farmNames.length)];
        String type = farmTypes[random.nextInt(farmTypes.length)];

        return ImmutableFarm.builder()
                .name(name)
                .type(type)
                .size(random.nextInt(1, 10))
                .build();
    }
}
