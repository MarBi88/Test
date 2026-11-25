package game.lumi.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import game.lumi.art.ArtFactory;
import game.lumi.art.ArtSheet;
import game.lumi.config.GameConfig;
import game.lumi.entities.Decor;

/**
 * Der Zauberwald als Daten: organische Begehbarkeitsform, Bodenfarben, Weg, Teich, Dekoration.
 * Wird deterministisch (fester Seed) erzeugt, damit Position/Fortschritt im Speicherstand stimmen.
 */
public final class WorldMap {
    public static final int N = GameConfig.WORLD_TILES;
    public static final float CX = N / 2f, CY = N / 2f;
    private static final float R = 25f;

    /** Bodenfleck (weiches Oval in Bildschirmgröße). */
    public static final class Decal {
        public float sx, sy, w, h;
        public float colorBits;
        public Color color = new Color();
        public int kind; // 0 normal, 1 Wasser
        public float phase;
    }

    public final Array<Decor> decor = new Array<>();
    public final Array<Decal> decals = new Array<>();
    public final Array<Decal> waterDecals = new Array<>();

    // Hindernisse (Kreise) für die Kollision
    private final FloatArray obX = new FloatArray(), obY = new FloatArray(), obR = new FloatArray();

    // Boden-Kacheln
    public final int[] tileVariant = new int[N * N];
    public final float[] tileTint = new float[N * N];
    public final boolean[] tileActive = new boolean[N * N];

    public float spawnX = CX, spawnY = CY;
    public float pondX = CX + 11f, pondY = CY + 9.5f, pondRx = 4.6f, pondRy = 3.2f;
    private final FloatArray pathX = new FloatArray(), pathY = new FloatArray();

    /** Geheime Lichtung (magischer Platz): von Büschen umringt, Zugang nur durch das Ranken-Tor. */
    public static final float SECRET_R = 3.4f;
    public final float secretX = CX + 9.5f, secretY = CY - 16f;
    public final float gateAngle = MathUtils.atan2(3f, 2.5f);
    public boolean gateOpen;
    public final Array<Decor> gateDecor = new Array<>();
    private final FloatArray gX = new FloatArray(), gY = new FloatArray();

    public float boundsMinX, boundsMaxX, boundsMinY, boundsMaxY;

    private final RandomXS128 rnd = new RandomXS128(2024);
    private final Color tmpC = new Color();

    public WorldMap(ArtSheet art) {
        buildPath();
        buildTiles();
        buildGroundDecals();
        buildDecor(art);
        decor.sort((a, b) -> Float.compare(a.depth(), b.depth()));
        for (Decor d : decor) {
            d.finish();
            if (d.solid > 0f) { obX.add(d.x); obY.add(d.y); obR.add(d.solid); }
        }
    }

    // ------------------------------------------------------------------ Form

    /** < 1 innen, ≥ 1 außerhalb des begehbaren Bereichs. */
    public float field(float x, float y) {
        float dx = x - CX, dy = y - CY;
        float ang = MathUtils.atan2(dy, dx);
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float lim = R * (1f + 0.10f * MathUtils.sin(ang * 3f + 0.7f) + 0.07f * MathUtils.sin(ang * 5f + 2.1f)
                + 0.04f * MathUtils.sin(ang * 9f));
        return r / lim;
    }

    public boolean walkable(float x, float y) {
        if (field(x, y) > 0.93f) return false;
        float px = (x - pondX) / (pondRx * 0.92f), py = (y - pondY) / (pondRy * 0.92f);
        return px * px + py * py > 1f;
    }

    /**
     * Verschiebt (x,y) um (dx,dy). Kollision mit Kreis-Hindernissen durch Herausschieben entlang der Normalen:
     * Lumi gleitet dadurch weich an Bäumen, Büschen und Steinen vorbei, statt hängen zu bleiben.
     * Ergebnis in out[0..1].
     */
    public void move(float x, float y, float dx, float dy, float radius, float[] out) {
        float nx = x + dx, ny = y + dy;
        if (!walkable(nx, ny)) { // Rand/Teich: achsenweise gleiten
            if (walkable(x + dx, y)) ny = y; else if (walkable(x, y + dy)) nx = x; else { nx = x; ny = y; }
        }
        for (int pass = 0; pass < 3; pass++) {
            boolean moved = false;
            for (int i = 0; i < obX.size; i++) { if (push(obX.items[i], obY.items[i], obR.items[i] + radius, nx, ny, dx, dy)) { nx = pushX; ny = pushY; moved = true; } }
            if (!gateOpen) for (int i = 0; i < gX.size; i++) { if (push(gX.items[i], gY.items[i], 0.55f + radius, nx, ny, dx, dy)) { nx = pushX; ny = pushY; moved = true; } }
            if (!moved) break;
        }
        if (!walkable(nx, ny)) { nx = x; ny = y; }
        out[0] = nx; out[1] = ny;
    }

