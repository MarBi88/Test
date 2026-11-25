package game.lumi.art;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/** Waldtiere (Kaninchen, Eichhörnchen, Vogel) im selben Kugel-Shading-Stil wie Lumi, plus Sprechblasen-Symbole. */
final class AnimalArt {
    private AnimalArt() {}

    static void build(ArtSheet s) {
        rabbit(s); squirrel(s); bird(s); bubbles(s);
    }

    private static void eye(Painter p, float x, float y, float r) {
        p.sphere(x, y, r, Painter.rgb(0x2A1830));
        p.disc(x - r * 0.35f, y - r * 0.4f, r * 0.38f, Color.WHITE, 1f);
    }

    /** Baumfrosch (Name "animal_rabbit" bleibt – Spiellogik-Typ RABBIT = hüpfendes, fütterbares Tier). */
    private static void rabbit(ArtSheet s) {
        Painter p = s.alloc("animal_rabbit", 96, 120);
        Color green = Painter.rgb(0x5FC04A), dark = Painter.rgb(0x3E9A3E), belly = Painter.rgb(0xEAF6B2);
        p.ellipse(22, 102, 13, 9, dark); p.ellipse(74, 102, 13, 9, dark);        // Hinterbeine
        p.ellipse(36, 111, 11, 5, green); p.ellipse(60, 111, 11, 5, green);      // Vorderfüße
        p.ellipse(48, 92, 30, 20, green);                                         // Körper
        p.ellipse(48, 99, 18, 12, belly);
        p.ellipse(48, 74, 27, 17, green);                                         // Kopf
        for (int i = 0; i < 5; i++) p.disc(30 + i * 9f, 94 + (i % 2) * 6f, 2.6f, dark, 0.55f);
        p.sphere(33, 60, 11f, green); p.sphere(63, 60, 11f, green);               // Augenhügel
        for (int e = 0; e < 2; e++) {
            float x = e == 0 ? 33 : 63;
            p.sphere(x, 59, 7.5f, Painter.rgb(0xF4BC3A));
            p.ellipse(x, 60, 2.6f, 5.4f, Painter.rgb(0x1E1020));
            p.disc(x - 2.2f, 56.5f, 1.8f, Color.WHITE, 1f);
        }
        p.stroke(26, 82, 48, 87, 2.2f, Painter.rgb(0x2A6A32), 0.9f); p.stroke(48, 87, 70, 82, 2.2f, Painter.rgb(0x2A6A32), 0.9f);
        p.disc(43, 74, 1.4f, Painter.rgb(0x2A6A32), 0.8f); p.disc(53, 74, 1.4f, Painter.rgb(0x2A6A32), 0.8f);
        p.softEllipse(24, 80, 6, 4, Painter.rgb(0xFF9A5A), 0.5f); p.softEllipse(72, 80, 6, 4, Painter.rgb(0xFF9A5A), 0.5f);
    }

    /** Krabbe (Typ SQUIRREL: huscht seitlich umher und kann buddeln). */
    private static void squirrel(ArtSheet s) {
        Painter p = s.alloc("animal_squirrel", 120, 140);
        Color red = Painter.rgb(0xE8503C), light = Painter.rgb(0xFF8A6A), dark = Painter.rgb(0xB83828);
        for (int side = -1; side <= 1; side += 2) {
            float cx = 60;
            for (int k = 0; k < 3; k++) p.stroke(cx + side * 22f, 112 + k * 4f, cx + side * (46f + k * 5f), 124 + k * 3f, 5f, dark, 1f); // Beine
            // Arm + Schere
            p.stroke(cx + side * 26f, 100, cx + side * 40f, 74, 9f, red, 1f);
            p.sphere(cx + side * 42f, 64, 14f, red);
            p.tri(cx + side * 42f, 64, cx + side * 52f, 46, cx + side * 36f, 52, dark, 1f);
            // Augenstiele
            p.stroke(cx + side * 10f, 90, cx + side * 11f, 68, 3.5f, light, 1f);
            p.sphere(cx + side * 11f, 62, 7.5f, Color.WHITE);
            p.disc(cx + side * 11f, 62.5f, 3.6f, Painter.rgb(0x1E1020), 1f);
            p.disc(cx + side * 11f - 1.2f, 61f, 1.2f, Color.WHITE, 1f);
        }
        p.ellipse(60, 102, 36, 22, red);
        p.ellipse(52, 96, 18, 9, light);
        p.stroke(50, 111, 60, 114, 2f, dark, 0.8f); p.stroke(60, 114, 70, 111, 2f, dark, 0.8f);
        for (int i = 0; i < 5; i++) p.disc(36 + i * 12f, 100 + (i % 2) * 7f, 2.4f, dark, 0.45f);
    }

