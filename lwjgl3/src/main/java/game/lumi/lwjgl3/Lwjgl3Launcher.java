package game.lumi.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import game.lumi.LumiGame;

/** Desktop-Testplattform (LWJGL3). */
public final class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Lumi – Das Geheimnis des Zauberwaldes");
        cfg.setWindowedMode(1280, 720);
        cfg.useVsync(true);
        cfg.setForegroundFPS(60);
        cfg.setResizable(true);
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        new Lwjgl3Application(new LumiGame(), cfg);
    }
}
