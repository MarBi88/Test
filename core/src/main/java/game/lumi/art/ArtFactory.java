package game.lumi.art;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;

/**
 * Erzeugt die gesamte Umgebungsgrafik prozedural (kein Download, keine Fremdassets,
 * keine Lizenzfragen). Die Palette folgt der Art Direction: Waldgrün, Moosgrün,
 * Sonnengelb, sanftes Orange, Türkis, Himmelblau, magisches Violett, Cremeweiß.
 */
public final class ArtFactory {
    private ArtFactory() {}

    // ---- Palette
    public static final Color FOREST = Painter.rgb(0x2E7D4A);
    public static final Color MOSS = Painter.rgb(0x6FA84A);
    public static final Color SUNNY = Painter.rgb(0xF6D860);
    public static final Color ORANGE = Painter.rgb(0xF58A2E);
    public static final Color TEAL = Painter.rgb(0x3CC6B8);
    public static final Color SKY = Painter.rgb(0x8EC9F0);
    public static final Color VIOLET = Painter.rgb(0x9B6BE0);
    public static final Color CREAM = Painter.rgb(0xFFF1D6);

    public static final float TREE_ANCHOR = 24f; // Pixel vom unteren Rand bis zum Fußpunkt
    public static final int TREE_VARIANTS = 4, BUSH_VARIANTS = 3, FLOWER_VARIANTS = 8, TUFT_VARIANTS = 3,
            ROCK_VARIANTS = 3, TILE_VARIANTS = 4;

    public static ArtSheet build() {
        ArtSheet s = new ArtSheet();
        tiles(s);
        softShapes(s);
        trees(s);
        bushes(s);
        flowers(s);
        tufts(s);
        mushrooms(s);
        rocks(s);
        log(s);
        leaf(s);
        crystal(s);
        AnimalArt.build(s);
        PuzzleArt.build(s);
        cliff(s);
        waterfall(s);
        LumiArt.build(s);
        UiArt.build(s);
        s.finish();
        return s;
    }

    // ------------------------------------------------------------------ Boden

    private static void tiles(ArtSheet s) {
        int[] bases = { 0x5FAE48, 0x6BB549, 0x56A64F, 0x65AA45 };
        RandomXS128 rnd = new RandomXS128(11);
        for (int v = 0; v < TILE_VARIANTS; v++) {
            Painter p = s.alloc("tile" + v, 132, 68);
            Color base = Painter.rgb(bases[v]);
            p.tri(0, 34, 66, 0, 132, 34, base, 1f);
            p.tri(0, 34, 66, 68, 132, 34, base, 1f);
            Color hi = Painter.mix(base, Painter.rgb(0xE4F28A), 0.45f, new Color());
            Color lo = Painter.mix(base, Painter.rgb(0x1F6B48), 0.5f, new Color());
            // weiche Lichtflecken
            p.softEllipse(60 + rnd.nextInt(14), 30 + rnd.nextInt(8), 24, 11, hi, 0.28f);
            p.softEllipse(40 + rnd.nextInt(50), 22 + rnd.nextInt(24), 16, 8, lo, 0.20f);
            // Grashalme und Sprenkel, sicher innerhalb der Raute
            for (int i = 0; i < 46; i++) {
                float dx = (rnd.nextFloat() - 0.5f) * 116f, dy = (rnd.nextFloat() - 0.5f) * 56f;
                if (Math.abs(dx) / 66f + Math.abs(dy) / 34f > 0.82f) continue;
                float x = 66 + dx, y = 34 + dy;
                boolean light = rnd.nextBoolean();
                Color c = light ? hi : lo;
                p.tri(x - 1.6f, y, x + 1.6f, y, x + (rnd.nextFloat() - 0.5f) * 3f, y - 4f - rnd.nextFloat() * 4f, c, 0.55f);
            }
        }
    }

