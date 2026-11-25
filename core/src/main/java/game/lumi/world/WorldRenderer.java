package game.lumi.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.Array;
import game.lumi.art.ArtFactory;
import game.lumi.art.ArtSheet;
import game.lumi.entities.Decor;
import game.lumi.entities.Entity;
import game.lumi.entities.Player;

/**
 * Zeichnet die Welt in fester Reihenfolge: Boden → Bodenflecken → Wasser → Schatten →
 * tiefensortierte Objekte (inkl. Lumi) → additive Leuchteffekte → Lichtstrahlen → Blätter.
 * Im Render-Loop werden keine Objekte angelegt.
 */
public final class WorldRenderer {
    private final TextureRegion[] tiles = new TextureRegion[ArtFactory.TILE_VARIANTS];
    private final TextureRegion blob, shadow, glow, ring, ray, vignette, crystal;
    private final float[] verts = new float[40];
    private float time;
    private final Entity[] dyn = new Entity[12];
    /** Entwickler-Hilfe: 0 Kacheln, 1 Bodenflecken, 2 Objekte, 3 Schatten ausblenden. */
    public final boolean[] hide = new boolean[4];

    public WorldRenderer(ArtSheet art) {
        for (int i = 0; i < tiles.length; i++) tiles[i] = art.get("tile" + i);
        blob = art.get("blob"); shadow = art.get("shadow"); glow = art.get("glow"); ring = art.get("ui_ring");
        crystal = art.get("crystal"); ray = art.get("ray"); vignette = art.get("vignette");
    }

