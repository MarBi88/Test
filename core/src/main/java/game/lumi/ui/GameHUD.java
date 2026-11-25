package game.lumi.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import game.lumi.art.ArtSheet;
import game.lumi.config.GameConfig;
import game.lumi.input.TouchInput;

/** Minimalistische Spiel-Oberfläche: Pause-Knopf, bei Touch Joystick + große Sprungtaste. */
public final class GameHUD {
    private final ArtSheet art;
    private final TextureRegion circle, ring, knob, jump, pause;
    private float time, interactPop;

    public GameHUD(ArtSheet art) {
        this.art = art;
        circle = art.get("ui_circle"); ring = art.get("ui_ring"); knob = art.get("ui_knob");
        jump = art.get("icon_jump"); pause = art.get("icon_pause");
    }

    /** Sammelanzeige links oben: Kristall-Symbol + eine Perle pro Kristall (gefüllt = gefunden). */
    private void drawCollection(SpriteBatch b, String bits, float h) {
        int n = bits.length();
        float w = 110f + n * 21f, x = 28f, y = h - 28f - 64f;
        b.setColor(0.1f, 0.15f, 0.2f, 0.22f);
        b.draw(art.get("ui_pill"), x + 2f, y - 5f, w, 64f);
        b.setColor(1f, 0.95f, 0.82f, 0.88f);
        b.draw(art.get("ui_pill"), x, y, w, 64f);
        b.setColor(1f, 1f, 1f, 1f);
        b.draw(art.get("icon_crystal"), x + 14f, y + 8f, 48f, 48f);
        for (int i = 0; i < n; i++) {
            boolean got = bits.charAt(i) == '1';
            if (got) b.setColor(0.2f, 0.82f, 0.74f, 1f); else b.setColor(0.75f, 0.7f, 0.6f, 0.55f);
            b.draw(circle, x + 78f + i * 21f, y + 22f, 18f, 18f);
        }
        b.setColor(Color.WHITE);
    }

    private static final String[] CHIP_ICON = { "crit_ladybug", "crit_frog0", "crit_spider0" };

    /** Zähler gefressener Beute: Symbol + Zahl (Ziffern aus dem Atlas), keine Schrift nötig. */
    private void drawEaten(SpriteBatch b, int[] eaten, float uiH) {
        float y = uiH - 28f - 64f - 70f, x = 28f;
        for (int k = 0; k < 3; k++) {
            int n = eaten[k];
            int digits = n >= 100 ? 3 : n >= 10 ? 2 : 1;
            float w = 78f + digits * 26f;
            b.setColor(0.1f, 0.15f, 0.2f, 0.2f);
            b.draw(art.get("ui_pill"), x + 2f, y - 4f, w, 56f);
            b.setColor(1f, 0.95f, 0.82f, 0.88f);
            b.draw(art.get("ui_pill"), x, y, w, 56f);
            TextureRegion ic = art.get(CHIP_ICON[k]);
            float is = 40f, ih = is * ic.getRegionHeight() / ic.getRegionWidth();
            b.setColor(1f, 1f, 1f, 1f);
            b.draw(ic, x + 14f, y + 28f - ih / 2f, is, ih);
            b.setColor(0.36f, 0.3f, 0.22f, 1f);
            String str = Integer.toString(n);
            for (int i = 0; i < str.length(); i++) {
                TextureRegion dg = art.get("digit" + (str.charAt(i) - '0'));
                b.draw(dg, x + 62f + i * 26f, y + 12f, 24f, 38.4f);
            }
            x += w + 12f;
        }
        b.setColor(Color.WHITE);
    }

    public void draw(SpriteBatch b, TouchInput t, float dt, String bits, float uiH, boolean canInteract, int[] eaten) {
        time += dt;
        interactPop += ((canInteract ? 1f : 0f) - interactPop) * Math.min(1f, 10f * dt);
        drawCollection(b, bits, uiH);
        drawEaten(b, eaten, uiH);
        boolean showTouch = GameConfig.touchDevice || t.used;
        if (showTouch) {
            // Wischen: nur ein dezentes Leuchten unter dem Finger, kein Joystick
            if (t.stick.active) {
                float pulse = 1f + 0.06f * (float) Math.sin(time * 8f);
                b.setColor(1f, 0.97f, 0.8f, 0.22f);
                b.draw(circle, t.stick.knobX - 46f * pulse, t.stick.knobY - 46f * pulse, 92f * pulse, 92f * pulse);
                b.setColor(1f, 0.97f, 0.8f, 0.45f);
                b.draw(ring, t.stick.knobX - 30f, t.stick.knobY - 30f, 60f, 60f);
            }
            // Sprungtaste
            float s = t.jumpHeld ? 0.92f : 1f;
            round(b, t.jumpX, t.jumpY, TouchInput.JUMP_R * s, jump, 0.96f, 0.55f, 0.2f);
            if (interactPop > 0.05f) round(b, t.actX, t.actY, TouchInput.ACT_R * interactPop * (t.interactHeld ? 0.92f : 1f), art.get("icon_paw"), 0.96f, 0.45f, 0.1f);
        }
        round(b, t.pauseX, t.pauseY, TouchInput.PAUSE_R * 0.92f, pause, 0.35f, 0.5f, 0.5f);
        b.setColor(Color.WHITE);
    }

    private void round(SpriteBatch b, float x, float y, float r, TextureRegion icon, float ir, float ig, float ib) {
        b.setColor(0.1f, 0.15f, 0.2f, 0.25f);
        b.draw(circle, x - r + 2f, y - r - 6f, r * 2f, r * 2f);
        b.setColor(1f, 0.95f, 0.82f, 0.88f);
        b.draw(circle, x - r, y - r, r * 2f, r * 2f);
        b.setColor(1f, 1f, 1f, 0.35f);
        b.draw(circle, x - r * 0.84f, y + r * 0.02f, r * 1.68f, r * 0.88f);
        b.setColor(ir, ig, ib, 1f);
        float is = r * 1.2f;
        b.draw(icon, x - is / 2f, y - is / 2f, is, is);
    }
}
