package game.lumi.art;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/** Rätsel-Grafik: leuchtende Blumen (rosa, gelb, blau), Hinweisstein, magischer Stein, Ranken-Tor. */
final class PuzzleArt {
    private PuzzleArt() {}

    static final int[] FLOWER_COLORS = { 0xFF6FA8, 0xFFD84D, 0x6FAEFF };

    static void build(ArtSheet s) {
        for (int v = 0; v < 3; v++) flower(s, v);
        hintStone(s); magicStone(s); vine(s);
    }

    private static void flower(ArtSheet s, int v) {
        Painter p = s.alloc("flower_glow" + v, 84, 116);
        Color pc = Painter.rgb(FLOWER_COLORS[v]), stem = Painter.rgb(0x3E9A4E), leaf = Painter.rgb(0x5BBE5A);
        p.stroke(42, 108, 42, 50, 4.4f, stem, 1f);
        p.ellipse(26, 90, 12, 5.5f, leaf); p.ellipse(58, 80, 12, 5.5f, leaf);
        for (int i = 0; i < 6; i++) {
            float a = i / 6f * MathUtils.PI2 - 1.4f;
            p.sphere(42 + MathUtils.cos(a) * 15f, 40 + MathUtils.sin(a) * 14f, 12.5f, pc);
        }
        p.sphere(42, 40, 9.5f, Painter.rgb(0xFFF4C0));
    }

    private static void hintStone(ArtSheet s) {
        Painter p = s.alloc("hintstone", 110, 120);
        Color g = Painter.rgb(0x9A98B4);
        p.softEllipse(55, 106, 42, 8, new Color(0.08f, 0.1f, 0.2f, 1f), 0.4f);
        p.ellipse(55, 70, 40, 38, g);
        p.ellipse(55, 40, 30, 22, Painter.mix(g, Color.WHITE, 0.12f, new Color()));
        p.ellipse(44, 24, 22, 8, Painter.rgb(0x6FB04E));
        // drei Farbperlen in Reihenfolge
        for (int i = 0; i < 3; i++) {
            float x = 28 + i * 27f;
            p.disc(x, 62, 12, Painter.rgb(0x5A5878), 1f);
            p.sphere(x, 62, 10, Painter.rgb(FLOWER_COLORS[i]));
        }
        p.stroke(36, 82, 46, 82, 3f, Painter.rgb(0x6A6888), 0.8f);
    }

    private static void magicStone(ArtSheet s) {
        Painter p = s.alloc("stone_magic", 90, 140);
        Color g = Painter.mix(Painter.rgb(0x8C8CAC), Painter.rgb(0xA896D0), 0.4f, new Color());
        p.softEllipse(45, 128, 34, 7, new Color(0.08f, 0.1f, 0.2f, 1f), 0.4f);
        p.ellipse(45, 100, 32, 26, g);
        p.ellipse(45, 66, 26, 44, Painter.mix(g, Color.WHITE, 0.08f, new Color()));
        p.ellipse(45, 30, 18, 20, Painter.mix(g, Color.WHITE, 0.14f, new Color()));
        p.ellipse(36, 14, 14, 6, Painter.rgb(0x6FB04E));
        // Rune: Raute mit Punkt
        Color rune = Painter.rgb(0x5A4A8A);
        p.stroke(45, 44, 58, 62, 4f, rune, 1f); p.stroke(58, 62, 45, 80, 4f, rune, 1f);
        p.stroke(45, 80, 32, 62, 4f, rune, 1f); p.stroke(32, 62, 45, 44, 4f, rune, 1f);
        p.disc(45, 62, 4.5f, rune, 1f);
    }

    private static void vine(ArtSheet s) {
        Painter p = s.alloc("vine", 110, 130);
        Color d = Painter.rgb(0x4A2E78), m = Painter.rgb(0x6A44A4), l = Painter.rgb(0x8C66C8);
        p.softEllipse(55, 118, 44, 8, new Color(0.08f, 0.1f, 0.2f, 1f), 0.4f);
        p.sphere(32, 92, 26, d); p.sphere(78, 92, 26, d); p.sphere(55, 82, 32, m);
        p.sphere(36, 62, 24, m); p.sphere(74, 60, 24, m); p.sphere(55, 44, 26, l);
        for (int i = 0; i < 9; i++) {
            float a = i / 9f * MathUtils.PI;
            float x = 55 - MathUtils.cos(a) * 44f, y = 70 - MathUtils.sin(a) * 52f;
            p.tri(x - 4, y + 3, x + 4, y + 3, x + (x < 55 ? -6 : 6), y - 12, Painter.rgb(0x2E1A50), 1f);
        }
        for (int i = 0; i < 7; i++) p.sphere(24 + i * 11f, 50 + (i % 3) * 18f, 4.5f, Painter.rgb(0xFF9AD0));
    }
}
