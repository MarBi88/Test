package game.lumi.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import game.lumi.art.ArtSheet;
import game.lumi.input.PointerTracker;

/** Dezentes Pausenmenü: Weiter, Zum Start, Ton. Nur Symbole. */
public final class PauseMenu {
    public final UiLayer layer;
    private final UiButton resume, home, sound, gear;
    private final ArtSheet art;
    private float time;

    public PauseMenu(ArtSheet art, PointerTracker pt, Runnable onResume, Runnable onHome, Runnable onSound, Runnable onSettings, boolean soundOn) {
        this.art = art;
        layer = new UiLayer(pt);
        resume = layer.add(new UiButton("icon_play", 84f, onResume));
        resume.bg.set(0.99f, 0.74f, 0.25f, 1f); resume.fg.set(1f, 1f, 1f, 1f);
        home = layer.add(new UiButton("icon_home", 60f, onHome));
        gear = layer.add(new UiButton("icon_gear", 50f, onSettings));
        sound = layer.add(new UiButton(soundOn ? "icon_sound_on" : "icon_sound_off", 60f, onSound));
    }

    public void setSound(boolean on) { sound.icon = on ? "icon_sound_on" : "icon_sound_off"; }

    public void update(float dt, Viewport ui) {
        time += dt;
        float w = ui.getWorldWidth(), h = ui.getWorldHeight();
        resume.x = w * 0.5f; resume.y = h * 0.5f;
        home.x = w * 0.5f - 200f; home.y = h * 0.5f - 10f;
        sound.x = w * 0.5f + 200f; sound.y = h * 0.5f - 10f;
        gear.x = w * 0.5f; gear.y = h * 0.5f - 170f;
        layer.update(dt, ui);
    }

    public void draw(SpriteBatch b, Viewport ui) {
        float w = ui.getWorldWidth(), h = ui.getWorldHeight();
        b.setColor(0.04f, 0.1f, 0.08f, 0.55f);
        b.draw(art.get("white"), 0, 0, w, h);
        b.setColor(Color.WHITE);
        layer.draw(b, art, time);
    }
}