    public void render(SpriteBatch b, GameWorld w, WorldCamera cam, float dt) {
        time += dt;
        WorldMap map = w.map;
        Player p = w.player;
        float L = cam.left() - 140f, R = cam.right() + 140f, B = cam.bottom() - 160f, T = cam.top() + 260f;

        b.setProjectionMatrix(cam.camera.combined);
        b.begin();
        b.setColor(Color.WHITE);

        // ---- Boden-Kacheln
        final int n = WorldMap.N;
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                int idx = j * n + i;
                if (!map.tileActive[idx] || hide[0]) continue;
                float sx = Iso.sx(i + 0.5f, j + 0.5f), sy = Iso.sy(i + 0.5f, j + 0.5f);
                if (sx < L - 66f || sx > R + 66f || sy < B - 34f || sy > T + 34f) continue;
                b.setPackedColor(map.tileTint[idx]);
                b.draw(tiles[map.tileVariant[idx]], sx - 66f, sy - 34f, 132f, 68f);
            }
        }
        b.setColor(Color.WHITE);

        // ---- Bodenflecken (organische Struktur, Weg, Ufer)
        Array<WorldMap.Decal> decals = map.decals;
        for (int i = 0; i < decals.size; i++) {
            WorldMap.Decal d = decals.get(i);
            if (hide[1] || d.sx + d.w * 0.5f < L || d.sx - d.w * 0.5f > R || d.sy + d.h * 0.5f < B || d.sy - d.h * 0.5f > T) continue;
            b.setPackedColor(d.colorBits);
            b.draw(blob, d.sx - d.w * 0.5f, d.sy - d.h * 0.5f, d.w, d.h);
        }

        // ---- Wasser (sanft animiert)
        Array<WorldMap.Decal> water = map.waterDecals;
        for (int i = 0; i < water.size; i++) {
            WorldMap.Decal d = water.get(i);
            float pulse = 1f + 0.015f * MathUtils.sin(time * 0.9f + d.phase);
            float ww = d.w * pulse, hh = d.h * pulse;
            float shimmer = 0.92f + 0.08f * MathUtils.sin(time * 1.3f + d.phase * 2f);
            b.setColor(d.color.r * shimmer, d.color.g * shimmer, d.color.b * shimmer, d.color.a);
            b.draw(blob, d.sx + MathUtils.sin(time * 0.5f + d.phase) * 4f - ww * 0.5f, d.sy - hh * 0.5f, ww, hh);
        }
        // Ringwellen
        float px = Iso.sx(map.pondX, map.pondY), py = Iso.sy(map.pondX, map.pondY);
        for (int k = 0; k < 4; k++) {
            float t = (time * 0.18f + k * 0.25f) % 1f;
            float s = 60f + t * 240f;
            b.setColor(1f, 1f, 1f, (1f - t) * 0.28f);
            b.draw(ring, px + (k - 1.5f) * 34f - s * 0.5f, py + MathUtils.sin(k * 2.3f) * 16f - s * 0.25f, s, s * 0.5f);
        }
        b.setColor(Color.WHITE);

        // ---- Spieler-Verdeckungsrechteck
        float pSX = p.screenX(), pSY = p.screenY();
        float pl = pSX - 70f, pr = pSX + 70f, pb = pSY - 6f, pt = pSY + 175f + p.z;
        float pDepth = p.depth();

        // ---- Schatten
        Array<Decor> decor = map.decor;
        for (int i = 0; i < decor.size; i++) {
            Decor d = decor.get(i);
            if (hide[3] || d.shadowW <= 0f || d.right < L || d.left > R || d.top < B || d.bottom > T) continue;
            b.setColor(1f, 1f, 1f, 0.8f * d.alpha);
            float sx = d.screenX(), sy = d.screenY();
            b.draw(shadow, sx - d.shadowW * 0.5f + d.shadowW * 0.10f, sy - d.shadowH * 0.5f - d.shadowH * 0.06f, d.shadowW, d.shadowH);
        }
        b.setColor(Color.WHITE);
        w.lumi.drawShadow(b, pSX, pSY, p.z);

        // ---- Objekte tiefensortiert (statische Liste + wenige bewegliche Figuren, per Einfügesort pro Frame)
        int dn = 0;
        dyn[dn++] = p;
        for (int i = 0; i < w.animals.size && dn < dyn.length; i++) dyn[dn++] = w.animals.get(i);
        for (int i = 1; i < dn; i++) {
            Entity e = dyn[i]; int j = i - 1;
            while (j >= 0 && dyn[j].depth() > e.depth()) { dyn[j + 1] = dyn[j]; j--; }
            dyn[j + 1] = e;
        }
        int di = 0, ci = 0;
        Array<game.lumi.entities.Collectible> cr = w.crystals;
        float fade = Math.min(1f, 9f * dt);
        for (int i = 0; i < decor.size; i++) {
            Decor d = decor.get(i);
            float dd = d.depth();
            while (ci < cr.size && cr.get(ci).depth() <= dd) drawCrystal(b, cr.get(ci++), L, R, B, T);
            while (di < dn && dyn[di].depth() <= dd) drawDyn(b, w, dyn[di++]);
            if (d.occluder) {
                boolean hides = dd > pDepth && dd < pDepth + 7f && d.left < pr && d.right > pl && d.top > pb && d.bottom < pt
                        && overlapsCanopy(d, pl, pr, pb, pt);
                d.alpha += ((hides ? 0.34f : 1f) - d.alpha) * fade;
            }
            if (hide[2] || d.right < L || d.left > R || d.top < B || d.bottom > T) continue;
            drawDecor(b, d);
        }
        while (ci < cr.size) drawCrystal(b, cr.get(ci++), L, R, B, T);
        while (di < dn) drawDyn(b, w, dyn[di++]);
        b.setColor(Color.WHITE);
        for (int i = 0; i < w.animals.size; i++) w.animalArt.drawBubble(b, w.animals.get(i), time);
        b.setColor(Color.WHITE);

        // ---- Leuchteffekte (additiv)
        b.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        w.lumi.drawGlow(b, pSX, pSY, p.z, w.lumi.isBackView());
        drawWaterSparkles(b, map);
        for (int i = 0; i < cr.size; i++) drawCrystalGlow(b, cr.get(i), L, R, B, T);
        w.particles.drawGlows(b);
        drawLightShafts(b, cam);
        b.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        w.particles.drawLeaves(b);
        b.end();
    }

    /** Verdeckt die Baumkrone Lumi wirklich (obere ~65 % des Sprites), nicht nur der Stamm? */
    private boolean overlapsCanopy(Decor d, float pl, float pr, float pb, float pt) {
        float canopyBottom = d.bottom + (d.top - d.bottom) * 0.30f;
        return pt > canopyBottom;
    }

    private void drawDyn(SpriteBatch b, GameWorld w, Entity e) {
        if (e == w.player) w.lumi.draw(b, w.player.screenX(), w.player.screenY(), w.player.z);
        else w.animalArt.draw(b, (game.lumi.entities.Animal) e);
    }

    private void drawCrystal(SpriteBatch b, game.lumi.entities.Collectible c, float L, float R, float B, float T) {
        if (c.vanish >= 1f || !c.active) return;
        float sx = c.screenX(), sy = c.screenY();
        if (sx < L - 60f || sx > R + 60f || sy < B - 60f || sy > T + 60f) return;
        float k = 1f - c.vanish;
        float bob = MathUtils.sin(time * 1.8f + c.phase) * 4f + 6f + c.vanish * 50f;
        float w = 80f * 0.8f * k, h = 100f * 0.8f * k;
        b.setColor(1f, 1f, 1f, k);
        b.draw(shadow, sx - 24f, sy - 8f, 48f, 20f);
        b.setColor(1f, 1f, 1f, k);
        b.draw(crystal, sx - w / 2f, sy - 9f * 0.8f + bob, w, h);
        b.setColor(Color.WHITE);
    }

    private void drawCrystalGlow(SpriteBatch b, game.lumi.entities.Collectible c, float L, float R, float B, float T) {
        if (c.vanish >= 1f || !c.active) return;
        float sx = c.screenX(), sy = c.screenY();
        if (sx < L - 100f || sx > R + 100f || sy < B - 100f || sy > T + 100f) return;
        float pulse = 0.55f + 0.45f * MathUtils.sin(time * 2.2f + c.phase);
        float k = 1f - c.vanish;
        float s = (54f + pulse * 22f) * k;
        float cy = sy + 40f + MathUtils.sin(time * 1.8f + c.phase) * 4f + c.vanish * 50f;
        b.setColor(0.3f, 1f, 0.9f, (0.18f + pulse * 0.28f) * k);
        b.draw(glow, sx - s, cy - s, s * 2f, s * 2f);
        b.setColor(0.8f, 1f, 1f, 0.5f * pulse * k);
        b.draw(glow, sx - 14f, cy - 14f, 28f, 28f);
    }

    private void drawWaterSparkles(SpriteBatch b, WorldMap map) {
        float cx = Iso.sx(map.pondX, map.pondY), cy = Iso.sy(map.pondX, map.pondY);
        for (int i = 0; i < 16; i++) {
            float a = i * 2.399f;
            float rr = 0.25f + 0.7f * ((i * 0.37f) % 1f);
            float x = cx + MathUtils.cos(a) * rr * map.pondRx * 70f;
            float y = cy + MathUtils.sin(a) * rr * map.pondRx * 35f;
            float tw = MathUtils.sin(time * (1.4f + (i % 5) * 0.3f) + i * 1.7f);
            if (tw < 0.2f) continue;
            float s = 10f + tw * 16f;
            b.setColor(0.8f, 1f, 1f, tw * 0.5f);
            b.draw(glow, x - s * 0.5f, y - s * 0.25f, s, s * 0.5f);
        }
    }

    private void drawLightShafts(SpriteBatch b, WorldCamera cam) {
        for (int i = 0; i < 4; i++) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(time * 0.35f + i * 1.9f);
            float x = cam.camera.position.x + (i - 1.5f) * 340f + MathUtils.sin(time * 0.15f + i) * 40f;
            float y = cam.camera.position.y + 40f;
            b.setColor(1f, 0.93f, 0.62f, 0.035f + 0.035f * pulse);
            b.draw(ray, x - 70f, y - 380f, 70f, 380f, 140f, 760f, 1f, 1f, -24f);
        }
        b.setColor(Color.WHITE);
    }

    private void drawDecor(SpriteBatch b, Decor d) {
        TextureRegion r = d.region;
        float w = r.getRegionWidth() * d.scale, h = r.getRegionHeight() * d.scale;
        float x = d.screenX() - w * 0.5f, y = d.screenY() - d.anchor * d.scale;
        float col = Float.isNaN(d.tint) ? Color.WHITE_FLOAT_BITS : d.tint;
        if (d.alpha < 0.999f) {
            int ci = NumberUtils.floatToIntColor(Float.isNaN(d.tint) ? Color.WHITE.toFloatBits() : d.tint);
            ci = (ci & 0x00FFFFFF) | (((int) (d.alpha * 254f)) << 24);
            col = NumberUtils.intToFloatColor(ci);
        }
        float shift = d.sway > 0f
                ? MathUtils.sin(time * d.swaySpeed + d.swayPhase) * d.sway * 0.7f + MathUtils.sin(time * d.swaySpeed * 2.3f + d.swayPhase * 1.7f) * d.sway * 0.3f
                : 0f;
        float u = d.flip ? r.getU2() : r.getU(), u2 = d.flip ? r.getU() : r.getU2();
        float v = r.getV(), v2 = r.getV2();
        if (d.sway <= 0f) {
            b.setPackedColor(col);
            if (d.flip) b.draw(r, x + w, y, -w, h); else b.draw(r, x, y, w, h);
            return;
        }
        // Zwei Streifen: unten fast starr, oben stärker ausgelenkt (Stamm bleibt stehen, Krone wiegt sich)
        float split = 0.38f;
        float vm = v2 + (v - v2) * split;
        float ym = y + h * split;
        float s1 = shift * 0.10f, s2 = shift;
        int k = 0;
        k = vert(verts, k, x, y, col, u, v2);
        k = vert(verts, k, x + s1, ym, col, u, vm);
        k = vert(verts, k, x + w + s1, ym, col, u2, vm);
        k = vert(verts, k, x + w, y, col, u2, v2);
        k = vert(verts, k, x + s1, ym, col, u, vm);
        k = vert(verts, k, x + s2, y + h, col, u, v);
        k = vert(verts, k, x + w + s2, y + h, col, u2, v);
        k = vert(verts, k, x + w + s1, ym, col, u2, vm);
        b.draw(r.getTexture(), verts, 0, 40);
    }

    private static int vert(float[] a, int k, float x, float y, float c, float u, float v) {
        a[k] = x; a[k + 1] = y; a[k + 2] = c; a[k + 3] = u; a[k + 4] = v;
        return k + 5;
    }

    /** Bildschirmfüllende Stimmung (Vignette), im UI-Koordinatensystem zeichnen. */
    public void drawVignette(SpriteBatch b, float w, float h) {
        b.setColor(1f, 1f, 1f, 1f);
        b.draw(vignette, 0, 0, w, h);
    }
}