    private float pushX, pushY;

    private boolean push(float ox, float oy, float rr, float nx, float ny, float dx, float dy) {
        float ex = nx - ox, ey = ny - oy;
        float d2 = ex * ex + ey * ey;
        if (d2 >= rr * rr) return false;
        float d = (float) Math.sqrt(d2);
        if (d < 1e-4f) { ex = -dx; ey = -dy; d = (float) Math.sqrt(ex * ex + ey * ey); if (d < 1e-6f) { ex = 1f; ey = 0f; d = 1f; } }
        pushX = ox + ex / d * rr; pushY = oy + ey / d * rr;
        return true;
    }

    public boolean free(float x, float y, float radius) {
        if (!walkable(x, y)) return false;
        if (!gateOpen) {
            for (int i = 0; i < gX.size; i++) {
                float ddx = x - gX.items[i], ddy = y - gY.items[i];
                float rr = radius + 0.55f;
                if (ddx * ddx + ddy * ddy < rr * rr) return false;
            }
        }
        for (int i = 0; i < obX.size; i++) {
            float ddx = x - obX.items[i], ddy = y - obY.items[i];
            float rr = radius + obR.items[i];
            if (ddx * ddx + ddy * ddy < rr * rr) return false;
        }
        return true;
    }

    /** Zusätzliches festes Hindernis (z. B. magischer Stein). Vor der Welt-Fertigstellung aufrufen oder direkt nutzen. */
    public void addObstacle(float x, float y, float r) { obX.add(x); obY.add(y); obR.add(r); }

    public boolean inSecret(float x, float y, float extra) {
        float dx = x - secretX, dy = y - secretY;
        float rr = SECRET_R + extra;
        return dx * dx + dy * dy < rr * rr;
    }

    public float gateX() { return Iso.sx(secretX + MathUtils.cos(gateAngle) * SECRET_R, secretY + MathUtils.sin(gateAngle) * SECRET_R); }
    public float gateY() { return Iso.sy(secretX + MathUtils.cos(gateAngle) * SECRET_R, secretY + MathUtils.sin(gateAngle) * SECRET_R); }

    public void gateAlphaReset(boolean open) { for (Decor d : gateDecor) d.alpha = open ? 0f : 1f; }

    private void buildSecretRing(Array<TextureRegion> bushes, Array<TextureRegion> rocks, Array<TextureRegion> trees, TextureRegion vine) {
        float step = 0.25f;
        int k = 0;
        for (float a = gateAngle + 0.34f; a < gateAngle + MathUtils.PI2 - 0.34f; a += step, k++) {
            float r = SECRET_R + rf(-0.05f, 0.05f);
            float x = secretX + MathUtils.cos(a) * r, y = secretY + MathUtils.sin(a) * r;
            Decor d;
            switch (k % 4) {
                case 1: d = add(rocks.get(k % rocks.size), x, y, 0.95f, 14f); d.solid = 0.34f * d.scale; d.shadowW = 120f * d.scale; d.shadowH = 44f * d.scale; break;
                case 3: d = add(trees.get(1), x, y, 0.62f, ArtFactory.TREE_ANCHOR); d.solid = 0.28f; d.sway = 4f; d.swaySpeed = rf(0.7f, 1f);
                        d.shadowW = 200f * d.scale; d.shadowH = 90f * d.scale; d.occluder = true; break;
                default: d = add(bushes.get(k % bushes.size), x, y, 1.15f, 16f); d.solid = 0.30f * d.scale; d.sway = 3f; d.swaySpeed = rf(0.8f, 1.2f);
                        d.shadowW = 150f * d.scale; d.shadowH = 60f * d.scale;
            }
            d.tint = tintFor(x, y, 1.0f);
            placed.add(new float[] { x, y, 0.4f });
        }
        // Ranken-Tor: sperrt die Lücke, bis alle drei Steine leuchten
        float[] ga = { -0.2f, 0f, 0.2f };
        for (float da : ga) {
            float a = gateAngle + da;
            float x = secretX + MathUtils.cos(a) * SECRET_R, y = secretY + MathUtils.sin(a) * SECRET_R;
            Decor d = add(vine, x, y, 1.0f, 12f);
            d.gate = true; d.sway = 3f; d.swaySpeed = rf(0.9f, 1.3f); d.shadowW = 150f; d.shadowH = 56f;
            gateDecor.add(d);
        }
        float[] oa = { -0.27f, -0.09f, 0.09f, 0.27f };
        for (float da : oa) {
            float a = gateAngle + da;
            gX.add(secretX + MathUtils.cos(a) * SECRET_R); gY.add(secretY + MathUtils.sin(a) * SECRET_R);
        }
    }

