package astechzgo.luminescent.rendering;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import astechzgo.luminescent.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class LightSource {
    public static final int MAX_LIGHTS = 250;

    private static final List<LightSource> lights = new ArrayList<>();

    public static boolean contains(LightSource source) {
        return lights.contains(source);
    }

    public static void addSource(LightSource source) {
        if(source == null) {
            return;
        }

        if(!contains(source)) {
            if(lights.size() == MAX_LIGHTS) {
                System.out.println("Out of space for lights");
                return;
            }

            lights.add(source);
        }
    }

    public static void removeSource(LightSource source) {
        if(source == null) {
            return;
        }

        lights.remove(source);
    }

    public static LightSource get(int index) {
        return lights.get(index);
    }

    public static int size() {
        return lights.size();
    }

    private GameCoordinates coords;
    private double radius;

    public LightSource(GameCoordinates coords, double radius) {
        this.coords = coords;
        this.radius = radius;
    }

    public GameCoordinates getCoords() {
        return coords;
    }

    public void setCoords(GameCoordinates coords) {
        this.coords = coords;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public float getScaledX() {
        return DisplayUtils.widthOffset + (float) new ScaledWindowCoordinates(coords).getScaledWindowCoordinatesX();
    }

    public float getScaledY() {
        return DisplayUtils.heightOffset + (float) new ScaledWindowCoordinates(coords).getScaledWindowCoordinatesY();
    }

    public float getScaledRadius() {
        return (float) Math.round((double) radius / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));
    }
}
