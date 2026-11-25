package game.lumi.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import game.lumi.art.ArtSheet;
import game.lumi.art.LumiArt;
import game.lumi.entities.Player;

/**
 * Lumis Darstellung: Körperteile aus dem Atlas werden pro Frame per Pose-Parametern
 * transformiert. Alle Parameter werden exponentiell geglättet, dadurch gehen Zustände
 * (Idle → Walk → Run → Jump → Land → Sleep) ohne harte Sprünge ineinander über.
 */
public final class LumiCharacter {
    /** Gesamtgröße relativ zu den Atlas-Maßen. */
    private static final float SCALE = 1.6f;
    private static final float INV_K = 1f / LumiArt.K;

    private final TextureRegion body, bodyBack, head, headBack, muzzle, eye, eyeHappy, blush, ear, earBack, tail, foot, pendant, glow, shadow;

    // --- geglättete Pose
    private float bob, squashY = 1f, squashX = 1f;
    private float headY, headTilt, lookX, lookY;
    private float tailRot, tailSwing, earL, earR, legAmp, eyeOpen = 1f, lean;
    private float turn = 1f;       // -1 (links) … +1 (rechts), animiert für weiche Drehung
    private float backBlend;       // 0 Vorderansicht … 1 Rückansicht
    private float sleepBlend, happyBlend;

    // --- Zeitgeber
    private float time, phase;
    private float blinkTimer = 2.2f, blink;
    private float specialTimer = 7f, special;
    private float earTwitchTimer = 3f, earTwitch;
    private float happyTime;
    private float curiousTime;
    private float interactTime, glanceTime, glanceX;

    public LumiCharacter(ArtSheet art) {
        body = art.get("lumi_body"); bodyBack = art.get("lumi_bodyBack");
        head = art.get("lumi_head"); headBack = art.get("lumi_headBack");
        muzzle = art.get("lumi_muzzle"); eye = art.get("lumi_eye"); eyeHappy = art.get("lumi_eyeHappy");
        blush = art.get("lumi_blush");
        ear = art.get("lumi_ear"); earBack = art.get("lumi_earBack");
        tail = art.get("lumi_tail"); foot = art.get("lumi_foot"); pendant = art.get("lumi_pendant");
        glow = art.get("glow"); shadow = art.get("shadow");
    }

    /** Freudige Reaktion (Hüpfer + glückliche Augen), z. B. beim Einsammeln. */
    public void cheer() { happyTime = 1.3f; }
    /** Blick zur Seite (Hinweis): dir = -1 links, +1 rechts. */
    public void glance(float dir, float seconds) { glanceX = dir; glanceTime = seconds; curiousTime = Math.max(curiousTime, seconds); }

    /** Kurzes Untersuchen/Anstupsen: Kopf senkt sich, Schwanz wedelt. */
    public void interact() { interactTime = 0.9f; }

    /** Neugieriger Blick (Kopf neigen, Ohren aufrichten). */
    public void beCurious(float seconds) { curiousTime = Math.max(curiousTime, seconds); }

    private static float sm(float cur, float target, float rate, float dt) {
        return cur + (target - cur) * (1f - (float) Math.exp(-rate * dt));
    }

