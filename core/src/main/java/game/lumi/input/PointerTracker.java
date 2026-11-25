package game.lumi.input;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;

/**
 * Sammelt Zeiger-Ereignisse (Touch und Maus) ereignisbasiert. Dadurch geht auch ein sehr kurzer
 * Tipper nicht verloren, der zwischen zwei Frames beginnt und endet (reines Polling würde ihn verpassen).
 * Koordinaten sind Bildschirm-Pixel (y nach unten, wie Gdx.input).
 */
public final class PointerTracker extends InputAdapter {
    public static final int MAX = 5;
    public final boolean[] down = new boolean[MAX], downEvt = new boolean[MAX], upEvt = new boolean[MAX];
    public final float[] x = new float[MAX], y = new float[MAX];

    @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
        if (pointer >= MAX || (pointer == 0 && button != Buttons.LEFT)) return false;
        down[pointer] = true; downEvt[pointer] = true; x[pointer] = sx; y[pointer] = sy;
        return true;
    }

    @Override public boolean touchDragged(int sx, int sy, int pointer) {
        if (pointer >= MAX) return false;
        x[pointer] = sx; y[pointer] = sy;
        return true;
    }

    @Override public boolean touchUp(int sx, int sy, int pointer, int button) {
        if (pointer >= MAX || !down[pointer]) return false;
        down[pointer] = false; upEvt[pointer] = true; x[pointer] = sx; y[pointer] = sy;
        return true;
    }

    /** Am Ende jedes Frames aufrufen. */
    public void endFrame() {
        for (int i = 0; i < MAX; i++) { downEvt[i] = false; upEvt[i] = false; }
    }

    /** Alles vergessen (z. B. bei Bildschirmwechsel). */
    public void reset() {
        for (int i = 0; i < MAX; i++) { down[i] = false; downEvt[i] = false; upEvt[i] = false; }
    }
}
