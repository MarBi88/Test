package game.lumi.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/** Tastatur: WASD/Pfeile, Shift = rennen, Leertaste = springen, E = interagieren, Esc/P = Pause. */
public final class DesktopInput {
    public float moveX, moveY;
    public boolean run, jump, interact, pause;

    public void update() {
        float x = 0, y = 0;
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) x -= 1;
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) x += 1;
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) y += 1;
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) y -= 1;
        float len = (float) Math.sqrt(x * x + y * y);
        if (len > 1f) { x /= len; y /= len; }
        moveX = x; moveY = y;
        run = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
        jump = Gdx.input.isKeyJustPressed(Keys.SPACE);
        interact = Gdx.input.isKeyJustPressed(Keys.E);
        pause = Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.P);
    }
}
