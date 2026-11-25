package game.lumi.systems;

import game.lumi.entities.Collectible;
import game.lumi.world.GameWorld;

/** Einsammeln durch Berühren: Funkeln, freudige Reaktion von Lumi, Neugier in der Nähe. */
public final class CollectionSystem {
    private static final float PICKUP_R = 0.75f, CURIOUS_R = 3.2f;

    /** @return true, wenn in diesem Frame etwas eingesammelt wurde. */
    public boolean update(GameWorld w, float dt) {
        boolean got = false;
        float near = 1e9f;
        for (Collectible c : w.crystals) {
            if (c.collected) { c.vanish = Math.min(1f, c.vanish + dt * 2.2f); continue; }
            float dx = c.x - w.player.x, dy = c.y - w.player.y;
            float d2 = dx * dx + dy * dy;
            near = Math.min(near, d2);
            if (d2 < PICKUP_R * PICKUP_R) {
                c.collected = true; got = true;
                w.particles.burst(c.screenX(), c.screenY() + 50f, 26);
                w.lumi.cheer();
            }
        }
        if (!got && near < CURIOUS_R * CURIOUS_R && w.player.state == game.lumi.entities.Player.State.IDLE) w.lumi.beCurious(0.4f);
        return got;
    }
}
