package game.lumi.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import game.lumi.entities.Critter;
import game.lumi.entities.Decor;
import game.lumi.entities.Player;
import game.lumi.world.GameWorld;
import game.lumi.world.WorldMap;

/**
 * Insekten, Frösche und Spinnen huschen, hüpfen und flattern über die Insel. Sie sind langsamer als Lumi,
 * weichen aber aus – Fangen ist ein Spiel, nie ein Frust. Gefressene Tiere tauchen nach einer Weile
 * an anderer Stelle wieder auf, damit immer Nachschub da ist.
 */
public final class CritterSystem {
    private static final float EAT_RADIUS = 0.75f, FLEE_RADIUS = 2.3f, HOP_CYCLE = 0.9f, HOP_ACTIVE = 0.42f;
    private final float[] out = new float[2];

    public static float speedOf(Critter c) {
        if (c.kind == Critter.FROG) return 1.6f;
        if (c.kind == Critter.SPIDER) return 1.2f;
        return c.variant == 0 ? 0.7f : c.variant == 1 ? 1.5f : 1.9f;
    }

    /** Sucht einen passenden Lebensraum: Frösche am Wasser, Spinnen bei Bäumen, Insekten überall. */
    public void place(GameWorld w, Critter c, RandomXS128 r, boolean avoidPlayer) {
        WorldMap m = w.map;
        for (int tries = 0; tries < 120; tries++) {
            float x, y;
            if (c.kind == Critter.FROG && m.streamCount() > 0 && r.nextFloat() < 0.8f) {
                int i = r.nextInt(m.streamCount());
                x = m.streamX(i) + (r.nextFloat() - 0.5f) * 3f; y = m.streamY(i) + (r.nextFloat() - 0.5f) * 3f;
            } else if (c.kind == Critter.FROG) {
                float a = r.nextFloat() * MathUtils.PI2, d = m.pondRx + 0.8f + r.nextFloat() * 2f;
                x = m.pondX + MathUtils.cos(a) * d; y = m.pondY + MathUtils.sin(a) * d * 0.7f;
            } else if (c.kind == Critter.SPIDER && w.map.decor.size > 0) {
                Decor d = w.map.decor.get(r.nextInt(w.map.decor.size));
                if (!d.occluder) continue;
                float a = r.nextFloat() * MathUtils.PI2;
                x = d.x + MathUtils.cos(a) * 1.3f; y = d.y + MathUtils.sin(a) * 1.3f;
            } else {
                x = WorldMap.CX + (r.nextFloat() - 0.5f) * 2f * 34f; y = WorldMap.CY + (r.nextFloat() - 0.5f) * 2f * 34f;
            }
            if (m.field(x, y) > 0.84f || m.inSecret(x, y, 2f) || !m.free(x, y, 0.16f)) continue;
            if (avoidPlayer && Math.hypot(x - w.player.x, y - w.player.y) < 9.0) continue;
            c.x = x; c.y = y; c.homeX = x; c.homeY = y; c.alive = true; c.timer = r.nextFloat() * 2f; c.moving = false; c.z = 0f;
            return;
        }
        c.x = c.homeX = WorldMap.CX + 3f; c.y = c.homeY = WorldMap.CY + 3f; c.alive = true;
    }

    private final RandomXS128 rng = new RandomXS128(5150);

