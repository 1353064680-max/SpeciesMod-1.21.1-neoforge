package com.aigoth.species.race;

public class RaceFactory {
    public static Race createRaceFromId(String raceId) {
        return switch (raceId) {
            case "human" -> new Human();
            case "catkin" -> new Catkin();
            case "dogkin" -> new Dogkin();
            case "foxkin" -> new Foxkin();
            case "wingedbeast" -> new WingedBeast();
            case "aquatic" -> new Aquatic();
            case "slimekin" -> new Slimekin();
            case "elf" -> new Elf();
            case "plantkin" -> new Plantkin();
            case "cursed" -> new Cursed();
            case "aigoth" -> new Aigoth();
            default -> null;
        };
    }
}