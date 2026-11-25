package game.lumi.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Speichert den Fortschritt über libGDX-Preferences. Im Browser landet das automatisch im
 * LocalStorage, auf dem Desktop in einer Datei. Jeder Zugriff ist abgesichert: Wenn Speichern
 * nicht möglich ist (z. B. blockierter Browser-Speicher), läuft das Spiel einfach ohne Speicherstand.
 */
public final class SaveManager {
    private static final String NAME = "lumi_zauberwald_v1";
    private Preferences prefs;
    private float sinceFlush;
    private boolean dirty;

    public SaveManager() {
        try { prefs = Gdx.app.getPreferences(NAME); } catch (Throwable t) { prefs = null; log("Speicher nicht verfügbar", t); }
    }

    private void log(String m, Throwable t) { Gdx.app.error("Save", m + ": " + t); }

    public boolean hasGame() { return prefs != null && prefs.getBoolean("started", false); }

    public float getX(float def) { return prefs == null ? def : prefs.getFloat("x", def); }
    public float getY(float def) { return prefs == null ? def : prefs.getFloat("y", def); }

    /** Eingesammelte Kristalle als Zeichenkette aus '0'/'1' (ein Zeichen pro Kristall). */
    public String getCrystals() { return prefs == null ? "" : prefs.getString("crystals", ""); }

    public void setCrystals(String bits) { put(() -> prefs.putString("crystals", bits)); flush(); }

    public boolean soundOn() { return prefs == null || prefs.getBoolean("sound", true); }

    public void setSound(boolean on) { put(() -> prefs.putBoolean("sound", on)); flush(); }

    public void setPosition(float x, float y) {
        put(() -> { prefs.putFloat("x", x); prefs.putFloat("y", y); prefs.putBoolean("started", true); });
    }

    public void newGame() { put(() -> prefs.clear()); flush(); }

    private void put(Runnable r) {
        if (prefs == null) return;
        try { r.run(); dirty = true; } catch (Throwable t) { log("Schreiben fehlgeschlagen", t); }
    }

    /** Regelmäßig aufrufen; schreibt höchstens alle 4 Sekunden. */
    public void tick(float dt) {
        sinceFlush += dt;
        if (dirty && sinceFlush > 4f) flush();
    }

    public void flush() {
        if (prefs == null) return;
        try { prefs.flush(); } catch (Throwable t) { log("Flush fehlgeschlagen", t); }
        dirty = false; sinceFlush = 0f;
    }
}
