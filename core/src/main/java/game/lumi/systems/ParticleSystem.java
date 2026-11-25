package game.lumi.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import game.lumi.art.ArtSheet;

/**
 * Atmosphärische Partikel mit festem Budget (keine Allokation im Render-Loop):
 * Glühwürmchen, Staubflimmern im Licht und fallende Blätter. Alle leben in
 * Bildschirmkoordinaten rund um die Kamera und werden beim Verlassen des Bildes recycelt.
 */
public final class ParticleSystem {
    private static final int FIREFLIES = 34, MOTES = 28, LEAVES = 9;

    private final float[] fx = new float[FIREFLIES], fy = new float[FIREFLIES], fPhase = new float[FIREFLIES], fSpeed = new float[FIREFLIES], fSize = new float[FIREFLIES];
    private final float[] mx = new float[MOTES], my = new float[MOTES], mPhase = new float[MOTES], mSize = new float[MOTES];
    private final float[] lx = new float[LEAVES], ly = new float[LEAVES], lRot = new float[LEAVES], lPhase = new float[LEAVES], lLife = new float[LEAVES];
    private final TextureRegion glow, leaf;
    private float time;
    private boolean seeded;
    /** 0..1: Effektstärke, bei schwacher Leistung reduzierbar (dekorative Effekte zuerst). */
    public float quality = 1f;

    public ParticleSystem(ArtSheet art) { glow = art.get("glow"); leaf = art.get("leaf"); }

    public void update(float dt, float camX, float camY, float vw, float vh) {
        time += dt;
        float hw = vw * 0.5f + 80f, hh = vh * 0.5f + 80f;
        if (!seeded) {
            for (int i = 0; i < FIREFLIES; i++) { fx[i] = camX + MathUtils.random(-hw, hw); fy[i] = camY + MathUtils.random(-hh, hh); fPhase[i] = MathUtils.random(MathUtils.PI2); fSpeed[i] = MathUtils.random(0.5f, 1.3f); fSize[i] = MathUtils.random(14f, 26f); }
            for (int i = 0; i < MOTES; i++) { mx[i] = camX + MathUtils.random(-hw, hw); my[i] = camY + MathUtils.random(-hh, hh); mPhase[i] = MathUtils.random(MathUtils.PI2); mSize[i] = MathUtils.random(5f, 11f); }
            for (int i = 0; i < LEAVES; i++) { respawnLeaf(i, camX, camY, hw, hh); ly[i] = camY + MathUtils.random(-hh, hh); }
            seeded = true;
        }
        for (int i = 0; i < FIREFLIES; i++) {
            fPhase[i] += dt * fSpeed[i];
            fx[i] += MathUtils.sin(fPhase[i] * 1.3f + i) * 14f * dt;
            fy[i] += MathUtils.cos(fPhase[i] * 0.9f + i * 2f) * 10f * dt;
            wrap(fx, fy, i, camX, camY, hw, hh);
        }
        for (int i = 0; i < MOTES; i++) {
            mPhase[i] += dt;
            mx[i] += (6f + MathUtils.sin(mPhase[i] * 0.7f + i) * 5f) * dt;
            my[i] += (3f + MathUtils.cos(mPhase[i] * 0.5f + i) * 4f) * dt;
            wrap(mx, my, i, camX, camY, hw, hh);
        }
        for (int i = 0; i < LEAVES; i++) {
            lLife[i] += dt;
            ly[i] -= (28f + 8f * MathUtils.sin(lPhase[i])) * dt;
            lx[i] += MathUtils.sin(lLife[i] * 1.6f + lPhase[i]) * 36f * dt + 6f * dt;
            lRot[i] += MathUtils.cos(lLife[i] * 1.6f + lPhase[i]) * 60f * dt;
            if (ly[i] < camY - hh) respawnLeaf(i, camX, camY, hw, hh);
        }
    }

    private void respawnLeaf(int i, float camX, float camY, float hw, float hh) {
        lx[i] = camX + MathUtils.random(-hw, hw); ly[i] = camY + hh + MathUtils.random(0, 60f);
        lPhase[i] = MathUtils.random(MathUtils.PI2); lLife[i] = 0f; lRot[i] = MathUtils.random(360f);
    }

    private static void wrap(float[] x, float[] y, int i, float cx, float cy, float hw, float hh) {
        if (x[i] < cx - hw) x[i] += hw * 2f; else if (x[i] > cx + hw) x[i] -= hw * 2f;
        if (y[i] < cy - hh) y[i] += hh * 2f; else if (y[i] > cy + hh) y[i] -= hh * 2f;
    }

    /** Fallende Blätter (normales Blending). */
    public void drawLeaves(SpriteBatch b) {
        int n = Math.max(0, Math.round(LEAVES * quality));
        for (int i = 0; i < n; i++) {
            float a = 0.9f;
            b.setColor(1f, 1f, 1f, a);
            b.draw(leaf, lx[i] - 20f, ly[i] - 20f, 20f, 20f, 40f, 40f, 1f, 0.8f + 0.2f * MathUtils.sin(lLife[i] * 3f), lRot[i]);
        }
        b.setColor(1f, 1f, 1f, 1f);
    }

    /** Leuchtende Partikel (additives Blending, vom Aufrufer gesetzt). */
    public void drawGlows(SpriteBatch b) {
        int nf = Math.round(FIREFLIES * quality), nm = Math.round(MOTES * quality);
        for (int i = 0; i < nf; i++) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(time * (1.6f + fSpeed[i]) + fPhase[i] * 3f);
            pulse *= pulse;
            float s = fSize[i] * (0.8f + pulse * 0.5f);
            b.setColor(0.85f, 1f, 0.45f, 0.15f + pulse * 0.55f);
            b.draw(glow, fx[i] - s, fy[i] - s, s * 2f, s * 2f);
            b.setColor(1f, 1f, 0.85f, 0.35f + pulse * 0.6f);
            b.draw(glow, fx[i] - 3f, fy[i] - 3f, 6f, 6f);
        }
        for (int i = 0; i < nm; i++) {
            float tw = 0.5f + 0.5f * MathUtils.sin(time * 1.1f + mPhase[i] * 2f);
            b.setColor(1f, 0.95f, 0.7f, 0.10f + 0.28f * tw);
            b.draw(glow, mx[i] - mSize[i], my[i] - mSize[i], mSize[i] * 2f, mSize[i] * 2f);
        }
        b.setColor(1f, 1f, 1f, 1f);
    }
}
