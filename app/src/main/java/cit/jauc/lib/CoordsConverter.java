package cit.jauc.lib;

import java.text.DecimalFormat;

import cit.jauc.model.Location;

public class CoordsConverter {

    public static String getLocationfromCoords(double longitude, double latitude) {
        return "Location " + new DecimalFormat("#.00").format(longitude) + "," + new DecimalFormat("#.00").format(latitude);
    }

    public static String getLocationfromCoords(Location location) {
        return getLocationfromCoords(location.getLon(), location.getLat());
    }
}
