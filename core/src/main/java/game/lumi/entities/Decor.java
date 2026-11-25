package game.lumi.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Statisches Weltobjekt (Baum, Busch, Blume, Stein …). Wiegt sich optional im Wind und
 * wird transparent, wenn es Lumi verdeckt (nur bei "occluder"-Objekten).
 */
public final class Decor extends Entity {
    public TextureRegion region;
    public float scale = 1f;
    public boolean flip;
    /** Pixel vom unteren Bildrand bis zum Fußpunkt. */
    public float anchor;
    /** Wind-Auslenkung der Spitze in Pixeln (0 = starr). */
    public float sway;
    public float swayPhase, swaySpeed = 1f;
    public float tint = Float.NaN; // Color.toFloatBits, NaN → unset
    public float shadowW, shadowH;
    /** Kollisionsradius in Kacheln, 0 = begehbar. */
    public float solid;
    /** Kann Lumi verdecken → wird bei Überlappung transparent. */
    public boolean occluder;
    public float alpha = 1f;
    /** Teil des Ranken-Tors (verschwindet, wenn das Steinrätsel gelöst ist). */
    public boolean gate;
    /** Bildschirm-Begrenzungsrechteck (für Culling und Verdeckungsprüfung), im Konstruktor-Aufruf finalize() gesetzt. */
    public float left, right, bottom, top;

    public void finish() {
        float w = region.getRegionWidth() * scale, h = region.getRegionHeight() * scale;
        float sx = screenX(), sy = screenY();
        left = sx - w / 2f - 6f; right = sx + w / 2f + 6f;
        bottom = sy - anchor * scale; top = bottom + h + sway;
    }
}