    public void update(float dt, Player p) {
        time += dt;
        happyTime = Math.max(0f, happyTime - dt);
        curiousTime = Math.max(0f, curiousTime - dt);
        interactTime = Math.max(0f, interactTime - dt);
        glanceTime = Math.max(0f, glanceTime - dt);

        boolean sleeping = p.state == Player.State.SLEEP;
        boolean walking = p.state == Player.State.WALK, running = p.state == Player.State.RUN;
        boolean air = !p.grounded;
        boolean landing = p.state == Player.State.LAND;
        boolean idle = p.state == Player.State.IDLE;

        // Schrittphase
        float cadence = running ? 15f : 10f;
        if (walking || running) phase += dt * cadence * (0.55f + p.speed01 * 0.7f);

        // Zufällige Idle-Extras (umschauen, Ohrzucken, Schwanzwedeln)
        if (idle) {
            specialTimer -= dt;
            if (specialTimer <= 0f) { special = 2.6f; specialTimer = MathUtils.random(6f, 11f); }
        } else special = 0f;
        special = Math.max(0f, special - dt);
        earTwitchTimer -= dt;
        if (earTwitchTimer <= 0f) { earTwitch = 0.35f; earTwitchTimer = MathUtils.random(2.5f, 6f); }
        earTwitch = Math.max(0f, earTwitch - dt);

        // Blinzeln
        blinkTimer -= dt;
        if (blinkTimer <= 0f) { blink = 0.16f; blinkTimer = MathUtils.random(2.2f, 5.2f); }
        blink = Math.max(0f, blink - dt);
        float blinkAmt = blink > 0f ? MathUtils.sin(blink / 0.16f * MathUtils.PI) : 0f;

        // --- Zielwerte je Zustand
        float tBob = 0f, tSquashY = 1f, tSquashX = 1f, tHeadY = 0f, tTilt = 0f, tLookX = 0f, tLookY = 0f;
        float tTail = 18f, tSwing = 7f, tEar = 6f, tLegs = 0f, tLean = 0f;
        float breathe = MathUtils.sin(time * 2.1f);

        if (idle || landing || sleeping) {
            tBob = breathe * 0.9f;
            tSquashY = 1f + breathe * 0.012f;
            tSwing = 8f;
        }
        if (special > 0f) { // umschauen: links – rechts – Blick nach oben
            float s = special / 2.6f;
            tLookX = MathUtils.sin((1f - s) * MathUtils.PI * 2.2f) * 0.9f;
            tTilt = MathUtils.sin((1f - s) * MathUtils.PI * 2.2f) * 7f;
            tSwing = 16f; tEar = 12f;
        }
        if (walking || running) {
            float amp = running ? 1f : 0.62f;
            tBob = Math.abs(MathUtils.sin(phase)) * (running ? 5.5f : 3.2f);
            tLegs = amp;
            tLean = running ? 9f : 3f;
            tTail = running ? 48f : 24f;
            tSwing = running ? 6f : 10f;
            tEar = running ? 22f : 9f;
            tSquashY = 1f + MathUtils.sin(phase * 2f) * 0.025f * amp;
            tTilt = MathUtils.sin(phase) * 2.2f * amp;
        }
        if (air) {
            boolean up = p.vz > 0f;
            tSquashY = up ? 1.10f : 0.96f; tSquashX = up ? 0.93f : 1.04f;
            tLegs = 0.2f; tEar = up ? -4f : 26f; tTail = up ? 34f : 8f; tSwing = 3f; tLean = 5f;
            tHeadY = up ? 2f : 0f;
        }
        if (landing) {
            float t = MathUtils.clamp(p.stateTime / 0.24f, 0f, 1f);
            float dip = (1f - t) * (1f - t);
            tSquashY = 1f - 0.20f * dip; tSquashX = 1f + 0.13f * dip; tBob = -3f * dip; tEar = 14f * dip;
        }
        if (curiousTime > 0f) {
            tTilt += 12f * (p.facingX); tEar = Math.min(tEar, -3f); tHeadY += 3f; tLookY = 0.4f;
        }
        if (glanceTime > 0f && idle) { tLookX = glanceX * 0.9f; tTilt = glanceX * 6f; }
        if (interactTime > 0f) {
            float u = 1f - interactTime / 0.9f;
            float nod = MathUtils.sin(u * MathUtils.PI);
            tHeadY -= 6f * nod; tTilt += 8f * p.facingX * nod; tSquashY = Math.min(tSquashY, 1f - 0.05f * nod);
            tSwing = 20f; tLookY = -0.5f;
        }
        float tSleep = sleeping ? 1f : 0f;
        float tHappy = happyTime > 0f ? 1f : 0f;
        if (happyTime > 0f) {
            float h = happyTime / 1.3f;
            tBob = Math.abs(MathUtils.sin((1f - h) * MathUtils.PI * 4f)) * 9f * h;
            tSwing = 24f; tTail = 30f; tEar = 18f;
        }
        if (sleeping) {
            tBob = -5f + breathe * 0.7f; tSquashY = 0.74f + breathe * 0.012f; tSquashX = 1.16f;
            tHeadY = -17f; tTilt = -14f * p.facingX; tEar = -16f; tSwing = 3f; tTail = 70f;
        }

        // --- Glättung
        float r = 14f;
        bob = sm(bob, tBob, r, dt);
        squashY = sm(squashY, tSquashY, 16f, dt); squashX = sm(squashX, tSquashX, 16f, dt);
        headY = sm(headY, tHeadY, 10f, dt); headTilt = sm(headTilt, tTilt, 9f, dt);
        lookX = sm(lookX, tLookX, 8f, dt); lookY = sm(lookY, tLookY, 8f, dt);
        tailRot = sm(tailRot, tTail, 7f, dt); tailSwing = sm(tailSwing, tSwing, 6f, dt);
        earL = sm(earL, tEar, 12f, dt); earR = sm(earR, tEar, 12f, dt);
        legAmp = sm(legAmp, tLegs, 14f, dt); lean = sm(lean, tLean, 8f, dt);
        sleepBlend = sm(sleepBlend, tSleep, 4f, dt);
        happyBlend = sm(happyBlend, tHappy, 14f, dt);
        eyeOpen = sm(eyeOpen, sleeping ? 0f : 1f, 8f, dt);
        // Drehung (weiche Spiegelung)
        turn = sm(turn, p.facingX, 14f, dt);
        backBlend = sm(backBlend, p.facingBack ? 1f : 0f, 13f, dt);
        if (Math.abs(turn - p.facingX) < 0.02f) turn = p.facingX;
        this.blinkAmt = blinkAmt;
    }

