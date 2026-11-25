package game.lumi.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import game.lumi.art.ArtSheet;

/** Großer, runder (oder pillenförmiger) Knopf mit Symbol. Weiche Druck-Animation, keine Texte. */
public final class UiButton {
    public float x, y;          // Mittelpunkt
    public float r;             // Radius (Kreis) bzw. halbe Höhe (Pille)
    public float halfW;         // nur Pille: halbe Breite
    public boolean pill;
    public String icon;
    public final Color bg = new Color(1f, 0.95f, 0.82f, 1f);
    public final Color fg = new Color(0.96f, 0.5f, 0.15f, 1f);
    public Runnable action;
    public boolean visible = true;
    float press;                 // 0..1 Druckanimation
    boolean down;

    public UiButton(String icon, float r, Runnable action) { this.icon = icon; this.r = r; this.action = action; }

    public UiButton pill(float halfW) { this.pill = true; this.halfW = halfW; return this; }

    public boolean hit(float px, float py) {
        if (!visible) return false;
        float pad = 10f;
        if (!pill) { float dx = px - x, dy = py - y; return dx * dx + dy * dy <= (r + pad) * (r + pad); }
        return Math.abs(px - x) <= halfW + pad && Math.abs(py - y) <= r + pad;
    }

    public void update(float dt) {
        press += ((down ? 1f : 0f) - press) * Math.min(1f, 18f * dt);
    }

    public void draw(SpriteBatch b, ArtSheet art, float time) {
        if (!visible) return;
        float s = 1f - 0.08f * press + 0.015f * MathUtils.sin(time * 2.2f + x * 0.01f);
        TextureRegion shape = art.get(pill ? "ui_pill" : "ui_circle");
        float w = (pill ? halfW * 2f : r * 2f) * s, h = r * 2f * s;
        // weicher Schatten
        b.setColor(0.1f, 0.15f, 0.2f, 0.28f);
        b.draw(shape, x - w / 2f + 3f, y - h / 2f - 7f, w, h);
        // Körper
        b.setColor(bg);
        b.draw(shape, x - w / 2f, y - h / 2f, w, h);
        // Glanz
        b.setColor(1f, 1f, 1f, 0.35f);
        b.draw(shape, x - w * 0.42f, y + h * 0.02f, w * 0.84f, h * 0.44f);
        // Symbol
        TextureRegion ic = art.get(icon);
        float is = r * 1.25f * s;
        b.setColor(fg);
        b.draw(ic, x - is / 2f, y - is / 2f, is, is);
        b.setColor(Color.WHITE);
    }
}