    public void update(GameWorld w, float dt) {
        Player p = w.player;
        for (Critter c : w.critters) {
            if (!c.alive) {
                c.respawn -= dt;
                if (c.respawn <= 0f) place(w, c, rng, true);
                continue;
            }
            c.anim += dt;
            float dx = p.x - c.x, dy = p.y - c.y;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < EAT_RADIUS && p.state != Player.State.SLEEP) { eat(w, c); continue; }

            boolean flee = d < FLEE_RADIUS;
            if (flee) {
                float l = Math.max(d, 0.001f);
                c.dirX = -dx / l + (rng.nextFloat() - 0.5f) * 0.6f; c.dirY = -dy / l + (rng.nextFloat() - 0.5f) * 0.6f;
                c.moving = true;
            } else {
                c.timer -= dt;
                if (c.timer <= 0f) {
                    if (c.moving && rng.nextFloat() < 0.5f) { c.moving = false; c.timer = 0.8f + rng.nextFloat() * 2.2f; }
                    else {
                        float hx = c.homeX - c.x, hy = c.homeY - c.y, h = (float) Math.sqrt(hx * hx + hy * hy);
                        float a = rng.nextFloat() * MathUtils.PI2;
                        c.dirX = MathUtils.cos(a); c.dirY = MathUtils.sin(a);
                        if (h > 3.2f) { c.dirX = hx / h; c.dirY = hy / h; }
                        c.moving = true; c.timer = 0.8f + rng.nextFloat() * 1.8f;
                    }
                }
            }
            if (c.flies()) { // Schmetterling schlängelt sich flatternd, bleibt in der Luft
                float wob = MathUtils.sin(c.anim * 2.3f) * 0.8f;
                float cx = c.dirX - wob * c.dirY * 0.5f, cy = c.dirY + wob * c.dirX * 0.5f;
                c.moving = true;
                step(w, c, cx, cy, speedOf(c) * (flee ? 1.4f : 1f), dt);
                c.z = 34f + MathUtils.sin(c.anim * 3.1f) * 14f;
            } else if (c.hops()) {
                c.hopT += dt; if (c.hopT > HOP_CYCLE) c.hopT -= HOP_CYCLE;
                boolean air = c.moving && c.hopT < HOP_ACTIVE;
                c.z = air ? MathUtils.sin(c.hopT / HOP_ACTIVE * MathUtils.PI) * (c.kind == Critter.FROG ? 16f : 22f) : 0f;
                if (air) step(w, c, c.dirX, c.dirY, speedOf(c) * (flee ? 1.4f : 1f) * 1.9f, dt);
            } else if (c.moving) {
                step(w, c, c.dirX, c.dirY, speedOf(c) * (flee ? 1.4f : 1f), dt);
            }
        }
    }

    private void step(GameWorld w, Critter c, float dx, float dy, float speed, float dt) {
        float l = (float) Math.sqrt(dx * dx + dy * dy);
        if (l < 1e-4f) return;
        float mx = dx / l * speed * dt, my = dy / l * speed * dt;
        w.map.move(c.x, c.y, mx, my, 0.12f, out);
        boolean blocked = Math.abs(out[0] - (c.x + mx)) + Math.abs(out[1] - (c.y + my)) > Math.abs(mx) + Math.abs(my);
        if (w.map.field(out[0], out[1]) > 0.86f || w.map.inSecret(out[0], out[1], 0.6f)) blocked = true; else { c.x = out[0]; c.y = out[1]; }
        if (blocked) { c.dirX = -c.dirX + (rng.nextFloat() - 0.5f); c.dirY = -c.dirY + (rng.nextFloat() - 0.5f); }
        float sd = c.dirX - c.dirY;
        if (Math.abs(sd) > 0.05f) c.facing = sd >= 0f ? 1 : -1;
    }

    private void eat(GameWorld w, Critter c) {
        c.alive = false; c.respawn = 25f + rng.nextFloat() * 20f;
        w.eaten[c.kind]++;
        w.eatChanged = true;
        w.lumi.eat();
        float[][] col = { { 1f, 0.8f, 0.4f }, { 0.5f, 1f, 0.5f }, { 0.8f, 0.6f, 1f } };
        w.particles.burst(c.screenX(), c.screenY() + c.z + 20f, 12, col[c.kind][0], col[c.kind][1], col[c.kind][2]);
        w.sfx("munch", 0.8f);
        if (w.totalEaten() % 10 == 0) { // kleine Feier: Lumi ist begeistert
            w.lumi.cheer();
            w.particles.burst(w.player.screenX(), w.player.screenY() + 90f, 30, 1f, 0.9f, 0.5f);
            w.sfx("collect", 0.7f); w.sfx("lumi_happy", 0.5f);
        }
    }
}
