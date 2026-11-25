package game.lumi.art;

import com.badlogic.gdx.graphics.Color;

/**
 * Lumi, ein Katta (Ringelschwanz-Maki): graues Fell, weißes Gesicht und Bauch, schwarze Augenmasken,
 * große bernsteinfarbene Augen, schwarze Nase, lange schwarz-weiß geringelte Schwanz.
 * Körperteile sind einzeln animierbare, einheitlich schattierte Sprites (K-fache Auflösung).
 */
public final class LumiArt {
    private LumiArt() {}

    /** Zeichen-Auflösung relativ zur Anzeigegröße. */
    public static final float K = 2f;

    public static final Color FUR = Painter.rgb(0x929AA6);
    public static final Color FUR_DARK = Painter.rgb(0x6C7380);
    public static final Color FUR_LIGHT = Painter.rgb(0xB9BFC9);
    public static final Color WHITE = Painter.rgb(0xF7F4EF);
    public static final Color BLACK = Painter.rgb(0x26222C);
    public static final Color IRIS = Painter.rgb(0xEC9A2E);
    public static final Color PENDANT = Painter.rgb(0x58E6D6);

    static void build(ArtSheet s) {
        body(s); head(s); face(s); ears(s); tail(s); feet(s); pendant(s);
    }

    private static int k(float v) { return Math.round(v * K); }

    private static void body(ArtSheet s) {
        Painter p = s.alloc("lumi_body", k(60), k(56));
        p.ellipse(k(30), k(28), k(21), k(20), FUR);
        p.ellipse(k(30), k(34), k(13), k(14), WHITE);
        Painter b = s.alloc("lumi_bodyBack", k(60), k(56));
        b.ellipse(k(30), k(28), k(21), k(20), FUR);
        b.ellipse(k(30), k(22), k(14), k(10), FUR_DARK);
    }

    private static void head(ArtSheet s) {
        Painter p = s.alloc("lumi_head", k(76), k(64));
        p.ellipse(k(13), k(42), k(10), k(8), WHITE);   // Wangenfell
        p.ellipse(k(63), k(42), k(10), k(8), WHITE);
        p.ellipse(k(38), k(33), k(30), k(24), FUR);
        p.ellipse(k(38), k(22), k(15), k(8), FUR_DARK); // Scheitel
        p.ellipse(k(38), k(41), k(23), k(16), WHITE);   // weißes Gesicht
        Painter b = s.alloc("lumi_headBack", k(76), k(64));
        b.ellipse(k(38), k(33), k(30), k(24), FUR);
        b.ellipse(k(38), k(24), k(18), k(10), FUR_DARK);
    }

    private static void face(ArtSheet s) {
        // schwarze Augenmaske (wird wie die Augen verschoben → plastischer Blick)
        Painter pa = s.alloc("lumi_patch", k(30), k(36));
        pa.ellipse(k(15), k(18), k(10.5f), k(14f), BLACK);
        // Schnauze mit großer schwarzer Nase
        Painter m = s.alloc("lumi_muzzle", k(40), k(30));
        m.ellipse(k(20), k(17), k(12), k(9), Painter.rgb(0xEFEBE4));
        m.ellipse(k(20), k(9), k(7.2f), k(5f), BLACK);
        m.disc(k(17.6f), k(7.4f), k(1.5f), Color.WHITE, 0.8f);
        m.stroke(k(20), k(13), k(20), k(16.5f), k(1.1f), BLACK, 0.75f);
        m.stroke(k(20), k(16.5f), k(17.2f), k(18.8f), k(1.1f), BLACK, 0.75f);
        m.stroke(k(17.2f), k(18.8f), k(14f), k(17.4f), k(1.1f), BLACK, 0.75f);
        m.stroke(k(20), k(16.5f), k(22.8f), k(18.8f), k(1.1f), BLACK, 0.75f);
        m.stroke(k(22.8f), k(18.8f), k(26f), k(17.4f), k(1.1f), BLACK, 0.75f);
        // Augen: leuchtend bernsteinfarben, große Pupille, zwei Glanzlichter
        Painter e = s.alloc("lumi_eye", k(26), k(30));
        float cx = k(13), cy = k(15);
        e.ellipse(cx, cy, k(7.6f), k(8.8f), IRIS);
        e.ellipse(cx, cy + k(0.6f), k(4.6f), k(6.6f), BLACK);
        e.disc(cx - k(2.2f), cy - k(2.6f), k(2.4f), Color.WHITE, 1f);
        e.disc(cx + k(2.2f), cy + k(3.4f), k(1.1f), Color.WHITE, 0.85f);
        // glückliche/geschlossene Augen: helle Bögen (auf der schwarzen Maske sichtbar)
        Painter h = s.alloc("lumi_eyeHappy", k(26), k(30));
        Color light = Painter.rgb(0xFFE6B0);
        for (int i = 0; i <= 14; i++) {
            float t = i / 14f;
            h.disc(k(4) + t * k(18), k(19) - (float) Math.sin(t * Math.PI) * k(9), k(1.8f), light, 1f);
        }
        Painter c = s.alloc("lumi_blush", k(24), k(16));
        c.softEllipse(k(12), k(8), k(9), k(5), Painter.rgb(0xFFB8B0), 0.22f);
    }