    private float blinkAmt;

    // -------------------------------------------------------------------- Zeichnen

    private float cr, cg, cb;

    /** Setzt die Umgebungsfarbe (Tageslichtstimmung). */
    public void setAmbient(float r, float g, float b) { cr = r; cg = g; cb = b; }

    /** Bodenschatten (vor den tiefensortierten Objekten zeichnen). */
    public void drawShadow(SpriteBatch b, float sx, float sy, float z) {
        float k = 1f - MathUtils.clamp(z / 160f, 0f, 0.6f);
        float w = 64f * SCALE * k * (sleepBlend > 0.5f ? 1.3f : 1f), h = 28f * SCALE * k;
        b.setColor(1f, 1f, 1f, 0.85f * k);
        b.draw(shadow, sx - w / 2f + 3f, sy - h / 2f - 1f, w, h);
        b.setColor(Color.WHITE);
    }

    private boolean flipNow;

    /** Hilfsroutine: Teil zeichnen. px/py in Lumi-Anzeige-Pixeln, relativ zum Fußpunkt; x wird mit turn gespiegelt. */
    private void part(SpriteBatch b, TextureRegion r, float ox, float oy, float px, float py, float rot, float sx, float sy, float scaleX, float scaleY,
                      float originX, float originY) {
        float w = r.getRegionWidth() * INV_K, h = r.getRegionHeight() * INV_K;
        float flipSign = turn < 0f ? -1f : 1f;
        float tx = px * turn;   // Positionen verengen sich beim Drehen
        float widthScale = scaleX * (0.62f + 0.38f * Math.abs(turn));
        float gx = ox + tx * SCALE, gy = oy + py * SCALE;
        b.draw(r, gx - originX * SCALE, gy - originY * SCALE, originX * SCALE, originY * SCALE, w * SCALE, h * SCALE,
                widthScale * flipSign, scaleY, rot * flipSign);
    }