    private static void softShapes(ArtSheet s) {
        Painter b = s.alloc("blob", 256, 128);
        b.softEllipse(128, 64, 126, 62, Color.WHITE, 0.95f);
        Painter sh = s.alloc("shadow", 128, 64);
        sh.softEllipse(64, 32, 62, 30, new Color(0.10f, 0.08f, 0.22f, 1f), 0.55f);
        Painter g = s.alloc("glow", 128, 128);
        g.softEllipse(64, 64, 62, 62, Color.WHITE, 0.95f);
        Painter w = s.alloc("white", 8, 8);
        w.rect(0, 0, 8, 8, Color.WHITE, 1f);
        Painter ray = s.alloc("ray", 64, 256);
        ray.softEllipse(32, 128, 31, 127, Color.WHITE, 0.9f);
        Painter vig = s.alloc("vignette", 256, 144);
        // Vignette: weiche dunkle Ränder. Jede Spalte/Zeile bekommt ihren Alpha-Wert direkt (kein Aufsummieren → keine Streifen).
        Color vc = new Color(0.04f, 0.09f, 0.07f, 1f);
        for (int x = 0; x < 256; x++) {
            float d = Math.min(x, 255 - x) / 76f;
            if (d < 1f) vig.rect(x, 0, 1, 144, vc, 0.50f * (1f - d) * (1f - d));
        }
        for (int y = 0; y < 144; y++) {
            float d = Math.min(y, 143 - y) / 52f;
            if (d < 1f) vig.rect(0, y, 256, 1, vc, 0.50f * (1f - d) * (1f - d));
        }
    }

    // ------------------------------------------------------------------ Bäume

    private static void trunk(Painter p, float cx, float baseY, float topY, float w0, float w1, Color dark, Color mid, Color light) {
        float len = baseY - topY;
        for (float y = baseY; y >= topY; y -= 3f) {
            float t = (baseY - y) / len;
            float r = MathUtils.lerp(w0, w1, t) * 0.5f;
            p.disc(cx, y, r, dark, 1f);
        }
        for (float y = baseY; y >= topY; y -= 3f) {
            float t = (baseY - y) / len;
            float r = MathUtils.lerp(w0, w1, t) * 0.5f;
            p.disc(cx - r * 0.26f, y, r * 0.66f, mid, 1f);
        }
        for (float y = baseY - 4; y >= topY; y -= 3f) {
            float t = (baseY - y) / len;
            float r = MathUtils.lerp(w0, w1, t) * 0.5f;
            p.disc(cx - r * 0.48f, y, r * 0.2f, light, 0.65f);
        }
        // Wurzelanläufe
        p.disc(cx - w0 * 0.55f, baseY - 2, w0 * 0.28f, dark, 1f);
        p.disc(cx + w0 * 0.55f, baseY - 2, w0 * 0.30f, dark, 1f);
        p.disc(cx - w0 * 0.55f, baseY - 4, w0 * 0.20f, mid, 1f);
    }

    private static void canopy(Painter p, RandomXS128 rnd, float cx, float cy, float rx, float ry, int perLayer,
                               float sphereR, Color dark, Color mid, Color light, Color top) {
        // Hinten dunkel → vorne hell, damit der Kronenkörper räumlich wirkt.
        Color[] layers = { dark, mid, light };
        for (int l = 0; l < 3; l++) {
            int n = perLayer - l;
            float off = rnd.nextFloat() * 6f;
            for (int k = 0; k < n; k++) {
                float a = off + k / (float) n * MathUtils.PI2;
                float lx = cx + MathUtils.cos(a) * rx * (1f - l * 0.22f);
                float ly = cy + MathUtils.sin(a) * ry * (1f - l * 0.22f) - l * ry * 0.10f;
                Color c = Painter.mix(layers[l], Painter.mix(layers[Math.min(2, l + 1)], layers[l], 0.5f, new Color()), rnd.nextFloat(), new Color());
                p.sphere(lx, ly, sphereR * (1f - l * 0.10f) * (0.88f + rnd.nextFloat() * 0.24f), c);
            }
        }
        // Sonnenseite oben
        for (int k = 0; k < 3; k++) {
            p.sphere(cx + (k - 1) * rx * 0.45f - rx * 0.1f, cy - ry * 0.62f + Math.abs(k - 1) * 10f, sphereR * 0.72f, top);
        }
        // kleine Blattbüschel am Rand
        for (int k = 0; k < 18; k++) {
            float a = rnd.nextFloat() * MathUtils.PI2;
            float x = cx + MathUtils.cos(a) * rx * 1.05f, y = cy + MathUtils.sin(a) * ry * 1.05f - 6f;
            p.sphere(x, y, 8 + rnd.nextFloat() * 6, rnd.nextBoolean() ? light : mid);
        }
    }

