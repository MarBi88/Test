package game.lumi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import game.lumi.art.ArtFactory;
import game.lumi.art.ArtSheet;
import game.lumi.input.InputManager;
import game.lumi.input.PointerTracker;
import game.lumi.world.GameWorld;
import org.mockito.Mockito;

/** Gemeinsame Test-Infrastruktur: headless-libGDX mit Mock-GL, damit die echte Welt gebaut werden kann. */
public final class TestWorlds {
    private TestWorlds() {}

    private static boolean ready;
    private static ArtSheet art;

    public static synchronized void init() {
        if (ready) return;
        new HeadlessApplication(new ApplicationAdapter() { }, new HeadlessApplicationConfiguration());
        GL20 gl = Mockito.mock(GL20.class);
        Gdx.gl = gl; Gdx.gl20 = gl;
        art = ArtFactory.build();
        ready = true;
    }

    public static GameWorld newWorld() { init(); return new GameWorld(art); }

    public static InputManager noInput() { return new InputManager(new PointerTracker()); }

    /** Lässt die Welt n Frames à dt laufen. */
    public static void run(GameWorld w, InputManager in, int frames, float dt) { for (int i = 0; i < frames; i++) w.update(dt, in); }
}
