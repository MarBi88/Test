package game.lumi.config;

/** Zentrale Konstanten. Keine Logik, keine Abhängigkeiten. */
public final class GameConfig {
    private GameConfig() {}

    /** Virtuelle Bildschirmgröße (Welt- und UI-Viewport werden per ExtendViewport darauf angepasst). */
    public static final float VIEW_W = 1280f;
    public static final float VIEW_H = 720f;

    /** Isometrische Kachelgröße in Pixeln (2:1-Raster). */
    public static final float TILE_W = 128f;
    public static final float TILE_H = 64f;

    /** Weltgröße in Kacheln (quadratisch). Der begehbare Bereich ist eine organische Form darin. */
    public static final int WORLD_TILES = 96;

    /** Bewegung in Kacheln pro Sekunde. Für ein 5-jähriges Kind bewusst gemütlich. */
    public static final float WALK_SPEED = 3.4f;
    public static final float RUN_SPEED = 5.6f;

    /** Wird vom Launcher gesetzt (HTML: Browser meldet Touch-Fähigkeit). */
    public static boolean touchDevice = false;
}
