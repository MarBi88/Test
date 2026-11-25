package game.lumi.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import game.lumi.art.ArtSheet;
import game.lumi.input.PointerTracker;

/**
 * Einstellungen nur mit Symbolen: Musik an/aus, Effekte an/aus, Fortschritt zurücksetzen
 * (Knopf 2 Sekunden gedrückt halten, damit es nie aus Versehen passiert) und Zurück.
 */
public final class SettingsMenu {
    public interface Host {
        boolean music(); boolean fx();
        void toggleMusic(); void toggleFx();
        void resetProgress();
        void close();
    }

    private static final float HOLD_SECONDS = 2f;
    public final UiLayer layer;
    private final UiButton music, fx, reset, back;
    private final ArtSheet art;
    private final Host host;
    private float hold, time;

    public SettingsMenu(ArtSheet art, PointerTracker pt, Host host) {
        this.art = art; this.host = host;
        layer = new UiLayer(pt);
        music = layer.add(new UiButton("icon_music", 72f, host::toggleMusic));
        fx = layer.add(new UiButton("icon_fx", 72f, host::toggleFx));
        reset = layer.add(new UiButton("icon_reset", 72f, null));
        reset.bg.set(0.98f, 0.88f, 0.82f, 1f); reset.fg.set(0.85f, 0.3f, 0.25f, 1f);
        back = layer.add(new UiButton("icon_back", 56f, host::close));
    }

    public void open() { hold = 0f; layer.cancel(); refresh(); }

    private void refresh() {
        music.icon = host.music() ? "icon_music" : "icon_music_off";
        fx.icon = host.fx() ? "icon_fx" : "icon_fx_off";
        music.bg.set(host.music() ? 1f : 0.82f, host.music() ? 0.95f : 0.82f, host.music() ? 0.82f : 0.8f, 1f);
        fx.bg.set(host.fx() ? 1f : 0.82f, host.fx() ? 0.95f : 0.82f, host.fx() ? 0.82f : 0.8f, 1f);
    }

    public void update(float dt, Viewport ui) {
        time += dt;
        float w = ui.getWorldWidth(), h = ui.getWorldHeight();
        music.x = w * 0.5f - 190f; fx.x = w * 0.5f; reset.x = w * 0.5f + 190f;
        music.y = fx.y = reset.y = h * 0.54f;
        back.x = w * 0.5f; back.y = h * 0.54f - 170f;
        layer.update(dt, ui);
        refresh();
        // Zurücksetzen: nur durch langes Halten
        if (reset.down) {
            hold += dt;
            if (hold >= HOLD_SECONDS) { hold = 0f; layer.cancel(); host.resetProgress(); host.close(); }
        } else hold = Math.max(0f, hold - dt * 3f);
    }

    public void draw(SpriteBatch b, Viewport ui) {
        float w = ui.getWorldWidth(), h = ui.getWorldHeight();
        b.setColor(0.04f, 0.1f, 0.08f, 0.62f);
        b.draw(art.get("white"), 0, 0, w, h);
        b.setColor(Color.WHITE);
        // Fortschrittsring beim Halten
        if (hold > 0f) {
            float k = hold / HOLD_SECONDS, s = 150f + 40f * k;
            b.setColor(0.9f, 0.3f, 0.25f, 0.25f + 0.5f * k);
            b.draw(art.get("ui_circle"), reset.x - s / 2f, reset.y - s / 2f, s, s);
        }
        b.setColor(Color.WHITE);
        layer.draw(b, art, time);
    }
}
