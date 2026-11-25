package game.lumi.input;

import com.badlogic.gdx.utils.viewport.Viewport;

/** Führt Tastatur und Touch zu einem einheitlichen Eingabezustand zusammen. */
public final class InputManager {
    public final DesktopInput desktop = new DesktopInput();
    public final TouchInput touch = new TouchInput();

    /** Bildschirmrichtung (x rechts, y oben), Betrag 0..1. */
    public float moveX, moveY;
    public boolean run, jump, interact, pause;

    public void update(Viewport ui) {
        desktop.update();
        touch.update(ui);
        float tx = touch.stick.outX, ty = touch.stick.outY;
        if (touch.stick.magnitude > 0f) {
            moveX = tx; moveY = ty;
            run = touch.stick.magnitude > 0.88f;
        } else {
            moveX = desktop.moveX; moveY = desktop.moveY;
            run = desktop.run;
        }
        jump = desktop.jump || touch.jump;
        interact = desktop.interact || touch.interact;
        pause = desktop.pause || touch.pause;
    }

    public void clear() { moveX = moveY = 0f; run = jump = interact = pause = false; }
}
