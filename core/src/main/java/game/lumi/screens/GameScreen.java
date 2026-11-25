package game.lumi.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import game.lumi.LumiGame;
import game.lumi.config.GameConfig;
import game.lumi.entities.Player;
import game.lumi.input.InputManager;
import game.lumi.ui.GameHUD;
import game.lumi.ui.PauseMenu;

/** Der eigentliche Spielbildschirm: Welt, HUD, Pausenmenü. */
public final class GameScreen extends ScreenAdapter {
    private final LumiGame game;
    private final ExtendViewport ui = new ExtendViewport(GameConfig.VIEW_W, GameConfig.VIEW_H);
    private final InputManager input;
    private final GameHUD hud;
    private final PauseMenu pauseMenu;
    private boolean paused;
    /** F9: zeigt den prozeduralen Atlas (Entwickler-Hilfe zur Grafikkontrolle). */
    private boolean debugAtlas;

    public GameScreen(LumiGame game) {
        this.game = game;
        input = new InputManager(game.pointers);
        hud = new GameHUD(game.art);
        pauseMenu = new PauseMenu(game.art, game.pointers, () -> setPaused(false), () -> { savePosition(); game.save.flush(); game.showMenu(); },
                () -> { game.toggleSound(); pauseMenu().setSound(game.audio.isEnabled()); }, game.audio.isEnabled());
    }

    private PauseMenu pauseMenu() { return pauseMenu; }

    @Override public void show() {
        paused = false;
        input.clear();
        pauseMenu.setSound(game.audio.isEnabled());
    }

    private void setPaused(boolean p) {
        paused = p;
        input.clear();
        pauseMenu.layer.cancel();
        if (p) { savePosition(); game.save.flush(); }
    }

    private void savePosition() { game.save.setPosition(game.world.player.x, game.world.player.y); }

    @Override public void render(float dt) {
        dt = Math.min(dt, 1f / 20f);
        input.update(ui);
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F9)) debugAtlas = !debugAtlas;
        for (int i = 0; i < 4; i++) if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F1 + i)) game.renderer.hide[i] = !game.renderer.hide[i];
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F8)) game.world.particles.quality = game.world.particles.quality > 0f ? 0f : 1f;
        if (input.pause) setPaused(!paused);

        if (!paused) {
            game.world.update(dt, input);
            Player p = game.world.player;
            game.cam.follow(dt, p.screenX(), p.screenY() + 20f, p.vx * 64f - p.vy * 64f, -(p.vx + p.vy) * 32f);
            game.world.particles.update(dt, game.cam.camera.position.x, game.cam.camera.position.y,
                    game.cam.camera.viewportWidth, game.cam.camera.viewportHeight);
            game.save.setPosition(p.x, p.y);
            if (game.world.progressChanged) { game.world.progressChanged = false; game.save.setFlags(game.world.flagsBits()); }
            if (game.world.collectionChanged) { game.world.collectionChanged = false; game.save.setCrystals(game.world.collectedBits()); }
            game.save.tick(dt);
        } else {
            pauseMenu.update(dt, ui);
        }

        Gdx.gl.glClearColor(0.06f, 0.16f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.cam.viewport.apply();
        game.renderer.render(game.batch, game.world, game.cam, paused ? 0f : dt);

        ui.apply();
        game.batch.setProjectionMatrix(ui.getCamera().combined);
        game.batch.begin();
        game.renderer.drawVignette(game.batch, ui.getWorldWidth(), ui.getWorldHeight());
        if (paused) pauseMenu.draw(game.batch, ui);
        else hud.draw(game.batch, input.touch, dt, game.world.collectedBits(), ui.getWorldHeight(), game.world.interactTarget != null);
        if (debugAtlas) {
            game.batch.setColor(0.2f, 0.2f, 0.25f, 1f);
            game.batch.draw(game.art.get("white"), 0, 0, ui.getWorldWidth(), ui.getWorldHeight());
            game.batch.setColor(1f, 1f, 1f, 1f);
            float sz = ui.getWorldHeight();
            game.batch.draw(game.art.texture(), (ui.getWorldWidth() - sz) / 2f, 0, sz, sz);
        }
        game.batch.end();
    }

    @Override public void resize(int w, int h) { game.cam.resize(w, h); ui.update(w, h, true); }

    @Override public void pause() { savePosition(); game.save.flush(); if (!paused) setPaused(true); }
}