    private static void trees(ArtSheet s) {
        Color tDark = Painter.rgb(0x5B3A2E), tMid = Painter.rgb(0x84573B), tLight = Painter.rgb(0xB98456);
        // 0 – runde Eiche
        {
            Painter p = s.alloc("tree0", 340, 420);
            RandomXS128 r = new RandomXS128(101);
            trunk(p, 170, 385, 250, 44, 28, tDark, tMid, tLight);
            canopy(p, r, 170, 170, 92, 58, 8, 66, Painter.rgb(0x1F6A45), Painter.rgb(0x2E8B4F), Painter.rgb(0x58AE50), Painter.rgb(0x8CCB55));
            blossoms(p, r, 170, 170, 100, 70, Painter.rgb(0xFFE27A), 9);
        }
        // 1 – schlanker Baum mit violetten Blüten
        {
            Painter p = s.alloc("tree1", 340, 420);
            RandomXS128 r = new RandomXS128(202);
            trunk(p, 170, 385, 230, 34, 20, tDark, tMid, tLight);
            canopy(p, r, 170, 140, 70, 78, 7, 56, Painter.rgb(0x2C7A57), Painter.rgb(0x3F9A5C), Painter.rgb(0x6DBA5E), Painter.rgb(0x9AD66A));
            blossoms(p, r, 170, 140, 82, 92, Painter.rgb(0xC9A2F5), 16);
        }
        // 2 – türkisgrüne Fantasie-Tanne
        {
            Painter p = s.alloc("tree2", 340, 420);
            RandomXS128 r = new RandomXS128(303);
            trunk(p, 170, 385, 330, 30, 22, tDark, tMid, tLight);
            Color d = Painter.rgb(0x1D6B5F), m = Painter.rgb(0x2E8F73), l = Painter.rgb(0x57B68A), t = Painter.rgb(0x8FD9A8);
            for (int tier = 0; tier < 5; tier++) {
                float cy = 330 - tier * 62f;
                float rx = 118 - tier * 21f;
                int n = 5 - tier / 2;
                for (int k = 0; k < n; k++) {
                    float x = 170 + (k - (n - 1) / 2f) * (rx * 2f / n) * 0.95f;
                    Color c = Painter.mix(d, m, tier / 4f + r.nextFloat() * 0.2f, new Color());
                    p.ellipse(x, cy + Math.abs(k - (n - 1) / 2f) * 6f, rx / n * 1.45f + 12, 36 - tier * 3, c);
                }
                p.ellipse(170, cy - 14, rx * 0.62f, 30 - tier * 2, tier > 2 ? t : l);
            }
            p.ellipse(170, 38, 18, 30, t);
            blossoms(p, r, 170, 190, 90, 130, Painter.rgb(0xBFF5E8), 10);
        }
        // 3 – dicke, alte Eiche in Goldgrün
        {
            Painter p = s.alloc("tree3", 340, 420);
            RandomXS128 r = new RandomXS128(404);
            trunk(p, 170, 385, 260, 62, 40, tDark, tMid, tLight);
            canopy(p, r, 170, 178, 112, 62, 9, 70, Painter.rgb(0x3B7A3C), Painter.rgb(0x5C9B3E), Painter.rgb(0x93BF48), Painter.rgb(0xCBDD62));
            blossoms(p, r, 170, 178, 120, 74, Painter.rgb(0xFFF1B0), 8);
        }
    }

    private static void blossoms(Painter p, RandomXS128 r, float cx, float cy, float rx, float ry, Color c, int n) {
        for (int i = 0; i < n; i++) {
            float a = r.nextFloat() * MathUtils.PI2, d = (float) Math.sqrt(r.nextFloat());
            float x = cx + MathUtils.cos(a) * rx * d * 0.9f, y = cy + MathUtils.sin(a) * ry * d * 0.9f;
            p.disc(x, y, 5.5f, c, 0.35f);
            p.sphere(x, y, 3.8f, c);
        }
    }

    // ------------------------------------------------------------------ Büsche, Blumen, Gras