    // ------------------------------------------------------------------ Weg

    private void buildPath() {
        float[][] pts = {
            { CX, CY }, { CX + 4, CY - 2 }, { CX + 8, CY - 4 }, { CX + 11, CY - 8 }, { CX + 12, CY - 13 }, { CX + 10, CY - 17 },
        };
        float[][] pts2 = {
            { CX, CY }, { CX - 4, CY + 3 }, { CX - 8, CY + 5 }, { CX - 12, CY + 9 }, { CX - 14, CY + 14 }, { CX - 13, CY + 18 },
        };
        float[][] pts3 = {
            { CX, CY }, { CX + 3, CY + 4 }, { CX + 5, CY + 7 },
        };
        for (float[][] set : new float[][][] { pts, pts2, pts3 }) {
            for (int i = 0; i < set.length - 1; i++) {
                int steps = (int) (Math.hypot(set[i + 1][0] - set[i][0], set[i + 1][1] - set[i][1]) * 3f);
                for (int s = 0; s <= steps; s++) {
                    float t = s / (float) steps;
                    pathX.add(MathUtils.lerp(set[i][0], set[i + 1][0], t));
                    pathY.add(MathUtils.lerp(set[i][1], set[i + 1][1], t));
                }
            }
        }
    }

    public float distToPath(float x, float y) {
        float best = 1e9f;
        for (int i = 0; i < pathX.size; i++) {
            float dx = x - pathX.items[i], dy = y - pathY.items[i];
            float d = dx * dx + dy * dy;
            if (d < best) best = d;
        }
        return (float) Math.sqrt(best);
    }

    // ------------------------------------------------------------------ Boden

    private float noise(float x, float y) {
        // billiges Value-Noise aus Sinus-Überlagerung (deterministisch, GWT-sicher)
        return 0.5f + 0.25f * MathUtils.sin(x * 0.37f + 1.3f) * MathUtils.cos(y * 0.41f + 0.2f)
                + 0.15f * MathUtils.sin((x + y) * 0.83f + 2f) + 0.10f * MathUtils.sin(x * 1.9f - y * 1.7f);
    }

