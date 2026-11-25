package game.lumi.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import game.lumi.art.ArtSheet;
import game.lumi.input.PointerTracker;

/** Startbildschirm: Titel + großer Spielen-Knopf + Ton-Schalter. Keine Textpassagen. */
public final class MainMenu {
    public final UiLayer layer;
    private final UiButton play, sound, gear;
    private final ArtSheet art;
    private final TextureRegion title, glow;
    private float time;

    public MainMenu(ArtSheet art, PointerTracker pt, Runnable onPlay, Runnable onSound, Runnable onSettings, boolean soundOn) {
        this.art = art;
        layer = new UiLayer(pt);
        title = art.get("title"); glow = art.get("glow");
        play = layer.add(new UiButton("icon_play", 74f, onPlay).pill(120f));
        play.bg.set(0.99f, 0.74f, 0.25f, 1f); play.fg.set(1f, 1f, 1f, 1f);
        gear = layer.add(new UiButton("icon_gear", 46f, onSettings));
        sound = layer.add(new UiButton(soundOn ? "icon_sound_on" : "icon_sound_off", 46f, onSound));
    }

    public void setSound(boolean on) { sound.icon = on ? "icon_sound_on" : "icon_sound_off"; }

    public void update(float dt, Viewport ui) {
        time += dt;
        float w = ui.getWorldWidth(), h = ui.getWorldHeight();
        play.x = w * 0.5f; play.y = h * 0.20f;
        sound.x = w - 80f; sound.y = 80f;
        gear.x = 80f; gear.y = 80f;
        layer.update(dt, ui);
    }

    public void draw(SpriteBatch b, Viewport ui) {
        float w = ui.getWorldWidth(), h = ui.getWorldHeight();
        // weiches Leuchten hinter dem Titel
        b.setColor(1f, 0.9f, 0.55f, 0.35f + 0.08f * MathUtils.sin(time * 1.4f));
        b.draw(glow, w * 0.5f - 480f, h * 0.70f - 190f, 960f, 380f);
        float tw = 640f, th = tw * title.getRegionHeight() / title.getRegionWidth();
        float bob = MathUtils.sin(time * 1.6f) * 6f;
        b.setColor(0.2f, 0.1f, 0.05f, 0.3f);
        b.draw(title, w * 0.5f - tw / 2f + 5f, h * 0.76f - th / 2f + bob - 8f, tw, th);
        b.setColor(Color.WHITE);
        b.draw(title, w * 0.5f - tw / 2f, h * 0.76f - th / 2f + bob, tw, th);
        layer.draw(b, art, time);
    }
}
