package game.lumi.art;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * Zeichenwerkzeug für die prozedurale Grafik. Alle Operationen nutzen ausschließlich
 * Pixmap-Funktionen, die im libGDX-HTML5-Backend (Canvas) verfügbar sind.
 *
 * Kernidee des Looks: "Kugel-Shading" – Formen werden aus gestapelten, weich abgestuften
 * Kreisen aufgebaut (Licht oben links, kühle Schattenseite, warmes Rücklicht am Rand).
 * Das erzeugt die weiche, plastische Anmutung eines 3D-Animationsfilms ohne 3D-Engine.
 */
public final class Painter {
    public final Pixmap pm;
    public final int ox, oy, w, h;

    private static final Color SHADOW_TINT = new Color(0.62f, 0.56f, 0.78f, 1f);
    private static final Color WARM = new Color(1f, 0.86f, 0.55f, 1f);
    private static final Color SUN = new Color(1f, 0.97f, 0.82f, 1f);
    private final Color dark = new Color(), rim = new Color(), light = new Color(), tmp = new Color();

    public Painter(Pixmap pm, int ox, int oy, int w, int h) {
        this.pm = pm; this.ox = ox; this.oy = oy; this.w = w; this.h = h;
    }

    // ------------------------------------------------------------------ Farbhilfen

    public static Color mix(Color a, Color b, float t, Color out) {
        return out.set(a.r + (b.r - a.r) * t, a.g + (b.g - a.g) * t, a.b + (b.b - a.b) * t, a.a + (b.a - a.a) * t);
    }

    public static Color rgb(int hex) {
        return new Color(((hex >> 16) & 255) / 255f, ((hex >> 8) & 255) / 255f, (hex & 255) / 255f, 1f);
    }

    // ------------------------------------------------------------------ Primitive

    public void disc(float cx, float cy, float r, Color c, float a) {
        pm.setColor(c.r, c.g, c.b, a);
        pm.fillCircle(ox + Math.round(cx), oy + Math.round(cy), Math.max(1, Math.round(r)));
    }

    public void rect(float x, float y, float rw, float rh, Color c, float a) {
        pm.setColor(c.r, c.g, c.b, a);
        pm.fillRectangle(ox + Math.round(x), oy + Math.round(y), Math.max(1, Math.round(rw)), Math.max(1, Math.round(rh)));
    }

    public void tri(float x1, float y1, float x2, float y2, float x3, float y3, Color c, float a) {
        pm.setColor(c.r, c.g, c.b, a);
        pm.fillTriangle(ox + Math.round(x1), oy + Math.round(y1), ox + Math.round(x2), oy + Math.round(y2),
                ox + Math.round(x3), oy + Math.round(y3));
    }

    /** Dicke Linie aus Kreisen (rund an den Enden), thickness = Durchmesser. */
    public void stroke(float x1, float y1, float x2, float y2, float thickness, Color c, float a) {
        float dx = x2 - x1, dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        int steps = Math.max(1, (int) (len / Math.max(1f, thickness * 0.25f)));
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            disc(x1 + dx * t, y1 + dy * t, thickness * 0.5f, c, a);
        }
    }

    // ------------------------------------------------------------------ Plastische Formen

    /** Weich schattierte Kugel. Lichtrichtung (-0.45, -0.6) = oben links. */
    public void sphere(float cx, float cy, float r, Color base) {
        if (r < 3f) { disc(cx, cy, r, base, 1f); return; }
        float lx = -0.45f, ly = -0.6f;
        mix(base, SHADOW_TINT, 0.32f, dark); dark.mul(0.86f, 0.86f, 0.90f, 1f);
        mix(base, WARM, 0.35f, rim);
        mix(base, SUN, 0.40f, light);
        // Rücklicht-Rand auf der Schattenseite
        disc(cx, cy, r, rim, 1f);
        float rr = r * 0.94f;
        disc(cx + lx * r * 0.07f, cy + ly * r * 0.07f, rr, dark, 1f);
        int n = Math.max(8, Math.min(26, (int) (r / 2.2f)));
        for (int i = 1; i <= n; i++) {
            float t = i / (float) n;
            float rad = rr * (1f - 0.74f * t);
            float sh = rr * 0.40f * t;
            if (t < 0.45f) mix(dark, base, t / 0.45f, tmp); else mix(base, light, (t - 0.45f) / 0.55f, tmp);
            disc(cx + lx * sh + lx * r * 0.07f, cy + ly * sh + ly * r * 0.07f, rad, tmp, 1f);
        }
        // weicher Glanz
        disc(cx + lx * r * 0.50f, cy + ly * r * 0.50f, r * 0.20f, Color.WHITE, 0.10f);
        disc(cx + lx * r * 0.52f, cy + ly * r * 0.52f, r * 0.11f, Color.WHITE, 0.20f);
    }

    /** Schattierte Ellipse (Kugel, die in Breite/Höhe gestreckt wird). */
    public void ellipse(float cx, float cy, float rx, float ry, Color base) {
        if (Math.abs(rx - ry) < 1f) { sphere(cx, cy, rx, base); return; }
        float rmax = Math.max(rx, ry);
        int d = (int) Math.ceil(rmax * 2f) + 6;
        Pixmap t = new Pixmap(d, d, Pixmap.Format.RGBA8888);
        Painter tp = new Painter(t, 0, 0, d, d);
        tp.sphere(d / 2f, d / 2f, rmax, base);
        float dw = d * (rx / rmax), dh = d * (ry / rmax);
        pm.drawPixmap(t, 0, 0, d, d, ox + Math.round(cx - dw / 2f), oy + Math.round(cy - dh / 2f), Math.round(dw), Math.round(dh));
        t.dispose();
    }

    /** Weiche, einfarbige Ellipse mit radialem Alpha-Verlauf (Schatten, Glow, Decals). */
    public void softEllipse(float cx, float cy, float rx, float ry, Color c, float peakAlpha) {
        float rmax = Math.max(rx, ry);
        int d = (int) Math.ceil(rmax * 2f) + 2;
        Pixmap t = new Pixmap(d, d, Pixmap.Format.RGBA8888);
        Painter tp = new Painter(t, 0, 0, d, d);
        int n = 36;
        // Pro Ring so viel Alpha, dass das Zentrum ungefähr peakAlpha erreicht.
        float per = 1f - (float) Math.pow(1f - Math.min(0.98f, peakAlpha), 1f / n);
        for (int i = 0; i < n; i++) {
            float f = 1f - i / (float) n;
            // quadratische Verteilung → weicherer Rand
            tp.disc(d / 2f, d / 2f, (d / 2f - 1f) * f, c, per);
        }
        float dw = d * (rx / rmax), dh = d * (ry / rmax);
        pm.drawPixmap(t, 0, 0, d, d, ox + Math.round(cx - dw / 2f), oy + Math.round(cy - dh / 2f), Math.round(dw), Math.round(dh));
        t.dispose();
    }
}