    private void buildTiles() {
        float minSX = 1e9f, maxSX = -1e9f, minSY = 1e9f, maxSY = -1e9f;
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                int idx = j * N + i;
                float f = field(i + 0.5f, j + 0.5f);
                tileActive[idx] = f < 1.22f;
                tileVariant[idx] = (int) (noise(i * 2.1f, j * 1.7f) * 40f) % ArtFactory.TILE_VARIANTS;
                if (tileVariant[idx] < 0) tileVariant[idx] += ArtFactory.TILE_VARIANTS;
                float n = noise(i, j);
                float b = 0.86f + n * 0.26f;
                // zur Mitte hin sonniger, zum Rand tiefer/dunkler
                float depthF = MathUtils.clamp(f, 0f, 1.2f);
                b *= 1.06f - depthF * 0.30f;
                float warm = MathUtils.clamp(1f - depthF * 1.6f, 0f, 1f);
                tmpC.set(b * (0.98f + warm * 0.07f), b * (1.0f + warm * 0.02f), b * (0.94f - warm * 0.02f + (n - 0.5f) * 0.1f), 1f);
                tileTint[idx] = tmpC.toFloatBits();
                if (f < 1.0f) {
                    float sx = Iso.sx(i + 0.5f, j + 0.5f), sy = Iso.sy(i + 0.5f, j + 0.5f);
                    minSX = Math.min(minSX, sx); maxSX = Math.max(maxSX, sx);
                    minSY = Math.min(minSY, sy); maxSY = Math.max(maxSY, sy);
                }
            }
        }
        boundsMinX = minSX; boundsMaxX = maxSX; boundsMinY = minSY; boundsMaxY = maxSY;
    }

    private void addDecal(Array<Decal> list, float wx, float wy, float wRadius, float flat, Color c, float alpha, int kind) {
        Decal d = new Decal();
        d.sx = Iso.sx(wx, wy);
        d.sy = Iso.sy(wx, wy);
        d.w = wRadius * 2f * Iso.HW * 1.414f;
        d.h = d.w * 0.5f * flat;
        d.color.set(c.r, c.g, c.b, alpha);
        d.colorBits = d.color.toFloatBits();
        d.kind = kind;
        d.phase = rnd.nextFloat() * MathUtils.PI2;
        list.add(d);
    }

    private void buildGroundDecals() {
        Color sun = Painter_rgb(0xFFE98A), lightGrass = Painter_rgb(0x9ED45A), darkGrass = Painter_rgb(0x2F7E4C),
                dirt = Painter_rgb(0xC9A06A), dirtDark = Painter_rgb(0xB48655), moss = Painter_rgb(0x4D9A55);
        // Sonnige Lichtung
        for (int i = 0; i < 6; i++) addDecal(decals, CX + rf(-2, 2), CY + rf(-2, 2), 4.5f + rf(0, 2.5f), 1f, sun, 0.16f, 0);
        // Flecken in Gras-Tönen für organische Bodenstruktur
        for (int i = 0; i < 260; i++) {
            float x = rf(4, N - 4), y = rf(4, N - 4);
            float f = field(x, y);
            if (f > 1.15f) continue;
            boolean light = rnd.nextFloat() < 0.5f;
            addDecal(decals, x, y, rf(1.2f, 3.4f), 1f, light ? lightGrass : darkGrass, light ? 0.20f : 0.24f, 0);
        }
        for (int i = 0; i < 70; i++) {
            float x = rf(6, N - 6), y = rf(6, N - 6);
            if (field(x, y) > 1.0f) continue;
            addDecal(decals, x, y, rf(0.8f, 1.8f), 1f, moss, 0.30f, 0);
        }
        // Weg aus weichen Erdflecken
        for (int i = 0; i < pathX.size; i += 1) {
            float x = pathX.items[i] + rf(-0.18f, 0.18f), y = pathY.items[i] + rf(-0.18f, 0.18f);
            addDecal(decals, x, y, rf(0.95f, 1.25f), 1f, dirtDark, 0.30f, 0);
        }
        for (int i = 0; i < pathX.size; i += 1) {
            float x = pathX.items[i] + rf(-0.15f, 0.15f), y = pathY.items[i] + rf(-0.15f, 0.15f);
            addDecal(decals, x, y, rf(0.65f, 0.9f), 1f, dirt, 0.50f, 0);
        }
        // Magischer Platz: sanftes violettes Leuchten am Boden der geheimen Lichtung
        addDecal(decals, secretX, secretY, 3.0f, 1f, Painter_rgb(0xB48CF0), 0.30f, 0);
        addDecal(decals, secretX, secretY, 1.7f, 1f, Painter_rgb(0xE4D2FF), 0.35f, 0);
        // Teich: Ufer (Creme), Wasser (zwei Töne)
        Color shore = Painter_rgb(0xE9D9A8), w1 = Painter_rgb(0x3FB6C8), w2 = Painter_rgb(0x6FD6E0);
        addDecal(decals, pondX, pondY, pondRx + 0.9f, 1f, Painter_rgb(0x7FBF50), 0.5f, 0);
        addDecal(decals, pondX, pondY, pondRx + 0.5f, 1f, shore, 0.85f, 0);
        addDecal(waterDecals, pondX, pondY, pondRx + 0.05f, 1f, w1, 1f, 1);
        addDecal(waterDecals, pondX - 0.3f, pondY - 0.3f, pondRx * 0.75f, 1f, w2, 0.75f, 1);
        addDecal(waterDecals, pondX + 0.5f, pondY + 0.4f, pondRx * 0.5f, 1f, Painter_rgb(0x2E9DB8), 0.5f, 1);
    }

    private static Color Painter_rgb(int hex) { return game.lumi.art.Painter.rgb(hex); }

    private float rf(float a, float b) { return a + rnd.nextFloat() * (b - a); }

    // ------------------------------------------------------------------ Dekoration

    private final Array<float[]> placed = new Array<>();

    private boolean spaceFor(float x, float y, float minDist) {
        for (float[] p : placed) {
            float dx = x - p[0], dy = y - p[1];
            float md = Math.max(minDist, p[2]);
            if (dx * dx + dy * dy < md * md) return false;
        }
        return true;
    }

    private Decor add(TextureRegion r, float x, float y, float scale, float anchor) {
        Decor d = new Decor();
        d.region = r; d.x = x; d.y = y; d.scale = scale; d.anchor = anchor;
        d.flip = rnd.nextBoolean();
        d.swayPhase = rnd.nextFloat() * MathUtils.PI2;
        decor.add(d);
        return d;
    }

    private float tintFor(float x, float y, float base) {
        float f = MathUtils.clamp(field(x, y), 0f, 1.2f);
        float b = base * (1.04f - f * 0.22f) + (rnd.nextFloat() - 0.5f) * 0.10f;
        tmpC.set(b, b * 1.01f, b * (0.97f + (rnd.nextFloat() - 0.5f) * 0.08f), 1f);
        return tmpC.toFloatBits();
    }

    private boolean nearSpawn(float x, float y, float r) {
        float dx = x - spawnX, dy = y - spawnY;
        return dx * dx + dy * dy < r * r;
    }

    private boolean inPond(float x, float y, float extra) {
        float px = (x - pondX) / (pondRx + extra), py = (y - pondY) / (pondRy + extra);
        return px * px + py * py < 1f;
    }

    private void buildDecor(ArtSheet art) {
        Array<TextureRegion> trees = art.family("tree");
        Array<TextureRegion> bushes = art.family("bush");
        Array<TextureRegion> flowers = art.family("flower");
        Array<TextureRegion> tufts = art.family("tuft");
        Array<TextureRegion> mush = art.family("mushroom");
        Array<TextureRegion> rocks = art.family("rock");
        TextureRegion log = art.get("log0");

        buildSecretRing(bushes, rocks, trees, art.get("vine"));
        // --- Bäume: erst dichter Waldrand, dann lichter Innenraum
        int tries = 0;
        while (tries++ < 6000) {
            float x = rf(2, N - 2), y = rf(2, N - 2);
            float f = field(x, y);
            if (f > 1.18f || inSecret(x, y, 0.9f)) continue;
            boolean rim = f > 0.80f;
            if (!rim && (nearSpawn(x, y, 7.5f) || distToPath(x, y) < 1.7f || inPond(x, y, 2.0f))) continue;
            if (rim && (distToPath(x, y) < 1.0f && f < 0.95f)) continue;
            float gap = rim ? 1.7f : 3.8f;
            if (!spaceFor(x, y, gap)) continue;
            int v = rnd.nextInt(trees.size);
            if (rim && v == 1 && rnd.nextFloat() < 0.5f) v = 2;
            float scale = rim ? rf(0.78f, 1.1f) : rf(0.58f, 0.9f);
            Decor d = add(trees.get(v), x, y, scale, ArtFactory.TREE_ANCHOR);
            d.sway = 5f * scale; d.swaySpeed = rf(0.6f, 0.95f);
            d.tint = tintFor(x, y, 1.0f);
            d.shadowW = 230f * scale; d.shadowH = 110f * scale;
            d.solid = (v == 3 ? 0.45f : 0.30f) * scale;
            d.occluder = true;
            placed.add(new float[] { x, y, gap * 0.5f });
        }
        // --- Büsche
        tries = 0; int count = 0;
        while (tries++ < 3000 && count < 110) {
            float x = rf(3, N - 3), y = rf(3, N - 3);
            if (inSecret(x, y, 0.9f) || field(x, y) > 1.0f || nearSpawn(x, y, 3.2f) || distToPath(x, y) < 0.9f || inPond(x, y, 1.3f)) continue;
            if (!spaceFor(x, y, 1.5f)) continue;
            Decor d = add(bushes.get(rnd.nextInt(bushes.size)), x, y, rf(0.65f, 1.0f), 16f);
            d.sway = 3f; d.swaySpeed = rf(0.8f, 1.2f); d.tint = tintFor(x, y, 1.0f);
            d.shadowW = 150f * d.scale; d.shadowH = 60f * d.scale; d.solid = 0.28f * d.scale;
            placed.add(new float[] { x, y, 0.8f }); count++;
        }
        // --- Steine
        tries = 0; count = 0;
        while (tries++ < 2000 && count < 26) {
            float x = rf(3, N - 3), y = rf(3, N - 3);
            if (inSecret(x, y, 0.9f) || field(x, y) > 0.95f || nearSpawn(x, y, 3f) || distToPath(x, y) < 0.8f) continue;
            if (!spaceFor(x, y, 1.5f)) continue;
            Decor d = add(rocks.get(rnd.nextInt(rocks.size)), x, y, rf(0.55f, 0.95f), 14f);
            d.tint = tintFor(x, y, 1.0f); d.solid = 0.32f * d.scale; d.shadowW = 120f * d.scale; d.shadowH = 44f * d.scale;
            placed.add(new float[] { x, y, 0.8f }); count++;
        }
        // --- Baumstämme
        tries = 0; count = 0;
        while (tries++ < 1000 && count < 5) {
            float x = rf(6, N - 6), y = rf(6, N - 6);
            if (inSecret(x, y, 0.9f) || field(x, y) > 0.85f || nearSpawn(x, y, 5f) || distToPath(x, y) < 1.5f || inPond(x, y, 2f) || !spaceFor(x, y, 2.4f)) continue;
            Decor d = add(log, x, y, rf(0.8f, 1.0f), 22f);
            d.tint = tintFor(x, y, 1.0f); d.solid = 0.45f * d.scale; d.shadowW = 190f * d.scale; d.shadowH = 56f * d.scale;
            placed.add(new float[] { x, y, 1.2f }); count++;
        }
        // --- Pilze (an Baumrändern)
        tries = 0; count = 0;
        while (tries++ < 2000 && count < 34) {
            float x = rf(3, N - 3), y = rf(3, N - 3);
            if (inSecret(x, y, 0.9f) || field(x, y) > 0.95f || nearSpawn(x, y, 2.5f) || distToPath(x, y) < 0.7f || inPond(x, y, 1.0f)) continue;
            Decor d = add(mush.get(rnd.nextInt(mush.size)), x, y, rf(0.7f, 1.1f), 10f);
            d.tint = tintFor(x, y, 1.0f); d.shadowW = 50f * d.scale; d.shadowH = 20f * d.scale;
            count++;
        }
        // --- Blumenwiesen (Gruppen) + einzelne Blumen
        for (int c = 0; c < 22; c++) {
            float cx = rf(5, N - 5), cy = rf(5, N - 5);
            if (field(cx, cy) > 0.92f || inPond(cx, cy, 1f)) continue;
            int kind = rnd.nextInt(flowers.size);
            int n = 6 + rnd.nextInt(10);
            for (int k = 0; k < n; k++) {
                float x = cx + rf(-2.2f, 2.2f), y = cy + rf(-2.2f, 2.2f);
                if (field(x, y) > 0.95f || inPond(x, y, 0.6f) || distToPath(x, y) < 0.5f) continue;
                int v = rnd.nextFloat() < 0.65f ? kind : rnd.nextInt(flowers.size);
                Decor d = add(flowers.get(v), x, y, rf(0.75f, 1.15f), 6f);
                d.sway = 5f; d.swaySpeed = rf(1.1f, 1.8f); d.tint = tintFor(x, y, 1.02f);
                d.shadowW = 28f; d.shadowH = 12f;
            }
        }
        // --- Grasbüschel
        tries = 0; count = 0;
        while (tries++ < 4000 && count < 320) {
            float x = rf(2, N - 2), y = rf(2, N - 2);
            if (field(x, y) > 1.0f || inPond(x, y, 0.2f) || distToPath(x, y) < 0.45f) continue;
            Decor d = add(tufts.get(rnd.nextInt(tufts.size)), x, y, rf(0.8f, 1.5f), 4f);
            d.sway = 7f; d.swaySpeed = rf(1.2f, 2.0f); d.tint = tintFor(x, y, 1.0f);
            count++;
        }
        // --- Schilf am Teichufer
        for (int k = 0; k < 40; k++) {
            float a = rnd.nextFloat() * MathUtils.PI2;
            float x = pondX + MathUtils.cos(a) * (pondRx + rf(0.4f, 1.0f)), y = pondY + MathUtils.sin(a) * (pondRy + rf(0.4f, 1.0f));
            if (distToPath(x, y) < 0.9f) continue;
            Decor d = add(tufts.get(rnd.nextInt(tufts.size)), x, y, rf(1.1f, 1.7f), 4f);
            d.sway = 9f; d.swaySpeed = rf(1.0f, 1.6f); d.tint = tintFor(x, y, 1.05f);
        }
    }
}
