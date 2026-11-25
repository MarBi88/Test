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
            ROCK_VARIANTS = 3, TILE_VARIANTS = 4, SAND_VARIANTS = 2, SEA_VARIANTS = 2;

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
        // Tropische Wiese (4), Sand (2), Meer (2). Alle als Raute 132×68.
        int[] grass = { 0x7DC44E, 0x72BC4F, 0x86CB52, 0x6AB84E };
        RandomXS128 rnd = new RandomXS128(11);
        for (int v = 0; v < TILE_VARIANTS; v++) groundTile(s, "tile" + v, Painter.rgb(grass[v]), Painter.rgb(0xF4F58E), Painter.rgb(0x2F8A48), rnd, true);
        int[] sand = { 0xF2E0B0, 0xE9D49E };
        for (int v = 0; v < SAND_VARIANTS; v++) groundTile(s, "sand" + v, Painter.rgb(sand[v]), Painter.rgb(0xFFF6D6), Painter.rgb(0xC9B27A), rnd, false);
        int[] sea = { 0x58D0D6, 0x2AA3C4 };
        for (int v = 0; v < SEA_VARIANTS; v++) {
            Painter p = s.alloc("sea" + v, 132, 68);
            Color base = Painter.rgb(sea[v]);
            p.tri(0, 34, 66, 0, 132, 34, base, 1f);
            p.tri(0, 34, 66, 68, 132, 34, base, 1f);
            Color hi = Painter.mix(base, Color.WHITE, 0.45f, new Color()), lo = Painter.mix(base, Painter.rgb(0x0B5C86), 0.5f, new Color());
            p.softEllipse(50 + rnd.nextInt(30), 30 + rnd.nextInt(10), 26, 10, hi, 0.28f);
            p.softEllipse(40 + rnd.nextInt(40), 26 + rnd.nextInt(18), 22, 9, lo, 0.22f);
            for (int i = 0; i < 4; i++) { // kurze Wellenstriche
                float dx = (rnd.nextFloat() - 0.5f) * 70f, dy = (rnd.nextFloat() - 0.5f) * 30f;
                if (Math.abs(dx) / 66f + Math.abs(dy) / 34f > 0.75f) continue;
                p.stroke(66 + dx - 7, 34 + dy, 66 + dx + 7, 34 + dy, 1.8f, Color.WHITE, 0.35f);
            }
        }
    }

    private static void groundTile(ArtSheet s, String name, Color base, Color hiTint, Color loTint, RandomXS128 rnd, boolean blades) {
        Painter p = s.alloc(name, 132, 68);
        p.tri(0, 34, 66, 0, 132, 34, base, 1f);
        p.tri(0, 34, 66, 68, 132, 34, base, 1f);
        Color hi = Painter.mix(base, hiTint, 0.45f, new Color());
        Color lo = Painter.mix(base, loTint, 0.5f, new Color());
        p.softEllipse(60 + rnd.nextInt(14), 30 + rnd.nextInt(8), 24, 11, hi, 0.28f);
        p.softEllipse(40 + rnd.nextInt(50), 22 + rnd.nextInt(24), 16, 8, lo, 0.20f);
        for (int i = 0; i < 46; i++) {
            float dx = (rnd.nextFloat() - 0.5f) * 116f, dy = (rnd.nextFloat() - 0.5f) * 56f;
            if (Math.abs(dx) / 66f + Math.abs(dy) / 34f > 0.82f) continue;
            float x = 66 + dx, y = 34 + dy;
            Color c = rnd.nextBoolean() ? hi : lo;
            if (blades) p.tri(x - 1.6f, y, x + 1.6f, y, x + (rnd.nextFloat() - 0.5f) * 3f, y - 4f - rnd.nextFloat() * 4f, c, 0.55f);
            else p.disc(x, y, 1.2f + rnd.nextFloat() * 1.2f, c, 0.5f);
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
            if (d < 1f) vig.rect(x, 0, 1, 144, vc, 0.26f * (1f - d) * (1f - d));
        }
        for (int y = 0; y < 144; y++) {
            float d = Math.min(y, 143 - y) / 52f;
            if (d < 1f) vig.rect(0, y, 256, 1, vc, 0.26f * (1f - d) * (1f - d));
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

    /** Gebogener Stamm von (x0,y0) nach (x1,y1); bend = seitliche Ausbuchtung. */
    private static void palmTrunk(Painter p, float x0, float y0, float x1, float y1, float bend, float w0, float w1) {
        Color dark = Painter.rgb(0x7A5C3E), mid = Painter.rgb(0xA3835C), light = Painter.rgb(0xCBAE82);
        int n = 60;
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i <= n; i++) {
                float t = i / (float) n;
                float x = MathUtils.lerp(x0, x1, t) + bend * MathUtils.sin(t * MathUtils.PI), y = MathUtils.lerp(y0, y1, t);
                float r = MathUtils.lerp(w0, w1, t) * 0.5f;
                if (pass == 0) p.disc(x, y, r, dark, 1f);
                else if (pass == 1) p.disc(x - r * 0.25f, y, r * 0.65f, mid, 1f);
                else if (i % 5 == 0) p.stroke(x - r * 0.9f, y, x + r * 0.9f, y - 2f, 1.6f, dark, 0.35f); // Ringe
                else p.disc(x - r * 0.5f, y, r * 0.2f, light, 0.6f);
            }
        }
        p.disc(x0 - w0 * 0.55f, y0 - 2, w0 * 0.3f, dark, 1f); p.disc(x0 + w0 * 0.55f, y0 - 2, w0 * 0.3f, dark, 1f);
    }

    /** Palmwedel: dicker, spitz zulaufender Bogen mit Fiederblättchen. angle in Bogenmaß (Bildschirm, y nach unten). */
    private static void frond(Painter p, float x0, float y0, float angle, float len, float droop, Color dark, Color light) {
        int n = 22;
        float px = x0, py = y0;
        for (int i = 1; i <= n; i++) {
            float t = i / (float) n;
            float x = x0 + MathUtils.cos(angle) * len * t, y = y0 + MathUtils.sin(angle) * len * t + droop * len * t * t;
            float th = MathUtils.lerp(9f, 2f, t);
            Color c = Painter.mix(dark, light, t * 0.8f, new Color());
            p.stroke(px, py, x, y, th, c, 1f);
            if (i % 2 == 0 && i < n - 1) {
                float tx = x - px, ty = y - py, tl = (float) Math.sqrt(tx * tx + ty * ty) + 1e-3f;
                for (int side = -1; side <= 1; side += 2) {
                    float ll = MathUtils.lerp(26f, 9f, t);
                    float nx = -ty / tl * side, ny = tx / tl * side;
                    p.stroke(x, y, x + nx * ll + tx / tl * ll * 0.5f, y + ny * ll + ty / tl * ll * 0.5f + ll * 0.35f, MathUtils.lerp(4f, 1.6f, t), Painter.mix(c, light, 0.4f, new Color()), 0.95f);
                }
            }
            px = x; py = y;
        }
    }

    private static void crown(Painter p, RandomXS128 r, float cx, float cy, float len, Color nutColor) {
        float[] ang = { -2.95f, -2.45f, -1.95f, -1.57f, -1.2f, -0.7f, -0.2f, 0.25f, 2.85f };
        Color dark = Painter.rgb(0x1F7A3A), light = Painter.rgb(0x7FD04F);
        // Wedel hinten (nach oben) zuerst, hängende zuletzt
        for (int i = 0; i < ang.length; i++) {
            boolean hang = ang[i] > 0f || ang[i] < -2.8f;
            frond(p, cx, cy, ang[i] + (r.nextFloat() - 0.5f) * 0.12f, len * (0.85f + r.nextFloat() * 0.25f), hang ? 0.9f : 0.5f, dark, light);
        }
        p.sphere(cx - 8, cy + 10, 7f, nutColor); p.sphere(cx + 6, cy + 12, 7f, nutColor); p.sphere(cx - 1, cy + 18, 7f, nutColor);
    }

    private static void trees(ArtSheet s) {
        // 0 – Kokospalme
        {
            Painter p = s.alloc("tree0", 340, 420);
            RandomXS128 r = new RandomXS128(101);
            palmTrunk(p, 165, 385, 190, 190, 22f, 30, 17);
            crown(p, r, 190, 190, 115f, Painter.rgb(0x6B4A2A));
        }
        // 1 – schlanke, geneigte Palme mit grünen Kokosnüssen
        {
            Painter p = s.alloc("tree1", 340, 420);
            RandomXS128 r = new RandomXS128(202);
            palmTrunk(p, 140, 385, 215, 140, -34f, 26, 14);
            crown(p, r, 215, 140, 100f, Painter.rgb(0x9BC24A));
        }
        // 2 – Bananenstaude mit Fruchtstand
        {
            Painter p = s.alloc("tree2", 340, 420);
            RandomXS128 r = new RandomXS128(303);
            Color stem = Painter.rgb(0x93BE55), stemD = Painter.rgb(0x6E9A3E);
            for (float y = 385; y > 235; y -= 3) p.disc(170, y, MathUtils.lerp(17f, 12f, (385 - y) / 150f), stemD, 1f);
            for (float y = 385; y > 235; y -= 3) p.disc(166, y, MathUtils.lerp(11f, 7f, (385 - y) / 150f), stem, 1f);
            float[] ang = { -2.9f, -2.3f, -1.75f, -1.2f, -0.65f, -0.2f };
            for (int i = 0; i < ang.length; i++) {
                float a = ang[i], x0 = 170, y0 = 235;
                int n = 18; float len = 150f, px = x0, py = y0;
                for (int k = 1; k <= n; k++) {
                    float t = k / (float) n;
                    float x = x0 + MathUtils.cos(a) * len * t, y = y0 + MathUtils.sin(a) * len * t + 0.8f * len * t * t;
                    p.stroke(px, py, x, y, MathUtils.lerp(30f, 9f, t) * (0.9f + 0.1f * MathUtils.sin(t * 3.14f)), Painter.mix(Painter.rgb(0x3E9A3E), Painter.rgb(0x86D45A), t * 0.7f + (i % 2) * 0.1f, new Color()), 1f);
                    px = x; py = y;
                }
                p.stroke(x0, y0, px, py, 2f, Painter.rgb(0xC8F08A), 0.7f); // Mittelrippe
            }
            p.sphere(190, 258, 9f, Painter.rgb(0x8B2F6E)); // Blütenknospe
            for (int i = 0; i < 8; i++) p.ellipse(180 + (i % 4) * 9f, 266 + (i / 4) * 12f + (i % 2) * 4f, 6f, 11f, Painter.rgb(0xF6D84A));
        }
        // 3 – Flammenbaum: flache Schirmkrone voller roter Blüten
        {
            Painter p = s.alloc("tree3", 340, 420);
            RandomXS128 r = new RandomXS128(404);
            palmTrunk(p, 170, 385, 170, 235, 8f, 34, 22);
            p.stroke(170, 245, 90, 200, 14, Painter.rgb(0x8E6E4A), 1f); p.stroke(170, 245, 250, 196, 14, Painter.rgb(0x8E6E4A), 1f);
            p.ellipse(170, 196, 140, 42, Painter.rgb(0x2E8A44));
            p.ellipse(170, 178, 118, 40, Painter.rgb(0x42A44C));
            p.ellipse(172, 160, 84, 32, Painter.rgb(0x62BF55));
            for (int i = 0; i < 34; i++) {
                float a = r.nextFloat() * MathUtils.PI2, d = (float) Math.sqrt(r.nextFloat());
                p.sphere(170 + MathUtils.cos(a) * 112f * d, 168 + MathUtils.sin(a) * 30f * d - 6f, 6f + r.nextFloat() * 4f, r.nextBoolean() ? Painter.rgb(0xFF4A3A) : Painter.rgb(0xFF8A2E));
            }
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
        int[] flower = { 0xFF3E5E, 0xFFD23F, 0xFF8FC0 };
        for (int v = 0; v < BUSH_VARIANTS; v++) {
            Painter p = s.alloc("bush" + v, 190, 140);
            RandomXS128 r = new RandomXS128(500 + v);
            p.softEllipse(95, 122, 78, 14, new Color(0.08f, 0.1f, 0.2f, 1f), 0.35f);
            // breite Blätter fächerförmig aus der Mitte
            for (int i = 0; i < 9; i++) {
                float a = MathUtils.PI + (i + 0.5f) / 9f * MathUtils.PI, len = 62f + r.nextFloat() * 20f;
                float x0 = 95, y0 = 118, px = x0, py = y0;
                for (int k = 1; k <= 10; k++) {
                    float t = k / 10f, x = x0 + MathUtils.cos(a) * len * t, y = y0 + MathUtils.sin(a) * len * t * 0.85f + 12f * t * t;
                    p.stroke(px, py, x, y, MathUtils.lerp(20f, 6f, t), Painter.mix(Painter.rgb(0x2A9048), Painter.rgb(0x7ED45A), t * 0.7f + (i % 3) * 0.08f, new Color()), 1f);
                    px = x; py = y;
                }
            }
            Color fc = Painter.rgb(flower[v]);
            for (int i = 0; i < 4; i++) {
                float cx = 45 + i * 33f + r.nextInt(10), cy = 62 + r.nextInt(28);
                for (int k = 0; k < 5; k++) { float a = k / 5f * MathUtils.PI2; p.sphere(cx + MathUtils.cos(a) * 7f, cy + MathUtils.sin(a) * 7f, 6.5f, fc); }
                p.sphere(cx, cy, 4.2f, Painter.rgb(0xFFE08A));
            }
        }
    }

    private static void flowers(ArtSheet s) {
        int[] petals = { 0xFF3E5E, 0xFF8A2E, 0xFFEFA0, 0xE04FA8, 0xB070E8, 0xFF9AC0, 0xFFD23F, 0xFFFFFF };
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
            Color d = Painter.rgb(0x2F9A4A), l = Painter.rgb(0xA2E65E), m = Painter.rgb(0x4FBF50);
            for (int i = 0; i < 9; i++) {
                float bx = 32 + (i - 4) * 3.2f, tip = bx + (i - 4) * 3.6f + (r.nextFloat() - 0.5f) * 6f;
                float h = 24 + r.nextFloat() * 24;
                Color c = i % 3 == 0 ? d : i % 3 == 1 ? m : l;
                p.tri(bx - 2.6f, 60, bx + 2.6f, 60, tip, 60 - h, c, 1f);
            }
        }
    }

    private static void mushrooms(ArtSheet s) {
        // 0 – Fächermuschel, 1 – Seestern (Name "mushroom" bleibt: wird als Strandfund verwendet)
        Painter a = s.alloc("mushroom0", 72, 72);
        a.softEllipse(36, 62, 24, 6, new Color(0.08f, 0.1f, 0.2f, 1f), 0.3f);
        Color shell = Painter.rgb(0xFFB6B0), shell2 = Painter.rgb(0xFFE3D2);
        for (int i = 0; i < 9; i++) {
            float ang = MathUtils.PI + (i + 0.5f) / 9f * MathUtils.PI;
            float x2 = 36 + MathUtils.cos(ang) * 26f, y2 = 58 + MathUtils.sin(ang) * 30f;
            a.tri(36, 58, x2 - 4, y2 + 3, x2 + 4, y2 + 3, i % 2 == 0 ? shell : shell2, 1f);
        }
        a.ellipse(36, 58, 8, 5, shell2);
        Painter b = s.alloc("mushroom1", 72, 72);
        b.softEllipse(36, 62, 24, 6, new Color(0.08f, 0.1f, 0.2f, 1f), 0.3f);
        Color star = Painter.rgb(0xFF8A3A);
        for (int i = 0; i < 5; i++) {
            float ang = -MathUtils.HALF_PI + i * MathUtils.PI2 / 5f;
            b.stroke(36, 42, 36 + MathUtils.cos(ang) * 26f, 42 + MathUtils.sin(ang) * 24f, 10f, star, 1f);
            b.stroke(36, 42, 36 + MathUtils.cos(ang) * 22f, 42 + MathUtils.sin(ang) * 20f, 4f, Painter.rgb(0xFFC27A), 1f);
        }
        b.sphere(36, 42, 9f, star);
    }

    private static void rocks(ArtSheet s) {
        for (int v = 0; v < ROCK_VARIANTS; v++) {
            Painter p = s.alloc("rock" + v, 130, 100);
            Color g = Painter.mix(Painter.rgb(0xC6B79C), Painter.rgb(0xB0A088), v * 0.3f, new Color());
            p.softEllipse(65, 86, 52, 10, new Color(0.08f, 0.1f, 0.2f, 1f), 0.4f);
            p.ellipse(60, 62, 46 - v * 4, 30 - v * 2, g);
            p.ellipse(96, 72, 22, 16, Painter.mix(g, Color.WHITE, 0.1f, new Color()));
            p.ellipse(52, 40, 28 - v * 2, 12, Painter.rgb(0xE6D8B8));
            p.ellipse(44, 38, 14, 6, Painter.rgb(0xF6EAD0));
        }
    }

    private static void log(ArtSheet s) {
        Painter p = s.alloc("log0", 220, 110);
        Color d = Painter.rgb(0x9C8A70), m = Painter.rgb(0xBCAA8E), l = Painter.rgb(0xDCCBAE);
        p.softEllipse(110, 92, 90, 12, new Color(0.08f, 0.1f, 0.2f, 1f), 0.35f);
        for (float x = 30; x <= 190; x += 3) p.disc(x, 68, 27, d, 1f);
        for (float x = 30; x <= 190; x += 3) p.disc(x, 62, 19, m, 1f);
        for (float x = 30; x <= 190; x += 3) p.disc(x, 54, 7, l, 0.6f);
        p.ellipse(190, 66, 16, 27, Painter.rgb(0xE8D9BC));
        p.ellipse(190, 66, 10, 18, Painter.rgb(0xC9B48E));
        p.ellipse(190, 66, 4, 8, Painter.rgb(0xE8D9BC));
    }

    private static void leaf(ArtSheet s) {
        Painter p = s.alloc("leaf", 40, 40);
        p.ellipse(20, 20, 16, 8, Painter.rgb(0x8EDC5A));
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
        Color g1 = Painter.rgb(0x9C8872), g2 = Painter.rgb(0xB8A28A), g3 = Painter.rgb(0xD0BEA6);
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
