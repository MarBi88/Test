package game.lumi.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import game.lumi.ui.VirtualJoystick;

/**
 * Touch (und Maus als Zeiger): schwebender Joystick in der linken Bildschirmhälfte,
 * große Sprung- und Interaktionstaste rechts, Pause oben rechts.
 * Alle Koordinaten sind UI-Viewport-Koordinaten (y nach oben).
 */
public final class TouchInput {
    public static final float JUMP_R = 88f, ACT_R = 78f, PAUSE_R = 46f;

    public final VirtualJoystick stick = new VirtualJoystick();
    public boolean jump, interact, pause;
    public boolean jumpHeld, interactHeld;
    /** true, sobald je ein Touch-Ereignis gesehen wurde → Bedienelemente einblenden. */
    public boolean used;

    private final int[] role = new int[PointerTracker.MAX]; // 0 keine, 1 Joystick, 2 Sprung, 3 Interaktion, 4 Pause
    private final Vector2 v = new Vector2();

    public float jumpX, jumpY, actX, actY, pauseX, pauseY;

    public void layout(float w, float h) {
        jumpX = w - 140f; jumpY = 150f;
        actX = w - 310f; actY = 120f;
        pauseX = w - 72f; pauseY = h - 72f;
    }

    public void update(Viewport ui, PointerTracker pt) {
        jump = interact = pause = false;
        layout(ui.getWorldWidth(), ui.getWorldHeight());
        jumpHeld = interactHeld = false;
        for (int i = 0; i < PointerTracker.MAX; i++) {
            if (pt.downEvt[i]) { // neuer Fingerkontakt
                used = true;
                v.set(pt.x[i], pt.y[i]);
                ui.unproject(v);
                role[i] = classify(v.x, v.y, ui);
                if (role[i] == 1) stick.begin(i, v.x, v.y);
                else if (role[i] == 2) jump = true;
                else if (role[i] == 3) interact = true;
                else if (role[i] == 4) pause = true;
            }
            if (pt.down[i]) {
                if (role[i] == 1 && stick.pointer == i) { v.set(pt.x[i], pt.y[i]); ui.unproject(v); stick.drag(v.x, v.y); }
                if (role[i] == 2) jumpHeld = true;
                if (role[i] == 3) interactHeld = true;
            }
            if (!pt.down[i] && role[i] != 0) { // losgelassen
                if (role[i] == 1 && stick.pointer == i) stick.end();
                role[i] = 0;
            }
        }
    }

    private int classify(float x, float y, Viewport ui) {
        if (dist(x, y, pauseX, pauseY) < PAUSE_R + 16f) return 4;
        if (dist(x, y, jumpX, jumpY) < JUMP_R + 14f) return 2;
        if (dist(x, y, actX, actY) < ACT_R + 14f) return 3;
        if (x < ui.getWorldWidth() * 0.5f && !stick.active) return 1;
        return 0;
    }

    private static float dist(float x, float y, float cx, float cy) {
        float dx = x - cx, dy = y - cy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