    private static void ears(ArtSheet s) {
        for (int variant = 0; variant < 2; variant++) {
            Painter p = s.alloc(variant == 0 ? "lumi_ear" : "lumi_earBack", k(32), k(40));
            p.ellipse(k(16), k(22), k(11), k(14), FUR);
            if (variant == 0) p.ellipse(k(16), k(25), k(6.5f), k(9), WHITE);
            p.softEllipse(k(16), k(10), k(7), k(5), FUR_DARK, 0.5f);
        }
    }

    private static void tail(ArtSheet s) {
        int w = k(38), h = k(112);
        Painter p = s.alloc("lumi_tail", w, h);
        float cx = w / 2f, ry = h / 2f - k(2), rx = w / 2f - k(4);
        p.ellipse(cx, h / 2f, rx, ry, WHITE);
        float top = h / 2f - ry, band = k(9f);
        // Ringe entlang der Kontur, Spitze schwarz
        int ring = 0;
        for (float y0 = h / 2f + ry - band * 1.2f; y0 > top + band * 0.2f; y0 -= band, ring++) {
            if (ring % 2 == 1) continue;
            for (float y = y0; y < y0 + band; y += 1f) {
                float u = (y - h / 2f) / ry;
                if (Math.abs(u) >= 1f) continue;
                float half = rx * (float) Math.sqrt(1f - u * u) - k(0.6f);
                if (half > 1f) p.rect(cx - half, y, half * 2f, 1.4f, BLACK, 0.96f);
            }
        }
        for (float y = top + 1; y < top + band * 2.2f; y += 1f) { // schwarze Spitze
            float u = (y - h / 2f) / ry;
            float half = rx * (float) Math.sqrt(Math.max(0f, 1f - u * u)) - k(0.6f);
            if (half > 1f) p.rect(cx - half, y, half * 2f, 1.4f, BLACK, 0.96f);
        }
        // Rundung: Licht links, Schatten rechts
        p.softEllipse(cx - rx * 0.45f, h / 2f, rx * 0.35f, ry * 0.9f, Color.WHITE, 0.30f);
        p.softEllipse(cx + rx * 0.55f, h / 2f, rx * 0.4f, ry * 0.92f, Painter.rgb(0x2A2A44), 0.28f);
    }

    private static void feet(ArtSheet s) {
        Painter p = s.alloc("lumi_foot", k(24), k(18));
        p.ellipse(k(12), k(9), k(9.5f), k(7), Painter.rgb(0x4A4753));
        p.ellipse(k(12), k(11), k(6.5f), k(4.4f), Painter.rgb(0x8A8693));
    }

    private static void pendant(ArtSheet s) {
        Painter p = s.alloc("lumi_pendant", k(20), k(24));
        p.stroke(k(5), k(2), k(10), k(9), k(1f), Painter.rgb(0xC9A25A), 1f);
        p.stroke(k(15), k(2), k(10), k(9), k(1f), Painter.rgb(0xC9A25A), 1f);
        p.sphere(k(10), k(15), k(5.2f), PENDANT);
        p.disc(k(8.4f), k(13), k(1.5f), Color.WHITE, 0.9f);
        p.tri(k(10), k(9.4f), k(13.4f), k(15), k(10), k(21), Painter.rgb(0xB5FFF2), 0.35f);
    }
}
