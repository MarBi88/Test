package game.lumi.world;

/**
 * Isometrische Projektion (2:1). Weltkoordinaten sind Kachel-Einheiten (x, y auf der Bodenebene),
 * Bildschirmkoordinaten sind Pixel mit y nach oben (libGDX-Konvention).
 * Größere Tiefe (x + y) = weiter vorne = später zeichnen.
 */
public final class Iso {
    private Iso() {}

    public static final float HW = 64f, HH = 32f;

    public static float sx(float wx, float wy) { return (wx - wy) * HW; }
    public static float sy(float wx, float wy) { return -(wx + wy) * HH; }
    public static float depth(float wx, float wy) { return wx + wy; }

    /** Weltrichtung (Einheitsvektor, x/y) für eine Bildschirmrichtung (ix rechts, iy oben), 2:1-korrigiert. */
    public static float dirX(float ix, float iy) { return (ix - iy) * 0.70710678f; }
    public static float dirY(float ix, float iy) { return (-ix - iy) * 0.70710678f; }
}