    private static void bushes(ArtSheet s) {
        int[][] pal = { { 0x2A7A4E, 0x3F9A58, 0x6BBF5E }, { 0x3A8A55, 0x57AA5A, 0x86CB62 }, { 0x2D7B5E, 0x45A06A, 0x7ED08A } };
        for (int v = 0; v < BUSH_VARIANTS; v++) {
            Painter p = s.alloc("bush" + v, 190, 140);
            RandomXS128 r = new RandomXS128(500 + v);
            Color d = Painter.rgb(pal[v][0]), m = Painter.rgb(pal[v][1]), l = Painter.rgb(pal[v][2]);
            p.softEllipse(95, 122, 78, 14, new Color(0.08f, 0.1f, 0.2f, 1f), 0.35f);
            p.sphere(60, 92, 38, d); p.sphere(130, 92, 38, d); p.sphere(95, 84, 44, m);
            p.sphere(70, 78, 32, m); p.sphere(120, 76, 32, m);
            p.sphere(92, 62, 32, l); p.sphere(60, 62, 22, l);
            for (int i = 0; i < 7; i++) {
                float x = 40 + r.nextFloat() * 110, y = 50 + r.nextFloat() * 50;
                Color bc = v == 0 ? Painter.rgb(0xFF8FB0) : v == 1 ? Painter.rgb(0xFFE070) : Painter.rgb(0xB89CF5);
                p.sphere(x, y, 5f, bc);
            }
        }
    }

    private static void flowers(ArtSheet s) {
        int[] petals = { 0xFF8FB0, 0xFFD84D, 0xFFFFFF, 0xB98CF0, 0xFF9A4A, 0x7FB8FF, 0xFF6FA8, 0x8FE8D8 };
        for (int v = 0; v < FLOWER_VARIANTS; v++) {
            Painter p = s.alloc("flower" + v, 56, 80);
            Color stem = Painter.rgb(0x3E9A4E), leaf = Painter.rgb(0x5BBE5A), pc = Painter.rgb(petals[v]);
            p.stroke(28, 76, 28 + (v % 2 == 0 ? 3 : -3), 40, 3.2f, stem, 1f);
            p.ellipse(19, 62, 8, 4, leaf); p.ellipse(37, 56, 8, 4, leaf);
            float fx = 28 + (v % 2 == 0 ? 3 : -3), fy = 34;
            int n = v % 3 == 0 ? 6 : 5;
            for (int i = 0; i < n; i++) {
                float a = i / (float) n * MathUtils.PI2 - 1.2f;
                p.sphere(fx + MathUtils.cos(a) * 9.5f, fy + MathUtils.sin(a) * 9f, 8.2f, pc);
            }
            p.sphere(fx, fy, 6.2f, v == 1 ? Painter.rgb(0xF58A2E) : Painter.rgb(0xFFD84D));
        }
    }

    private static void tufts(ArtSheet s) {
        for (int v = 0; v < TUFT_VARIANTS; v++) {
            Painter p = s.alloc("tuft" + v, 64, 64);
            RandomXS128 r = new RandomXS128(700 + v);
            Color d = Painter.rgb(0x3E9A4E), l = Painter.rgb(0x8CCB55), m = Painter.rgb(0x5BB04F);
            for (int i = 0; i < 9; i++) {
                float bx = 32 + (i - 4) * 3.2f, tip = bx + (i - 4) * 3.6f + (r.nextFloat() - 0.5f) * 6f;
                float h = 24 + r.nextFloat() * 24;
                Color c = i % 3 == 0 ? d : i % 3 == 1 ? m : l;
                p.tri(bx - 2.6f, 60, bx + 2.6f, 60, tip, 60 - h, c, 1f);
            }
        }
    }

    private static void mushrooms(ArtSheet s) {
        int[] caps = { 0xE8503C, 0xB070E8 };
        for (int v = 0; v < 2; v++) {
            Painter p = s.alloc("mushroom" + v, 72, 72);
            p.softEllipse(36, 62, 26, 6, new Color(0.08f, 0.1f, 0.2f, 1f), 0.35f);
            p.ellipse(36, 48, 8, 15, CREAM);
            Color cc = Painter.rgb(caps[v]);
            p.ellipse(36, 34, 26, 17, cc);
            for (int i = 0; i < 4; i++) p.sphere(24 + i * 8, 26 + (i % 2) * 8, 3.2f, CREAM);
        }
    }