    /** Papagei (Typ BIRD): rot, gelber Bauch, blaue Flügel, großer heller Schnabel. */
    private static void bird(ArtSheet s) {
        Painter p = s.alloc("animal_bird", 72, 64);
        Color red = Painter.rgb(0xE8352E), blue = Painter.rgb(0x2F6FD8), yellow = Painter.rgb(0xFFD23F);
        p.tri(20, 40, 2, 34, 6, 56, blue, 1f); p.tri(20, 42, 4, 46, 10, 58, red, 1f);   // langer Schwanz
        p.stroke(30, 52, 30, 60, 2f, Painter.rgb(0x8A8A8A), 1f); p.stroke(40, 52, 40, 60, 2f, Painter.rgb(0x8A8A8A), 1f);
        p.sphere(34, 36, 19, red);
        p.ellipse(38, 44, 11, 9, yellow);
        p.tri(46, 24, 64, 32, 50, 40, Painter.rgb(0xF4E8CC), 1f);                         // Schnabel oben
        p.tri(50, 36, 62, 34, 52, 42, Painter.rgb(0xCDB98A), 1f);
        p.disc(62, 33, 1.6f, Painter.rgb(0x2A2A2A), 1f);
        p.sphere(42, 29, 5.6f, Color.WHITE); eye(p, 43, 29, 3.6f);
        Painter w = s.alloc("animal_wing", 48, 40);
        w.ellipse(24, 20, 20, 10, Painter.rgb(0x2F6FD8));
        w.ellipse(28, 21, 12, 5, Painter.rgb(0xFFD23F));
    }

    private static void bubbles(ArtSheet s) {
        Painter b = s.alloc("ui_bubble", 112, 128);
        b.disc(56, 52, 46, new Color(0.1f, 0.15f, 0.2f, 1f), 0.25f);
        b.tri(44, 90, 68, 90, 56, 118, new Color(0.1f, 0.15f, 0.2f, 1f), 0.25f);
        b.disc(56, 48, 46, Color.WHITE, 1f);
        b.tri(42, 84, 70, 84, 56, 114, Color.WHITE, 1f);
        b.disc(56, 48, 41, Painter.rgb(0xFFF8E8), 1f);

        // Mango (Futter für den Frosch)
        Painter c = s.alloc("icon_carrot", 64, 64);
        c.ellipse(30, 38, 17, 21, Painter.rgb(0xFFA02E));
        c.softEllipse(34, 44, 11, 14, Painter.rgb(0xE8503C), 0.5f);
        c.ellipse(36, 22, 8, 5, Painter.rgb(0x4FB04F));
        c.stroke(30, 16, 34, 8, 3f, Painter.rgb(0x6A4A2A), 1f);

        // Fächermuschel (Wunsch der Krabbe: hier gibt es etwas zu buddeln)
        Painter a = s.alloc("icon_acorn", 64, 64);
        for (int i = 0; i < 7; i++) {
            float ang = MathUtils.PI + (i + 0.5f) / 7f * MathUtils.PI;
            a.tri(32, 50, 32 + MathUtils.cos(ang) * 28f - 4, 50 + MathUtils.sin(ang) * 36f + 3, 32 + MathUtils.cos(ang) * 28f + 4, 50 + MathUtils.sin(ang) * 36f + 3, i % 2 == 0 ? Painter.rgb(0xFF9AA0) : Painter.rgb(0xFFD8C8), 1f);
        }
        a.ellipse(32, 50, 9, 5, Painter.rgb(0xFFE3D2));

        Painter n = s.alloc("icon_note", 64, 64);
        Color nc = Painter.rgb(0x3A8FD0);
        n.stroke(40, 14, 40, 44, 5f, nc, 1f);
        n.ellipse(32, 46, 10, 7, nc);
        n.stroke(40, 14, 54, 24, 5f, nc, 1f);

        Painter h = s.alloc("icon_heart", 64, 64);
        Color hc = Painter.rgb(0xFF6F8F);
        h.sphere(22, 26, 14, hc); h.sphere(42, 26, 14, hc);
        h.tri(9, 32, 55, 32, 32, 58, hc, 1f);
    }
}
