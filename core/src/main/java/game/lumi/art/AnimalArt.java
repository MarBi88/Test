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

    private static void rabbit(ArtSheet s) {
        Painter p = s.alloc("animal_rabbit", 96, 120);
        Color fur = Painter.rgb(0xEADFD0), inner = Painter.rgb(0xFFC9D2), cream = Painter.rgb(0xFFF6EA);
        p.sphere(22, 92, 9, cream);                      // Puschelschwanz
        p.ellipse(54, 104, 11, 7, fur); p.ellipse(34, 106, 10, 6, fur); // Füße
        p.ellipse(46, 86, 27, 24, fur);                  // Körper
        p.ellipse(52, 92, 14, 16, cream);                // Bauch
        // Ohren
        p.ellipse(34, 22, 8, 22, fur); p.ellipse(34, 25, 4, 15, inner);
        p.ellipse(60, 24, 8, 22, fur); p.ellipse(60, 27, 4, 15, inner);
        p.ellipse(48, 54, 24, 21, fur);                  // Kopf
        p.softEllipse(32, 62, 6, 4, Painter.rgb(0xFF8FA0), 0.55f); p.softEllipse(64, 62, 6, 4, Painter.rgb(0xFF8FA0), 0.55f);
        eye(p, 38, 50, 4.4f); eye(p, 58, 50, 4.4f);
        p.ellipse(48, 59, 3.4f, 2.6f, Painter.rgb(0xF08096));
        p.stroke(48, 62, 48, 65, 1.2f, Painter.rgb(0x8A5060), 0.8f);
        p.stroke(48, 65, 44, 67, 1.2f, Painter.rgb(0x8A5060), 0.8f); p.stroke(48, 65, 52, 67, 1.2f, Painter.rgb(0x8A5060), 0.8f);
    }

    private static void squirrel(ArtSheet s) {
        Painter p = s.alloc("animal_squirrel", 120, 140);
        Color fur = Painter.rgb(0xC9703A), light = Painter.rgb(0xE9A56E), cream = Painter.rgb(0xFFEBCB);
        // buschiger Schwanz als geschwungene Kugelkette
        for (int i = 0; i <= 9; i++) {
            float t = i / 9f;
            float x = 76 + MathUtils.sin(t * 2.6f) * 16 + t * 14, y = 112 - t * 88;
            p.sphere(x, y, 13 + MathUtils.sin(t * MathUtils.PI) * 8, i > 6 ? light : fur);
        }
        p.ellipse(60, 128, 11, 7, fur); p.ellipse(40, 128, 11, 7, fur);
        p.ellipse(50, 102, 25, 28, fur);
        p.ellipse(54, 108, 13, 18, cream);
        p.ellipse(32, 98, 6, 9, fur);                    // Vorderpfote
        p.ellipse(34, 118, 5, 3, Painter.rgb(0x7A4020));
        // Ohren mit Pinseln
        p.ellipse(36, 36, 7, 10, fur); p.ellipse(64, 36, 7, 10, fur);
        p.tri(34, 30, 36, 14, 40, 30, Painter.rgb(0x7A4020), 1f); p.tri(60, 30, 64, 14, 66, 30, Painter.rgb(0x7A4020), 1f);
        p.ellipse(50, 56, 22, 20, light);
        p.ellipse(50, 62, 15, 12, cream);
        eye(p, 40, 52, 4.2f); eye(p, 60, 52, 4.2f);
        p.ellipse(50, 60, 3.2f, 2.4f, Painter.rgb(0x2A1830));
        p.softEllipse(32, 60, 5, 3.5f, Painter.rgb(0xFF8FA0), 0.5f); p.softEllipse(68, 60, 5, 3.5f, Painter.rgb(0xFF8FA0), 0.5f);
    }

    private static void bird(ArtSheet s) {
        Painter p = s.alloc("animal_bird", 72, 64);
        Color blue = Painter.rgb(0x5BB8F0), cream = Painter.rgb(0xFFF3DA), teal = Painter.rgb(0x2FA6B8);
        p.tri(18, 36, 2, 28, 4, 46, teal, 1f); p.tri(18, 38, 2, 38, 6, 48, Painter.rgb(0x1F8696), 1f);
        p.stroke(30, 52, 30, 60, 2f, Painter.rgb(0xF59A3A), 1f); p.stroke(40, 52, 40, 60, 2f, Painter.rgb(0xF59A3A), 1f);
        p.sphere(34, 36, 19, blue);
        p.ellipse(38, 44, 11, 9, cream);
        p.tri(48, 28, 62, 33, 48, 38, Painter.rgb(0xF5A03A), 1f);
        eye(p, 42, 28, 3.6f);
        p.softEllipse(36, 36, 5, 3, Painter.rgb(0xFF8FA0), 0.45f);
        Painter w = s.alloc("animal_wing", 48, 40);
        w.ellipse(24, 20, 20, 10, Painter.rgb(0x3A8FD0));
        w.ellipse(30, 22, 12, 5, Painter.rgb(0x6FB8F0));
    }

    private static void bubbles(ArtSheet s) {
        Painter b = s.alloc("ui_bubble", 112, 128);
        b.disc(56, 52, 46, new Color(0.1f, 0.15f, 0.2f, 1f), 0.25f);
        b.tri(44, 90, 68, 90, 56, 118, new Color(0.1f, 0.15f, 0.2f, 1f), 0.25f);
        b.disc(56, 48, 46, Color.WHITE, 1f);
        b.tri(42, 84, 70, 84, 56, 114, Color.WHITE, 1f);
        b.disc(56, 48, 41, Painter.rgb(0xFFF8E8), 1f);

        Painter c = s.alloc("icon_carrot", 64, 64);
        c.tri(24, 22, 42, 26, 36, 60, Painter.rgb(0xF58A2E), 1f);
        c.tri(24, 22, 36, 24, 34, 58, Painter.rgb(0xFFB15A), 0.8f);
        c.disc(33, 22, 9, Painter.rgb(0xF58A2E), 1f);
        for (int i = -1; i <= 1; i++) c.tri(30 + i * 5, 18, 34 + i * 5, 18, 32 + i * 9, 2, Painter.rgb(0x4FB04F), 1f);

        Painter a = s.alloc("icon_acorn", 64, 64);
        a.sphere(32, 40, 15, Painter.rgb(0xD9A25A));
        a.ellipse(32, 26, 17, 10, Painter.rgb(0x7A4A28));
        a.stroke(32, 18, 32, 8, 3f, Painter.rgb(0x5A3A20), 1f);

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
