package game.lumi.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import game.lumi.LumiGame;
import game.lumi.config.GameConfig;

/** Browser-Einstieg (libGDX-GWT-Backend). */
public class HtmlLauncher extends GwtApplication {
    @Override public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
        cfg.padHorizontal = 0;
        cfg.padVertical = 0;
        cfg.antialiasing = true;
        cfg.alpha = false;
        cfg.preserveDrawingBuffer = false;
        return cfg;
    }

    @Override public ApplicationListener createApplicationListener() {
        GameConfig.touchDevice = isTouchDevice();
        return new LumiGame();
    }

    private static native boolean isTouchDevice() /*-{
        try {
            return ('ontouchstart' in $wnd) || ($wnd.navigator.maxTouchPoints > 0 && $wnd.matchMedia && $wnd.matchMedia('(pointer: coarse)').matches);
        } catch (e) { return false; }
    }-*/;
}
