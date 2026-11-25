package game.lumi;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import game.lumi.art.ArtFactory;
import game.lumi.art.ArtSheet;
import game.lumi.audio.AudioManager;
import game.lumi.input.PointerTracker;
import game.lumi.save.SaveManager;
import game.lumi.screens.GameScreen;
import game.lumi.screens.MenuScreen;
import game.lumi.world.GameWorld;
import game.lumi.world.WorldCamera;
import game.lumi.world.WorldRenderer;

/** Einstiegspunkt. Hält die gemeinsam genutzten, teuren Objekte und wechselt die Bildschirme. */
public class LumiGame extends Game {
    public SpriteBatch batch;
    public ArtSheet art;
    public SaveManager save;
    public AudioManager audio;
    public GameWorld world;
    public WorldRenderer renderer;
    public WorldCamera cam;
    public final PointerTracker pointers = new PointerTracker();

    private MenuScreen menu;
    private GameScreen gameScreen;

    @Override public void create() {
        Gdx.app.setLogLevel(com.badlogic.gdx.Application.LOG_INFO);
        long t0 = System.currentTimeMillis();
        Gdx.input.setInputProcessor(pointers);
        batch = new SpriteBatch(2000);
        save = new SaveManager();
        audio = new AudioManager();
        audio.setEnabled(save.soundOn());
        art = ArtFactory.build();
        Gdx.app.log("Lumi", "Grafik erzeugt in " + (System.currentTimeMillis() - t0) + " ms");
        world = new GameWorld(art);
        renderer = new WorldRenderer(art);
        cam = new WorldCamera();
        cam.setBounds(world.map.boundsMinX, world.map.boundsMaxX, world.map.boundsMinY, world.map.boundsMaxY);
        menu = new MenuScreen(this);
        gameScreen = new GameScreen(this);
        showMenu();
        Gdx.app.log("Lumi", "Start abgeschlossen in " + (System.currentTimeMillis() - t0) + " ms");
    }

    public void showMenu() { pointers.reset(); setScreen(menu); }

    public void startGame(boolean continueSaved) {
        if (continueSaved && save.hasGame()) {
            world.resetPlayer(save.getX(world.map.spawnX), save.getY(world.map.spawnY));
            world.loadCollected(save.getCrystals());
            world.loadFlags(save.getFlags());
        } else {
            save.newGame();
            audio.setEnabled(true);
            world.resetPlayer(world.map.spawnX, world.map.spawnY);
            world.loadCollected("");
            world.loadFlags("");
        }
        cam.snapTo(world.player.screenX(), world.player.screenY());
        pointers.reset();
        setScreen(gameScreen);
    }

    public void toggleSound() {
        audio.setEnabled(!audio.isEnabled());
        save.setSound(audio.isEnabled());
    }

    @Override public void render() {
        super.render();
        pointers.endFrame();
    }

    @Override public void pause() { if (save != null) save.flush(); super.pause(); }

    @Override public void dispose() {
        if (save != null) save.flush();
        if (batch != null) batch.dispose();
        if (art != null) art.dispose();
    }
}
