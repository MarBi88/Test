package game.lumi.art;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/** Beutetiere, die Lumi am liebsten frisst: Insekten (Marienkäfer, Schmetterling, Heuschrecke), kleine Frösche, Spinnen. */
final class CritterArt {
    private CritterArt() {}

    static void build(ArtSheet s) {
        ladybug(s); butterfly(s); hopper(s); frog(s, 0, 0x5FC04A, 0xEAF6B2); frog(s, 1, 0xFF8A3A, 0xFFE9B0);
        spider(s, 0, 0x7A4A32); spider(s, 1, 0x8A4FC0);
        digits(s);
    }

    private static void eye(Painter p, float x, float y, float r) {
        p.sphere(x, y, r, Color.WHITE);
        p.disc(x + r * 0.15f, y + r * 0.1f, r * 0.55f, Painter.rgb(0x1E1020), 1f);
        p.disc(x - r * 0.2f, y - r * 0.2f, r * 0.2f, Color.WHITE, 1f);
    }

    private static void ladybug(ArtSheet s) {
        Painter p = s.alloc("crit_ladybug", 48, 40);
        Color k = Painter.rgb(0x2A2030);
        for (int i = -1; i <= 1; i++) { p.stroke(18 + i * 7f, 28, 14 + i * 9f, 36, 2f, k, 1f); p.stroke(30 + i * 7f, 28, 34 + i * 9f, 36, 2f, k, 1f); }
        p.sphere(24, 22, 15, Painter.rgb(0xE8352E));
        p.stroke(24, 8, 24, 36, 1.6f, k, 0.9f);
        p.sphere(24, 9, 6.5f, k);
        p.disc(21.5f, 8, 1.8f, Color.WHITE, 1f); p.disc(26.5f, 8, 1.8f, Color.WHITE, 1f);
        for (float[] d : new float[][] { { 17, 18 }, { 31, 18 }, { 16, 27 }, { 32, 27 }, { 24, 31 } }) p.disc(d[0], d[1], 2.6f, k, 1f);
    }

    private static void butterfly(ArtSheet s) {
        Painter p = s.alloc("crit_butterfly", 64, 52);
        Color a = Painter.rgb(0x4FA8F0), b = Painter.rgb(0xFF8A3A), body = Painter.rgb(0x3A2A40);
        p.ellipse(18, 18, 15, 13, a); p.ellipse(46, 18, 15, 13, a);
        p.ellipse(20, 34, 11, 10, b); p.ellipse(44, 34, 11, 10, b);
        p.disc(16, 17, 4.5f, Color.WHITE, 0.85f); p.disc(48, 17, 4.5f, Color.WHITE, 0.85f);
        p.ellipse(32, 26, 3.4f, 14f, body);
        p.stroke(30, 12, 24, 3, 1.4f, body, 1f); p.stroke(34, 12, 40, 3, 1.4f, body, 1f);
    }

    private static void hopper(ArtSheet s) {
        Painter p = s.alloc("crit_hopper", 56, 44);
        Color g = Painter.rgb(0x8CCB3E), d = Painter.rgb(0x4F9A32);
        p.tri(10, 34, 22, 14, 30, 34, d, 1f);                  // Hinterbein
        p.ellipse(28, 28, 18, 8, g);
        p.ellipse(44, 22, 8, 7, g);
        p.stroke(48, 16, 54, 4, 1.4f, d, 1f); p.stroke(44, 15, 46, 3, 1.4f, d, 1f);
        p.stroke(30, 32, 30, 40, 2f, d, 1f); p.stroke(38, 32, 40, 40, 2f, d, 1f);
        eye(p, 47, 21, 3.2f);
    }

    private static void frog(ArtSheet s, int v, int body, int belly) {
        Painter p = s.alloc("crit_frog" + v, 52, 44);
        Color g = Painter.rgb(body), dk = Painter.mix(g, Painter.rgb(0x1F4A2A), 0.4f, new Color());
        p.ellipse(10, 34, 8, 6, dk); p.ellipse(42, 34, 8, 6, dk);
        p.ellipse(26, 30, 18, 12, g); p.ellipse(26, 34, 10, 7, Painter.rgb(belly));
        p.ellipse(26, 22, 15, 10, g);
        p.sphere(17, 14, 6.5f, g); p.sphere(35, 14, 6.5f, g);
        eye(p, 17, 14, 4.2f); eye(p, 35, 14, 4.2f);
        p.stroke(18, 25, 26, 28, 1.6f, dk, 0.9f); p.stroke(26, 28, 34, 25, 1.6f, dk, 0.9f);
    }

    private static void spider(ArtSheet s, int v, int body) {
        Painter p = s.alloc("crit_spider" + v, 60, 48);
        Color g = Painter.rgb(body), k = Painter.mix(g, Painter.rgb(0x1E1020), 0.55f, new Color());
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 4; i++) {
                float y0 = 22 + (i - 1.5f) * 3f, kx = 30 + side * (15f + (i % 2) * 3f), ky = 10 + i * 6f - (i == 0 ? 4f : 0f);
                p.stroke(30 + side * 6f, y0, kx, ky, 2.6f, k, 1f);
                p.stroke(kx, ky, 30 + side * (26f - i * 1.5f), 38 + (i - 1.5f) * 1.5f, 2.4f, k, 1f);
            }
        }
        p.sphere(30, 26, 12, g);
        p.sphere(30, 15, 8.5f, Painter.mix(g, Color.WHITE, 0.12f, new Color()));
        eye(p, 26, 13, 3.6f); eye(p, 34, 13, 3.6f);
        p.softEllipse(30, 30, 6, 4, Painter.mix(g, Color.WHITE, 0.4f, new Color()), 0.5f);
    }

    /** Ziffern 0–9 im Siebensegment-Stil mit runden Enden (Zähler ohne Schriftart). */
    private static void digits(ArtSheet s) {
        int[] mask = { 0b0111111, 0b0000110, 0b1011011, 0b1001111, 0b1100110, 0b1101101, 0b1111101, 0b0000111, 0b1111111, 0b1101111 };
        // Segmente: a oben, b oben rechts, c unten rechts, d unten, e unten links, f oben links, g Mitte
        float[][] seg = { { 10, 10, 30, 10 }, { 32, 12, 32, 30 }, { 32, 34, 32, 52 }, { 10, 54, 30, 54 }, { 8, 34, 8, 52 }, { 8, 12, 8, 30 }, { 10, 32, 30, 32 } };
        for (int d = 0; d < 10; d++) {
            Painter p = s.alloc("digit" + d, 40, 64);
            for (int i = 0; i < 7; i++) if ((mask[d] & (1 << i)) != 0) p.stroke(seg[i][0], seg[i][1], seg[i][2], seg[i][3], 7f, Color.WHITE, 1f);
        }
    }
}
