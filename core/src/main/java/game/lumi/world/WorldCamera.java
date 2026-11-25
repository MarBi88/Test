package game.lumi.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import game.lumi.config.GameConfig;

/** Orthografische Kamera, die Lumi weich folgt; isometrische Perspektive bleibt fest. */
public final class WorldCamera {
    public final OrthographicCamera camera = new OrthographicCamera();
    public final ExtendViewport viewport = new ExtendViewport(GameConfig.VIEW_W, GameConfig.VIEW_H, camera);
    private float minX, maxX, minY, maxY;
    private boolean hasBounds;
    /** Leichter Vorlauf in Blickrichtung. */
    private float leadX, leadY;

    public void setBounds(float minX, float maxX, float minY, float maxY) {
        this.minX = minX; this.maxX = maxX; this.minY = minY; this.maxY = maxY; hasBounds = true;
    }

    public void snapTo(float x, float y) {
        camera.position.set(x, y + 40f, 0f);
        clamp();
        camera.update();
    }

    public void follow(float dt, float tx, float ty, float velSX, float velSY) {
        leadX += (velSX * 0.35f - leadX) * Math.min(1f, 2.5f * dt);
        leadY += (velSY * 0.35f - leadY) * Math.min(1f, 2.5f * dt);
        float gx = tx + leadX, gy = ty + 40f + leadY;
        float k = 1f - (float) Math.exp(-5.0 * dt);
        camera.position.x += (gx - camera.position.x) * k;
        camera.position.y += (gy - camera.position.y) * k;
        clamp();
        camera.update();
    }

    private void clamp() {
        if (!hasBounds) return;
        float hw = camera.viewportWidth * 0.5f, hh = camera.viewportHeight * 0.5f;
        float cminX = minX + hw * 0.55f, cmaxX = maxX - hw * 0.55f, cminY = minY + hh * 0.5f, cmaxY = maxY - hh * 0.5f;
        camera.position.x = cminX < cmaxX ? MathUtils.clamp(camera.position.x, cminX, cmaxX) : (minX + maxX) * 0.5f;
        camera.position.y = cminY < cmaxY ? MathUtils.clamp(camera.position.y, cminY, cmaxY) : (minY + maxY) * 0.5f;
    }

    public float left() { return camera.position.x - camera.viewportWidth * 0.5f; }
    public float right() { return camera.position.x + camera.viewportWidth * 0.5f; }
    public float bottom() { return camera.position.y - camera.viewportHeight * 0.5f; }
    public float top() { return camera.position.y + camera.viewportHeight * 0.5f; }

    public void resize(int w, int h) { viewport.update(w, h, false); }
}
