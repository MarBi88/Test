package game.lumi.art;

import com.badlogic.gdx.graphics.Color;

/**
 * Lumis Körperteile als einzeln animierbare, einheitlich schattierte Sprites.
 * Alle Maße in "Anzeige-Pixeln"; gezeichnet wird mit K-facher Auflösung für Schärfe.
 * Das Aussehen ist damit über alle Animationen identisch (gleiche Sprites, nur Transformationen).
 */
public final class LumiArt {
    private LumiArt() {}

    /** Zeichen-Auflösung relativ zur Anzeigegröße. */
    public static final float K = 2f;

    public static final Color ORANGE = Painter.rgb(0xF78A2C);
    public static final Color ORANGE_LIGHT = Painter.rgb(0xFFA24A);
    public static final Color CREAM = Painter.rgb(0xFFF1D6);
    public static final Color NOSE = Painter.rgb(0x2B1B2E);
    public static final Color EAR_TIP = Painter.rgb(0x7A3A22);
    public static final Color PENDANT = Painter.rgb(0x58E6D6);

    static void build(ArtSheet s) {
        body(s);
        head(s);
        face(s);
        ears(s);
        tail(s);
        feet(s);
        pendant(s);
    }

    private static int k(float v) { return Math.round(v * K); }

    private static void body(ArtSheet s) {
        // Vorderansicht mit cremefarbenem Bauch
        Painter p = s.alloc("lumi_body", k(60), k(56));
        p.ellipse(k(30), k(28), k(22), k(21), ORANGE);
        p.ellipse(k(30), k(35), k(13), k(13), CREAM);
        // Rückenansicht
        Painter b = s.alloc("lumi_bodyBack", k(60), k(56));
        b.ellipse(k(30), k(28), k(22), k(21), ORANGE);
        b.ellipse(k(30), k(22), k(14), k(11), ORANGE_LIGHT);
    }

    private static void head(ArtSheet s) {
        Painter p = s.alloc("lumi_head", k(76), k(64));
        // Wangenfell (cremefarben) hinter dem Kopf, dann Kopf
        p.ellipse(k(13), k(42), k(10), k(8), CREAM);
        p.ellipse(k(63), k(42), k(10), k(8), CREAM);
        p.ellipse(k(38), k(33), k(31), k(25), ORANGE);
        p.ellipse(k(38), k(24), k(15), k(9), ORANGE_LIGHT);
        // helle Wangen vorne
        p.ellipse(k(20), k(44), k(11), k(8), CREAM);
        p.ellipse(k(56), k(44), k(11), k(8), CREAM);
        Painter b = s.alloc("lumi_headBack", k(76), k(64));
        b.ellipse(k(38), k(33), k(31), k(25), ORANGE);
        b.ellipse(k(38), k(25), k(18), k(11), ORANGE_LIGHT);
    }

    private static void face(ArtSheet s) {
        // Schnauze mit Nase – wird separat verschoben (Parallaxe → 3/4-Blick)
        Painter m = s.alloc("lumi_muzzle", k(40), k(30));
        m.ellipse(k(20), k(17), k(13), k(10), CREAM);
        m.ellipse(k(20), k(9), k(5.6f), k(4f), NOSE);
        m.disc(k(18.2f), k(7.6f), k(1.4f), Color.WHITE, 0.75f);
        // Lächeln: kurzer Strich unter der Nase, dann zwei nach oben gebogene Bögen (ω)
        m.stroke(k(20), k(13), k(20), k(16.2f), k(1.1f), NOSE, 0.75f);
        m.stroke(k(20), k(16.2f), k(17.2f), k(18.6f), k(1.1f), NOSE, 0.75f);
        m.stroke(k(17.2f), k(18.6f), k(14f), k(17.2f), k(1.1f), NOSE, 0.75f);
        m.stroke(k(20), k(16.2f), k(22.8f), k(18.6f), k(1.1f), NOSE, 0.75f);
        m.stroke(k(22.8f), k(18.6f), k(26f), k(17.2f), k(1.1f), NOSE, 0.75f);

        // Augen: große, glänzende Kulleraugen
        Painter e = s.alloc("lumi_eye", k(26), k(30));
        float cx = k(13), cy = k(15);
        e.ellipse(cx, cy, k(8.4f), k(10.4f), Painter.rgb(0xFFFFFF));
        e.ellipse(cx, cy + k(0.8f), k(6.6f), k(8.4f), Painter.rgb(0x6B3A1E));
        e.ellipse(cx, cy + k(1.2f), k(4.4f), k(6.2f), Painter.rgb(0x1E1020));
        e.disc(cx - k(2.2f), cy - k(2.6f), k(2.4f), Color.WHITE, 1f);
        e.disc(cx + k(2.2f), cy + k(3.4f), k(1.1f), Color.WHITE, 0.85f);

        // Glückliche Augen (^ ^)
        Painter h = s.alloc("lumi_eyeHappy", k(26), k(30));
        Color dark = Painter.rgb(0x2A1424);
        for (int i = 0; i <= 14; i++) {
            float t = i / 14f;
            float x = k(4) + t * k(18);
            float y = k(19) - (float) Math.sin(t * Math.PI) * k(9);
            h.disc(x, y, k(1.8f), dark, 1f);
        }
        // Rosa Wangen
        Painter c = s.alloc("lumi_blush", k(24), k(16));
        c.softEllipse(k(12), k(8), k(10), k(6), Painter.rgb(0xFF7A8A), 0.55f);
    }