    private static void rocks(ArtSheet s) {
        for (int v = 0; v < ROCK_VARIANTS; v++) {
            Painter p = s.alloc("rock" + v, 130, 100);
            Color g = Painter.mix(Painter.rgb(0x8E94A6), Painter.rgb(0xA89AC0), v * 0.3f, new Color());
            p.softEllipse(65, 86, 52, 10, new Color(0.08f, 0.1f, 0.2f, 1f), 0.4f);
            p.ellipse(60, 62, 46 - v * 4, 30 - v * 2, g);
            p.ellipse(96, 72, 22, 16, Painter.mix(g, Color.WHITE, 0.1f, new Color()));
            p.ellipse(52, 40, 28 - v * 2, 12, Painter.rgb(0x6FB04E));
            p.ellipse(44, 38, 14, 6, Painter.rgb(0x9BD35E));
        }
    }

    private static void log(ArtSheet s) {
        Painter p = s.alloc("log0", 220, 110);
        Color d = Painter.rgb(0x5B3A2E), m = Painter.rgb(0x84573B), l = Painter.rgb(0xB98456);
        p.softEllipse(110, 92, 90, 12, new Color(0.08f, 0.1f, 0.2f, 1f), 0.35f);
        for (float x = 30; x <= 190; x += 3) p.disc(x, 68, 27, d, 1f);
        for (float x = 30; x <= 190; x += 3) p.disc(x, 62, 19, m, 1f);
        for (float x = 30; x <= 190; x += 3) p.disc(x, 54, 7, l, 0.6f);
        p.ellipse(190, 66, 16, 27, Painter.rgb(0xE3C28E));
        p.ellipse(190, 66, 10, 18, Painter.rgb(0xC99C64));
        p.ellipse(190, 66, 4, 8, Painter.rgb(0xE3C28E));
        p.ellipse(80, 40, 30, 10, Painter.rgb(0x6FB04E));
    }

    private static void leaf(ArtSheet s) {
        Painter p = s.alloc("leaf", 40, 40);
        p.ellipse(20, 20, 16, 8, Painter.rgb(0xA6DB5E));
        p.stroke(7, 20, 33, 20, 1.6f, Painter.rgb(0x4FA24E), 0.8f);
    }

    /** Lichtkristall: Hauptkristall + zwei kleine, Facetten in Türkis/Eisblau. Fußpunkt unten Mitte. */
    private static void crystal(ArtSheet s) {
        Painter p = s.alloc("crystal", 80, 100);
        Color deep = Painter.rgb(0x1FA9A0), mid = Painter.rgb(0x58E6D6), lite = Painter.rgb(0xB5FFF2), white = Painter.rgb(0xEFFFFC);
        p.softEllipse(40, 90, 26, 6, new Color(0.08f, 0.1f, 0.2f, 1f), 0.35f);
        // kleine Kristalle hinten
        p.tri(14, 90, 22, 54, 32, 90, deep, 1f); p.tri(22, 54, 26, 90, 32, 90, mid, 0.9f);
        p.tri(52, 90, 62, 58, 68, 90, deep, 1f); p.tri(62, 58, 66, 90, 68, 90, mid, 0.9f);
        // Hauptkristall: sechsseitige Spitze
        p.tri(40, 6, 22, 30, 58, 30, lite, 1f);
        p.tri(22, 30, 58, 30, 40, 92, mid, 1f);
        p.tri(22, 30, 40, 30, 40, 92, deep, 0.85f);
        p.tri(40, 6, 40, 30, 58, 30, white, 0.55f);
        p.tri(40, 30, 58, 30, 40, 92, Painter.rgb(0x3FD0C4), 0.8f);
        p.tri(30, 30, 40, 30, 36, 60, white, 0.35f);
        p.disc(36, 22, 2.5f, Color.WHITE, 0.9f);
    }