    /** Lumi zeichnen. sx/sy = Fußpunkt auf dem Bildschirm, z = Sprunghöhe. */
    public void draw(SpriteBatch b, float sx, float sy, float z) {
        float oy = sy + z;
        float ox = sx;
        float legPhase = phase;
        float bodyY = 25f + bob;
        float flat = 1f - sleepBlend * 0.12f;
        float back = backBlend;
        boolean showBack = back > 0.5f;
        b.setColor(1f + (cr - 1f) * 0f, 1f, 1f, 1f);
        float leanX = lean * 0.5f * turn;

        // Schwanz (hinter dem Körper, seitlich nach außen/hinten geneigt)
        float sw = MathUtils.sin(time * 3.1f) * tailSwing + (happyBlend > 0.5f ? MathUtils.sin(time * 14f) * 10f : 0f);
        float tailSide = showBack ? 17f : -21f;
        part(b, tail, ox, oy, tailSide - leanX * 0.5f, 10f + bob * 0.6f, (tailRot * 1.25f + sw) * (showBack ? -0.9f : 1f) + sleepBlend * 25f, 0, 0, 0.95f, 0.85f, tail.getRegionWidth() * INV_K * 0.5f, 6f);

        // hintere Füße
        float fl = MathUtils.sin(legPhase) * 9f * legAmp, fr = -fl;
        float liftL = Math.max(0f, MathUtils.cos(legPhase)) * 6f * legAmp;
        float liftR = Math.max(0f, -MathUtils.cos(legPhase)) * 6f * legAmp;
        float airTuck = z > 1f ? 4f : 0f;
        part(b, foot, ox, oy, -15f + fr * 0.7f, 4f + liftR * 0.8f + airTuck, 0, 0, 0, 1f, 1f, 12f, 4f);
        part(b, foot, ox, oy, 15f + fl * 0.7f, 4f + liftL * 0.8f + airTuck, 0, 0, 0, 1f, 1f, 12f, 4f);

        // Körper
        TextureRegion bodyR = showBack ? bodyBack : body;
        part(b, bodyR, ox, oy, leanX, bodyY - 28f * (1f - squashY) * 0.5f, 0, 0, 0, squashX * (1f + sleepBlend * 0.1f), squashY * flat, 30f, 28f);

        // vordere Füße
        part(b, foot, ox, oy, -11f + fl, 3f + liftL + airTuck, 0, 0, 0, 1.05f, 1.05f, 12f, 4f);
        part(b, foot, ox, oy, 11f + fr, 3f + liftR + airTuck, 0, 0, 0, 1.05f, 1.05f, 12f, 4f);

        // Kopf-Basis
        float hx = leanX * 1.6f + lookX * 3f, hy = 56f + bob * 1.15f + headY - (1f - squashY) * 20f;
        float tilt = headTilt - lean * 0.5f * turn * 0.4f;

        // Ohren (hinter dem Kopf, Wurzel überdeckt)
        TextureRegion earR_ = showBack ? earBack : ear;
        float eTwL = earTwitch > 0f ? MathUtils.sin(earTwitch / 0.35f * MathUtils.PI * 3f) * 16f : 0f;
        float pivotY = 5f;
        part(b, earR_, ox, oy, hx - 21f + lookX * 1f, hy + 15f, 24f + earL + tilt * 0.4f, 0, 0, 1f, 1f, 16f, pivotY);
        part(b, earR_, ox, oy, hx + 21f + lookX * 1f, hy + 15f, -24f - earR + eTwL + tilt * 0.4f, 0, 0, 1f, 1f, 16f, pivotY);

        // Kopf
        TextureRegion headR = showBack ? headBack : head;
        part(b, headR, ox, oy, hx, hy, tilt * 0.6f, 0, 0, 1f, 1f - sleepBlend * 0.04f, head.getRegionWidth() * INV_K * 0.5f, head.getRegionHeight() * INV_K * 0.5f);

        if (!showBack) {
            float fx = hx + lookX * 5.5f, fy = hy + lookY * 2f;
            part(b, blush, ox, oy, fx - 24f, fy - 7f, 0, 0, 0, 1f, 1f, 12f, 8f);
            part(b, blush, ox, oy, fx + 24f, fy - 7f, 0, 0, 0, 1f, 1f, 12f, 8f);
            // Augen
            float eyeScaleY = MathUtils.lerp(1f, 0.08f, Math.max(blinkAmt, 1f - eyeOpen));
            float eyeDx = 14.5f + lookX * 0.8f;
            TextureRegion er = happyBlend > 0.5f ? eyeHappy : (sleepBlend > 0.6f ? eyeHappy : eye);
            float sy2 = er == eye ? eyeScaleY : (sleepBlend > 0.6f ? -1f : 1f);
            part(b, er, ox, oy, fx - eyeDx, fy + 2f + lookY * 1.5f, 0, 0, 0, 1f, sy2, 13f, er == eye ? 15f : 15f);
            part(b, er, ox, oy, fx + eyeDx, fy + 2f + lookY * 1.5f, 0, 0, 0, 1f, sy2, 13f, 15f);
            // Schnauze
            part(b, muzzle, ox, oy, fx + lookX * 2.5f, fy - 11f, 0, 0, 0, 1f, 1f, 20f, 15f);
            // Anhänger auf der Brust
            part(b, pendant, ox, oy, leanX * 0.4f, bodyY + 4f - sleepBlend * 6f, 0, 0, 0, 1f, 1f, 10f, 24f);
        }
        b.setColor(Color.WHITE);
    }

    /** Leuchten des Anhängers (additiv zeichnen). */
    public void drawGlow(SpriteBatch b, float sx, float sy, float z, boolean back) {
        if (back) return;
        float pulse = 0.55f + 0.25f * MathUtils.sin(time * 2.4f);
        float gy = sy + z + (25f + bob + 5f - sleepBlend * 6f) * SCALE;
        float size = 46f * SCALE;
        b.setColor(0.35f, 0.95f, 0.85f, pulse * 0.55f);
        b.draw(glow, sx + lean * 0.2f - size / 2f, gy - size / 2f, size, size);
        b.setColor(Color.WHITE);
    }

    public boolean isBackView() { return backBlend > 0.5f; }
}