    private static void ears(ArtSheet s) {
        // Fuchsohren: breite Dreiecke mit runden Ecken, dunkle Spitze, rosa-cremefarbenes Inneres.
        for (int variant = 0; variant < 2; variant++) {
            Painter p = s.alloc(variant == 0 ? "lumi_ear" : "lumi_earBack", k(32), k(40));
            Color dk = Painter.mix(ORANGE, Painter.rgb(0x8A4020), 0.45f, new Color());
            // Grundform: Dreieck + abgerundete Basis
            p.tri(k(3), k(34), k(16), k(2), k(29), k(34), dk, 1f);
            p.tri(k(4.5f), k(33), k(16), k(4.5f), k(27.5f), k(33), ORANGE, 1f);
            p.disc(k(8), k(31), k(6), ORANGE, 1f);
            p.disc(k(24), k(31), k(6), ORANGE, 1f);
            p.rect(k(8), k(27), k(16), k(10), ORANGE, 1f);
            // Licht von links oben
            p.tri(k(5), k(32), k(16), k(6), k(16), k(32), ORANGE_LIGHT, 0.55f);
            if (variant == 0) {
                p.tri(k(9), k(33), k(16), k(13), k(23), k(33), Painter.rgb(0xFFD3B8), 1f);
                p.disc(k(12), k(31), k(3.2f), Painter.rgb(0xFFD3B8), 1f);
                p.disc(k(20), k(31), k(3.2f), Painter.rgb(0xFFD3B8), 1f);
                p.tri(k(11), k(33), k(16), k(17), k(21), k(33), Painter.rgb(0xFFF1E6), 0.55f);
            }
            // dunkle Ohrspitze
            p.tri(k(13), k(10), k(16), k(2), k(19), k(10), EAR_TIP, 0.95f);
            p.disc(k(16), k(4), k(2.2f), EAR_TIP, 0.95f);
        }
    }

    private static void tail(ArtSheet s) {
        int w = k(40), h = k(70);
        Painter p = s.alloc("lumi_tail", w, h);
        float cx = w / 2f, ry = h / 2f - k(2), rx = w / 2f - k(3);
        p.ellipse(cx, h / 2f, rx, ry, ORANGE);
        p.ellipse(cx - k(3), h / 2f + k(4), rx * 0.5f, ry * 0.7f, ORANGE_LIGHT);
        // Cremefarbene Spitze, exakt entlang der Kontur
        float top = h / 2f - ry;
        float tipDepth = ry * 0.62f;
        for (float y = top + 1; y < top + tipDepth; y += 1f) {
            float u = (y - h / 2f) / ry;
            float half = rx * (float) Math.sqrt(Math.max(0f, 1f - u * u)) - k(1.4f);
            if (half <= 1f) continue;
            p.rect(cx - half, y, half * 2f, 1.4f, CREAM, 0.97f);
        }
        p.softEllipse(cx - k(4), top + tipDepth * 0.4f, k(5), tipDepth * 0.28f, Color.WHITE, 0.35f);
        p.softEllipse(cx, top + tipDepth + k(1), rx * 0.7f, k(3), ORANGE, 0.5f);
    }

    private static void feet(ArtSheet s) {
        Painter p = s.alloc("lumi_foot", k(24), k(18));
        p.ellipse(k(12), k(9), k(9.5f), k(7), ORANGE);
        p.ellipse(k(12), k(11), k(6.5f), k(4.4f), Painter.rgb(0x6A3A28));
    }

    private static void pendant(ArtSheet s) {
        Painter p = s.alloc("lumi_pendant", k(20), k(24));
        p.stroke(k(5), k(2), k(10), k(9), k(1f), Painter.rgb(0xC9A25A), 1f);
        p.stroke(k(15), k(2), k(10), k(9), k(1f), Painter.rgb(0xC9A25A), 1f);
        p.sphere(k(10), k(15), k(5.2f), PENDANT);
        p.disc(k(8.4f), k(13), k(1.5f), Color.WHITE, 0.9f);
        // kleine Facette
        p.tri(k(10), k(9.4f), k(13.4f), k(15), k(10), k(21), Painter.rgb(0xB5FFF2), 0.35f);
    }

}
