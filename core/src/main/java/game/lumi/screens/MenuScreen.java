package game.lumi.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import game.lumi.LumiGame;
import game.lumi.config.GameConfig;
import game.lumi.input.InputManager;
import game.lumi.ui.MainMenu;

/** Startbildschirm: Die lebendige Waldwelt läuft im Hintergrund (Lumi sitzt im Sonnenlicht). */
public final class MenuScreen extends ScreenAdapter {
    private final LumiGame game;
    private final ExtendViewport ui = new ExtendViewport(GameConfig.VIEW_W, GameConfig.VIEW_H);
    private final MainMenu menu;
    private final InputManager noInput = new InputManager();
    private float time;

    public MenuScreen(LumiGame game) {
        this.game = game;
        menu = new MainMenu(game.art, () -> game.startGame(game.save.hasGame()), () -> { game.toggleSound(); menu().setSound(game.audio.isEnabled()); }, game.audio.isEnabled());
    }

    private MainMenu menu() { return menu; }

    @Override public void show() {
        game.world.resetPlayer(game.world.map.spawnX, game.world.map.spawnY);
        game.cam.snapTo(game.world.player.screenX(), game.world.player.screenY());
        menu.setSound(game.audio.isEnabled());
        menu.layer.cancel();
    }

    @Override public void render(float dt) {
        dt = Math.min(dt, 1f / 20f);
        time += dt;
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) game.startGame(game.save.hasGame());
        menu.update(dt, ui);
        // Kamera schwebt langsam über der Lichtung
        game.world.update(dt, noInput);
        float px = game.world.player.screenX() + MathUtils.sin(time * 0.25f) * 140f;
        float py = game.world.player.screenY() + MathUtils.cos(time * 0.2f) * 40f - 40f;
        game.cam.follow(dt, px, py, 0f, 0f);
        game.world.particles.update(dt, game.cam.camera.position.x, game.cam.camera.position.y, game.cam.camera.viewportWidth, game.cam.camera.viewportHeight);

        Gdx.gl.glClearColor(0.06f, 0.16f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.cam.viewport.apply();
        game.renderer.render(game.batch, game.world, game.cam, dt);

        ui.apply();
        game.batch.setProjectionMatrix(ui.getCamera().combined);
        game.batch.begin();
        game.renderer.drawVignette(game.batch, ui.getWorldWidth(), ui.getWorldHeight());
        menu.draw(game.batch, ui);
        game.batch.end();
    }

    @Override public void resize(int w, int h) { game.cam.resize(w, h); ui.update(w, h, true); }
}