    /** Felswand mit kleiner Höhle (violett leuchtend), Moos und Farn. Fußpunkt unten Mitte. */
    private static void cliff(ArtSheet s) {
        Painter p = s.alloc("cliff", 360, 300);
        RandomXS128 r = new RandomXS128(909);
        Color g1 = Painter.rgb(0x7E8098), g2 = Painter.rgb(0x9A98B4), g3 = Painter.rgb(0xB0AACB);
        p.softEllipse(180, 282, 150, 14, new Color(0.08f, 0.1f, 0.2f, 1f), 0.4f);
        // Felsmassiv aus Reihen runder Brocken (hinten oben → vorne unten), nach oben schmaler
        Color[] gc = { g1, g2, g3, Painter.mix(g1, g3, 0.5f, new Color()) };
        for (int row = 4; row >= 0; row--) {
            int n = new int[] { 5, 5, 4, 3, 2 }[row]; float rad = 40 - row * 3f, y = 244 - row * 42f;
            for (int i = 0; i < n; i++) {
                float x = 180 + (i - (n - 1) / 2f) * (rad * 1.5f) + (r.nextFloat() - 0.5f) * 8f;
                p.sphere(x, y + (r.nextFloat() - 0.5f) * 10f, rad * (0.9f + r.nextFloat() * 0.2f), gc[r.nextInt(4)]);
            }
        }
        // Höhle
        p.ellipse(180, 200, 58, 74, Painter.rgb(0x1E1830));
        p.softEllipse(180, 222, 44, 56, Painter.rgb(0x9B6BE0), 0.55f);
        p.softEllipse(180, 236, 20, 28, Painter.rgb(0xE4D2FF), 0.55f);
        for (int i = 0; i < 5; i++) { // Felsbogen um die Höhle
            float a = MathUtils.PI + i * MathUtils.PI / 4f;
            p.ellipse(180 + MathUtils.cos(a) * 62f, 205 + MathUtils.sin(a) * 82f, 17, 21, g3);
        }
        // Sockelsteine
        for (int i = 0; i < 6; i++) p.ellipse(52 + i * 51f, 272, 28 + (i % 3) * 5, 17 + (i % 2) * 3, i % 2 == 0 ? g2 : g3);
        // Moos und Farn oben
        for (int i = 0; i < 9; i++) p.ellipse(40 + i * 35f, 62 + r.nextInt(30), 28, 12, Painter.rgb(0x6FB04E));
        for (int i = 0; i < 7; i++) {
            float x = 40 + i * 46f + r.nextInt(10), y = 70 + r.nextInt(18);
            for (int k = -2; k <= 2; k++) p.tri(x - 3, y + 8, x + 3, y + 8, x + k * 11f, y - 26 + Math.abs(k) * 7f, Painter.rgb(k % 2 == 0 ? 0x3E9A4E : 0x5BBE5A), 1f);
        }
    }

    /** Wasserfall-Vorhang in 4 Animationsbildern (Streifen wandern nach unten, Periode 64 px). */
    private static void waterfall(ArtSheet s) {
        for (int f = 0; f < 4; f++) {
            Painter p = s.alloc("waterfall" + f, 150, 270);
            RandomXS128 r = new RandomXS128(33);
            // Grundkörper
            p.rect(22, 20, 106, 236, Painter.rgb(0x8FD8F0), 0.42f);
            p.softEllipse(75, 20, 62, 14, Color.WHITE, 0.8f);
            for (int i = 0; i < 11; i++) {
                float x = 24 + i * 9.5f + r.nextFloat() * 3f, w = 3f + r.nextFloat() * 4f;
                float ph = r.nextFloat() * 64f;
                for (int k = -1; k < 5; k++) {
                    float y = 20 + k * 64f + ((f * 16f + ph) % 64f);
                    if (y < 20 || y + 38 > 256) continue;
                    p.rect(x, y, w, 38f, Color.WHITE, 0.55f);
                }
            }
            for (int k = 0; k < 8; k++) p.rect(22, 20 + k * 30f + f * 7f % 30f, 106, 2f, Color.WHITE, 0.18f);
            // seitliche Ränder weich
            p.rect(22, 20, 4, 236, Color.WHITE, 0.3f); p.rect(124, 20, 4, 236, Color.WHITE, 0.3f);
            // Gischt unten
            p.softEllipse(75, 252, 70, 14, Color.WHITE, 0.9f);
            for (int i = 0; i < 6; i++) p.sphere(34 + i * 16f, 246 + (i + f) % 3 * 3f, 7f, Painter.rgb(0xEAF8FF));
        }
    }
}
