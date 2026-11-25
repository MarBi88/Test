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
    private float time;

    public GameHUD(ArtSheet art) {
        this.art = art;
        circle = art.get("ui_circle"); ring = art.get("ui_ring"); knob = art.get("ui_knob");
        jump = art.get("icon_jump"); pause = art.get("icon_pause");
    }

    public void draw(SpriteBatch b, TouchInput t, float dt) {
        time += dt;
        boolean showTouch = GameConfig.touchDevice || t.used;
        if (showTouch) {
            // Joystick (oder Hinweis-Ring an der Standardposition)
            if (t.stick.active) {
                b.setColor(1f, 0.97f, 0.88f, 0.35f);
                b.draw(circle, t.stick.baseX - 100f, t.stick.baseY - 100f, 200f, 200f);
                b.setColor(1f, 0.97f, 0.88f, 0.75f);
                b.draw(ring, t.stick.baseX - 100f, t.stick.baseY - 100f, 200f, 200f);
                b.setColor(1f, 0.82f, 0.45f, 0.95f);
                b.draw(knob, t.stick.knobX - 48f, t.stick.knobY - 48f, 96f, 96f);
            } else {
                b.setColor(1f, 0.97f, 0.88f, 0.28f);
                b.draw(ring, 230f - 100f, 190f - 100f, 200f, 200f);
                b.setColor(1f, 0.82f, 0.45f, 0.35f);
                b.draw(knob, 230f - 48f, 190f - 48f, 96f, 96f);
            }
            // Sprungtaste
            float s = t.jumpHeld ? 0.92f : 1f;
            round(b, t.jumpX, t.jumpY, TouchInput.JUMP_R * s, jump, 0.96f, 0.55f, 0.2f);
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
