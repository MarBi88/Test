package game.lumi.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import game.lumi.art.ArtSheet;
import game.lumi.input.PointerTracker;

/** Verwaltet eine Gruppe von Knöpfen: Zeiger-Eingabe (Maus/Touch), Auslösen beim Loslassen. */
public final class UiLayer {
    public final Array<UiButton> buttons = new Array<>();
    private final PointerTracker pt;
    /** Wird bei jedem ausgelösten Knopf aufgerufen (Klick-Ton). */
    public static Runnable onFire;
    private UiButton pressed;
    private final Vector2 v = new Vector2();

    public UiLayer(PointerTracker pt) { this.pt = pt; }

    public UiButton add(UiButton b) { buttons.add(b); return b; }

    public void update(float dt, Viewport ui) {
        if (pt.downEvt[0]) {
            v.set(pt.x[0], pt.y[0]);
            ui.unproject(v);
            pressed = null;
            for (UiButton b : buttons) if (b.hit(v.x, v.y)) { pressed = b; break; }
        }
        boolean holding = pt.down[0];
        if (pt.upEvt[0] && pressed != null) {
            v.set(pt.x[0], pt.y[0]);
            ui.unproject(v);
            UiButton fire = pressed;
            pressed = null;
            if (fire.hit(v.x, v.y) && fire.action != null) { if (onFire != null) onFire.run(); fire.action.run(); }
        }
        for (UiButton b : buttons) { b.down = (b == pressed) && holding; b.update(dt); }
    }

    public void draw(SpriteBatch b, ArtSheet art, float time) {
        for (UiButton btn : buttons) btn.draw(b, art, time);
    }

    /** Laufende Berührung verwerfen (z. B. beim Menüwechsel). */
    public void cancel() { pressed = null; }
}
