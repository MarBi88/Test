package game.lumi.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import game.lumi.art.ArtSheet;

/** Verwaltet eine Gruppe von Knöpfen: Zeiger-Eingabe (Maus/Touch), Auslösen beim Loslassen. */
public final class UiLayer {
    public final Array<UiButton> buttons = new Array<>();
    private UiButton pressed;
    private boolean wasDown;
    private final Vector2 v = new Vector2();

    public UiButton add(UiButton b) { buttons.add(b); return b; }

    public void update(float dt, Viewport ui) {
        boolean down = Gdx.input.isTouched();
        if (down) {
            v.set(Gdx.input.getX(), Gdx.input.getY());
            ui.unproject(v);
            if (!wasDown) {
                pressed = null;
                for (UiButton b : buttons) if (b.hit(v.x, v.y)) { pressed = b; break; }
            }
        } else if (wasDown && pressed != null) {
            v.set(Gdx.input.getX(), Gdx.input.getY());
            ui.unproject(v);
            if (pressed.hit(v.x, v.y) && pressed.action != null) pressed.action.run();
            pressed = null;
        }
        wasDown = down;
        for (UiButton b : buttons) { b.down = (b == pressed) && down; b.update(dt); }
    }

    public void draw(SpriteBatch b, ArtSheet art, float time) {
        for (UiButton btn : buttons) btn.draw(b, art, time);
    }

    public void cancel() { pressed = null; wasDown = Gdx.input.isTouched(); }
}
