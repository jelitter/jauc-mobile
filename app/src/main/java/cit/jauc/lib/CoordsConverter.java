package cit.jauc.lib;

import cit.jauc.model.Location;

public class CoordsConverter {

    public static String getLocationfromCoords(long longitude, long latitude) {
        // TODO API Call to get location from coords
        return "Location " + longitude + "," + latitude;
    }

    public static String getLocationfromCoords(Location location) {
        return getLocationfromCoords(location.getLon(), location.getLat());
    }
}
